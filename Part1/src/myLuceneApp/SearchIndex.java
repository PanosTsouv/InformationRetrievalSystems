package myLuceneApp;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.FSDirectory;

import Utils.IO;
import Utils.MyQuery;

public class SearchIndex {
    public SearchIndex(String indexPath, String field) throws IOException, ParseException
    {
        int[] hits = {20,30,50};
        IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        indexSearcher.setSimilarity(new ClassicSimilarity());

        final ArrayList<String> stopWords = IO.loadStopWords();
        final CharArraySet stopSet = new CharArraySet(stopWords, true);
        Analyzer analyzer = new EnglishAnalyzer(stopSet);
        QueryParser parser = new QueryParser(field, analyzer);

        ArrayList<MyQuery> myQueries = IO.loadQueries();
        for(int k : hits)
        {
            System.out.println("Store result for k = " + k);
            PrintWriter resultWriter = new PrintWriter("trec_eval/results_k" + k + "_classic" + ".test");
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
            if(MyLuceneApp.qRelsIds.contains(question.getId()))
            {
                resultWriter.println(question.getId() + " 0 " + hitDoc.get("id") + " 0 " + hits[i].score + " ClassicSimilarity");
            }
        }  
    }
}
