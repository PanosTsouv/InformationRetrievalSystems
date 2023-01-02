package myLuceneApp;


import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.classification.utils.DocToDoubleVectorUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import Utils.*;

public class CreateIndex {

    public CreateIndex(){}

    public void createPhaseTwoIndex(String indexPath) throws IOException
    {
        System.out.println("Indexing to directory '" + indexPath + "'...");
        long start = Calendar.getInstance().getTimeInMillis();
        Directory dir = FSDirectory.open(Paths.get(indexPath));

        final ArrayList<String> stopWords = IO.loadStopWords();
        final CharArraySet stopSet = new CharArraySet(stopWords, true);
        Analyzer analyzer = new EnglishAnalyzer(stopSet);

        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setSimilarity(new ClassicSimilarity());

        iwc.setOpenMode(OpenMode.CREATE);

        FieldType type = new FieldType();
        type.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
        type.setTokenized(true);
        type.setStored(true);
        type.setStoreTermVectors(true);

        IndexWriter indexWriter = new IndexWriter(dir, iwc);

        ArrayList<MyDoc> docs = IO.loadCacmDatabaseAsListOfDocs();
        for(MyDoc doc : docs)
        {
            String fullSearchableText = doc.getAuthors() + " " + doc.getTitle() + " " + doc.getAbstractText() + " " + doc.getKeywords();
            addDocWithTermVector(indexWriter, "mainContent", fullSearchableText, type);
        }

        indexWriter.close();

        long end = Calendar.getInstance().getTimeInMillis();
        System.out.println("Index was created  " + (end-start) + " milliseconds");
    }

    private void addDocWithTermVector(IndexWriter writer, String title, String value, FieldType type) throws IOException {
		Document luceneDoc = new Document();

		Field field = new Field(title, value, type);		
		luceneDoc.add(field);
        if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
		    writer.addDocument(luceneDoc);
        }
	}
 
	public ArrayList<Double[]> createTermXDocMatrix(String indexPath, String title) throws IOException {
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
		Terms fieldTerms = MultiFields.getTerms(reader, "mainContent");
		System.out.println("Terms:" + fieldTerms.size());

        System.out.println("Start create term x doc matrix");
        ArrayList<Double[]> myMatrix = new ArrayList<>();
		if (fieldTerms != null && fieldTerms.size() != -1) {
			IndexSearcher indexSearcher = new IndexSearcher(reader);
			for (ScoreDoc scoreDoc : indexSearcher.search(new MatchAllDocsQuery(), Integer.MAX_VALUE).scoreDocs) {
				Terms docTerms = reader.getTermVector(scoreDoc.doc, "mainContent");
				
				Double[] vector = DocToDoubleVectorUtils.toSparseLocalFreqDoubleArray(docTerms, fieldTerms);
				myMatrix.add(vector);
			}
		}
        reader.close();
        System.out.println("Term x Doc matrix done!");
        return myMatrix;
	}

    public ArrayList<Double[]> createQueriesAsTermArray(String indexPath) throws IOException, ParseException
    {
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
		Terms fieldTerms = MultiFields.getTerms(reader, "mainContent");
		System.out.println("Terms:" + fieldTerms.size());

        System.out.println("Start create term x queries matrix");
        final ArrayList<String> stopWords = IO.loadStopWords();
        final CharArraySet stopSet = new CharArraySet(stopWords, true);
        Analyzer analyzer = new EnglishAnalyzer(stopSet);
            
        QueryParser parser = new QueryParser("contents", analyzer);

        ArrayList<MyQuery> myQueries = IO.loadQueries();

        ArrayList<Double[]> queriesXterms = new ArrayList<>();
        for(MyQuery question : myQueries){
            Query query = parser.parse(question.getQuestion());
            String[] analuzeQuery = query.toString("contents").split(" ");

            Double[] termExist = new Double[(int)fieldTerms.size()];
            Arrays.fill(termExist, 0.0);
            TermsEnum it = fieldTerms.iterator();
            int count = 0;
            while(it.next() != null) {
                for(String word : analuzeQuery)
                {
                    if(word.equals(it.term().utf8ToString()))
                    {
                        termExist[count] += 1.0;
                    }
                }
                count++;
            }
            queriesXterms.add(termExist);
        }
        reader.close();
        System.out.println("Term x queries matrix done");

        return queriesXterms;
    }
}
