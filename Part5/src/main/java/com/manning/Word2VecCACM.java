package com.manning;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.Arrays;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.deeplearning4j.models.embeddings.learning.impl.elements.SkipGram;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.ops.transforms.Transforms;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;

import myUtils.IO;
import myUtils.MyDoc;
import myUtils.MyQuery;

public class Word2VecCACM {

    public ArrayList<String> qRelsIds;

    HashMap<String, INDArray> queriesVectors = new HashMap<>();
    HashMap<String, INDArray> docsVectors = new HashMap<>();
    HashMap<String, HashMap<String, Double>> queryDocsScores = new HashMap<>();
    String fieldName = "mainContent";
    Word2Vec vec = null;
    IndexReader reader;

    public void createIndex() throws IOException, ParseException {
        String indexPath = "index";
        System.out.println("Indexing to directory '" + indexPath + "'...");
        long start = Calendar.getInstance().getTimeInMillis();
        Directory dir = FSDirectory.open(Paths.get(indexPath));

        final ArrayList<String> stopWords = IO.loadStopWords();
        final CharArraySet stopSet = new CharArraySet(stopWords, true);
        Analyzer analyzer = new EnglishAnalyzer(stopSet);

        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

        iwc.setOpenMode(OpenMode.CREATE);

        FieldType type = new FieldType(TextField.TYPE_STORED);
        type.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
        type.setTokenized(true);
        type.setStored(true);
        type.setStoreTermVectors(true);
        type.setStoreTermVectorOffsets(true);
        type.setStoreTermVectorPositions(true);

        IndexWriter indexWriter = new IndexWriter(dir, iwc);

        ArrayList<MyDoc> docs = IO.loadCacmDatabaseAsListOfDocs();
        for (MyDoc doc : docs) {
            String fullSearchableText = doc.getAuthors() + " " + doc.getTitle() + " " + doc.getAbstractText() + " "
                    + doc.getKeywords();
            fullSearchableText = fullSearchableText.toLowerCase().replaceAll("\\W", " ");
            addDocWithTermVector(indexWriter, "mainContent", fullSearchableText, type, doc.getId());
        }
        indexWriter.commit();
        indexWriter.close();

        long end = Calendar.getInstance().getTimeInMillis();
        System.out.println("Index was created  " + (end - start) + " milliseconds");
    }

    private void addDocWithTermVector(IndexWriter writer, String title, String value, FieldType type, String id)
            throws IOException {
        Document luceneDoc = new Document();

        StoredField docID = new StoredField("id", id);
        luceneDoc.add(docID);

        Field field = new Field(title, value, type);
        luceneDoc.add(field);
        if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
            writer.addDocument(luceneDoc);
        }
    }

    private void trainWord2Vec(boolean existModel) throws IOException, ParseException {
        String indexPath = "index";
        reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
        FieldValuesSentenceIterator fieldValuesSentenceIterator = new FieldValuesSentenceIterator(reader, fieldName);

        if(!existModel)
        {
            vec = new Word2Vec.Builder().layerSize(1000).windowSize(10)
            .tokenizerFactory(new DefaultTokenizerFactory()).iterate(fieldValuesSentenceIterator)
            .elementsLearningAlgorithm(new SkipGram<>()).seed(12345).build();

            vec.fit();
        }
        else
        {
            vec = WordVectorSerializer.readWord2VecModel("model.txt");
        }
    }

    public void createQueriesVector() throws IOException, ParseException
    {
        qRelsIds = IO.fixQrels();

        final ArrayList<String> stopWords = IO.loadStopWords();
        final CharArraySet stopSet = new CharArraySet(stopWords, true);
        QueryParser parser = new QueryParser(fieldName, new EnglishAnalyzer(stopSet));

        ArrayList<MyQuery> myQueries = IO.loadQueries();

        for(MyQuery myQuery : myQueries){
            if(qRelsIds.contains(myQuery.getId()))
            {
                Query query = parser.parse(myQuery.getQuestion());
                String queryString = query.toString(fieldName);
                String[] split = queryString.split(" ");
                INDArray denseAverageQueryVector = vec.getWordVectorsMean(Arrays.asList(split));
                queriesVectors.put(myQuery.getId(), denseAverageQueryVector);
            }
        }
    }

    public void createDocVector() throws IOException
    {
        for(int i = 0; i < reader.numDocs(); i++)
        {
            Terms docTerms = reader.getTermVector(i, fieldName);
            INDArray denseDocumentVector = VectorizeUtils.toDenseAverageVector(docTerms, reader.numDocs(), vec, WordEmbeddingsSimilarity.Smoothing.MEAN);
            if(String.valueOf(i+1).length() < 4)
            {
                String temp = String.valueOf(i+1);
                while(temp.length() < 4)
                {
                    temp = "0" + temp;
                }
                docsVectors.put(temp, denseDocumentVector);
            }
            else
            {
                docsVectors.put(String.valueOf(i+1), denseDocumentVector);
            }
        }
    }

    public void calculateScore()
    {
        for(String queryId : queriesVectors.keySet())
        {
            HashMap<String, Double> docsScore = new HashMap<>();
            for(String docId : docsVectors.keySet())
            {
                double value = Transforms.cosineSim(queriesVectors.get(queryId), docsVectors.get(docId));
                if(Double.isNaN(value)){
                    value = 0.0;
                }
                docsScore.put(docId, value);
            }
            queryDocsScores.put(queryId, sortByValue(docsScore));
        }
    }

    public void createResults(boolean existModel) throws IOException{
        int[] hits = {20, 30, 50};
        for(int k : hits)
        {
            System.out.println("Store result for k = " + k);
            PrintWriter resultWriter;
            if(existModel)
            {
                resultWriter = new PrintWriter("trec_eval/results_k" + k + "_w2v_existModel" + ".test");
            }
            else
            {
                resultWriter = new PrintWriter("trec_eval/results_k" + k + "_w2v" + ".test");
            }
            SortedSet<String> keys = new TreeSet<>(queryDocsScores.keySet());
            for(String queryId : keys)
            {
                int counter = 0;
                for(String docId : queryDocsScores.get(queryId).keySet())
                {
                    resultWriter.println(queryId + " 0 " + docId + " 0 " + queryDocsScores.get(queryId).get(docId) + " a");
                    if(counter >= k)
                    {
                        break;
                    }
                    counter++;
                }
            }
            resultWriter.close();
        }
        reader.close();
    }

    public <K, V extends Comparable<? super V>> HashMap<K, V> sortByValue(HashMap<K, V> map) {
        List<Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Entry.comparingByValue());

        HashMap<K, V> result = new LinkedHashMap<>();
        ListIterator<Entry<K, V>> itr = list.listIterator(list.size());
        while (itr.hasPrevious()){
            Entry<K, V> temp = itr.previous();
            result.put(temp.getKey(), temp.getValue());
        }

        return result;
    }

    public static void main(String[] args) throws ParseException {
        Word2VecCACM myObj = new Word2VecCACM();
        try {
            myObj.createIndex();

            System.out.println("Train my model");
            myObj.trainWord2Vec(false);
            myObj.createQueriesVector();
            myObj.createDocVector();
            myObj.calculateScore();
            myObj.createResults(false);

            System.out.println("Use existing model");
            myObj.trainWord2Vec(true);
            myObj.createQueriesVector();
            myObj.createDocVector();
            myObj.calculateScore();
            myObj.createResults(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
