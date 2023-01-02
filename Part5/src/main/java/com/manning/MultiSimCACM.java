package com.manning;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;

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
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.MultiSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;

import myUtils.IO;
import myUtils.MyDoc;
import myUtils.MyQuery;

public class MultiSimCACM {

    Word2Vec vec = null;

    public MultiSimCACM(){}
    
    public void createIndex(String indexPath, String similarity, float lamda) throws IOException{
        System.out.println("Indexing to directory '" + indexPath + "'...");
        long start = Calendar.getInstance().getTimeInMillis();
        Directory dir = FSDirectory.open(Paths.get(indexPath));

        final ArrayList<String> stopWords = IO.loadStopWords();
        final CharArraySet stopSet = new CharArraySet(stopWords, true);
        Analyzer analyzer = new EnglishAnalyzer(stopSet);

        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        if(similarity.equals("Classic_BM25"))
        {
            iwc.setSimilarity(new MultiSimilarity(new Similarity[] { new ClassicSimilarity(), new BM25Similarity() }));
        }
        else if(similarity.equals("Classic_LM"))
        {
            iwc.setSimilarity(new MultiSimilarity(new Similarity[] { new ClassicSimilarity(), new LMJelinekMercerSimilarity(lamda) }));
        }
        else if(similarity.equals("BM25_LM"))
        {
            iwc.setSimilarity(new MultiSimilarity(new Similarity[] { new BM25Similarity(), new LMJelinekMercerSimilarity(lamda) }));
        }
        else if(similarity.equals("WV_Classic"))
        {
            iwc.setSimilarity(new MultiSimilarity(new Similarity[] { new WordEmbeddingsSimilarity(vec, "mainContent", WordEmbeddingsSimilarity.Smoothing.MEAN), new ClassicSimilarity() }));
        }
        else if(similarity.equals("WV_BM25"))
        {
            iwc.setSimilarity(new MultiSimilarity(new Similarity[] { new WordEmbeddingsSimilarity(vec, "mainContent", WordEmbeddingsSimilarity.Smoothing.MEAN), new BM25Similarity() }));
        }
        else if(similarity.equals("WV_LM"))
        {
            iwc.setSimilarity(new MultiSimilarity(new Similarity[] { new WordEmbeddingsSimilarity(vec, "mainContent", WordEmbeddingsSimilarity.Smoothing.MEAN), new LMJelinekMercerSimilarity(lamda) }));
        }

        iwc.setOpenMode(OpenMode.CREATE);

        IndexWriter indexWriter = new IndexWriter(dir, iwc);

        ArrayList<MyDoc> docs = IO.loadCacmDatabaseAsListOfDocs();

        for(MyDoc doc : docs)
        {
            insertDocToWriter(indexWriter, doc);
        }

        indexWriter.close();

        long end = Calendar.getInstance().getTimeInMillis();
        System.out.println("Index was created  " + (end-start) + " milliseconds");
    }

    private void insertDocToWriter(IndexWriter indexWriter, MyDoc doc) {
        Document luceneDoc = new Document();

        StoredField id = new StoredField("id", doc.getId());
        luceneDoc.add(id);

        String fullSearchableText = doc.getAuthors() + " " + doc.getTitle() + " " + doc.getAbstractText() + " " + doc.getKeywords();
        // TextField mainContent = new TextField("mainContent", fullSearchableText, Field.Store.NO);
        FieldType ft = new FieldType(TextField.TYPE_STORED);
        ft.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
        ft.setTokenized(true);
        ft.setStored(true);
        ft.setStoreTermVectors(true);
        ft.setStoreTermVectorOffsets(true);
        ft.setStoreTermVectorPositions(true);
        Field mainContent = new Field("mainContent", fullSearchableText, ft);
        luceneDoc.add(mainContent);

        if (indexWriter.getConfig().getOpenMode() == OpenMode.CREATE) {
            try {
                indexWriter.addDocument(luceneDoc);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } 
    }

    public void searchIndex(String indexPath, String field, String similarity, float lamda) throws IOException, ParseException
    {
        int[] hits = {20,30,50};
        IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        if(similarity.equals("Classic_BM25"))
        {
            indexSearcher.setSimilarity(new MultiSimilarity(new Similarity[] { new ClassicSimilarity(), new BM25Similarity() }));
        }
        else if(similarity.equals("Classic_LM"))
        {
            indexSearcher.setSimilarity(new MultiSimilarity(new Similarity[] { new ClassicSimilarity(), new LMJelinekMercerSimilarity(lamda) }));
        }
        else if(similarity.equals("BM25_LM"))
        {
            indexSearcher.setSimilarity(new MultiSimilarity(new Similarity[] { new BM25Similarity(), new LMJelinekMercerSimilarity(lamda) }));
        }
        else if(similarity.equals("WV_Classic"))
        {
            indexSearcher.setSimilarity(new MultiSimilarity(new Similarity[] { new WordEmbeddingsSimilarity(vec, "mainContent", WordEmbeddingsSimilarity.Smoothing.MEAN), new ClassicSimilarity() }));
        }
        else if(similarity.equals("WV_BM25"))
        {
            indexSearcher.setSimilarity(new MultiSimilarity(new Similarity[] { new WordEmbeddingsSimilarity(vec, "mainContent", WordEmbeddingsSimilarity.Smoothing.MEAN), new BM25Similarity() }));
        }
        else if(similarity.equals("WV_LM"))
        {
            indexSearcher.setSimilarity(new MultiSimilarity(new Similarity[] { new WordEmbeddingsSimilarity(vec, "mainContent", WordEmbeddingsSimilarity.Smoothing.MEAN), new LMJelinekMercerSimilarity(lamda) }));
        }

        final ArrayList<String> stopWords = IO.loadStopWords();
        final CharArraySet stopSet = new CharArraySet(stopWords, true);
        Analyzer analyzer = new EnglishAnalyzer(stopSet);
        QueryParser parser = new QueryParser(field, analyzer);

        ArrayList<MyQuery> myQueries = IO.loadQueries();
        for(int k : hits)
        {
            System.out.println("Store result for k = " + k);
            PrintWriter resultWriter = new PrintWriter("trec_eval/results_k" + k + "_" + similarity + ".test");
            for(MyQuery query : myQueries){
                search(indexSearcher, field, parser, query, resultWriter, k);
            }
            resultWriter.close();
        }
        indexReader.close();
    }

    private void search(IndexSearcher indexSearcher, String field, QueryParser parser, MyQuery question, PrintWriter resultWriter, int k) throws IOException, ParseException {
        Query query = parser.parse(question.getQuestion());
        
        TopDocs results = indexSearcher.search(query, k);
        ScoreDoc[] hits = results.scoreDocs;

        //write results
        for(int i=0; i<hits.length; i++){
            Document hitDoc = indexSearcher.doc(hits[i].doc);
            if(qRelsIds.contains(question.getId()))
            {
                resultWriter.println(question.getId() + " 0 " + hitDoc.get("id") + " 0 " + hits[i].score + " ClassicSimilarity");
            }
        }  
    }

    private void trainWord2Vec(boolean existModel) throws IOException, ParseException {
        vec = WordVectorSerializer.readWord2VecModel("model.txt");
    }

    public static ArrayList<String> qRelsIds;
    public static void main(String[] args) throws Exception {
        String indexPath = ("index");

        qRelsIds = IO.fixQrels();

        float lamda = 0.6f;

        MultiSimCACM myClassic_BM25Index = new MultiSimCACM();
        MultiSimCACM myClassic_LMIndex = new MultiSimCACM();
        MultiSimCACM myBM25_LMIndex = new MultiSimCACM();
        MultiSimCACM myWV_ClassicIndex = new MultiSimCACM();
        MultiSimCACM myWV_BM25Index = new MultiSimCACM();
        MultiSimCACM myWV_LMIndex = new MultiSimCACM();

        myClassic_BM25Index.createIndex(indexPath + "Classic_BM25", "Classic_BM25", lamda);
        myClassic_LMIndex.createIndex(indexPath + "Classic_LM", "Classic_LM", lamda);
        myBM25_LMIndex.createIndex(indexPath + "BM25_LM", "BM25_LM", lamda);
        myWV_ClassicIndex.trainWord2Vec(true);
        myWV_BM25Index.trainWord2Vec(true);
        myWV_LMIndex.trainWord2Vec(true);
        myWV_ClassicIndex.createIndex(indexPath + "WV_Classic", "WV_Classic", lamda);
        myWV_BM25Index.createIndex(indexPath + "WV_BM25", "WV_BM25", lamda);
        myWV_LMIndex.createIndex(indexPath + "WV_LM", "WV_LM", lamda);


        myClassic_BM25Index.searchIndex(indexPath + "Classic_BM25", "mainContent", "Classic_BM25", lamda);
        myClassic_LMIndex.searchIndex(indexPath + "Classic_LM", "mainContent", "Classic_LM", lamda);
        myBM25_LMIndex.searchIndex(indexPath + "BM25_LM", "mainContent", "BM25_LM", lamda);
        myWV_ClassicIndex.searchIndex(indexPath + "WV_Classic", "mainContent", "WV_Classic", lamda);
        myWV_BM25Index.searchIndex(indexPath + "WV_BM25", "mainContent", "WV_BM25", lamda);
        myWV_LMIndex.searchIndex(indexPath + "WV_LM", "mainContent", "WV_LM", lamda);
    }
    
}
