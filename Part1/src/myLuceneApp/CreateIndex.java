package myLuceneApp;


import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import Utils.*;

public class CreateIndex {

    public CreateIndex(String indexPath) throws IOException{
        System.out.println("Indexing to directory '" + indexPath + "'...");
        long start = Calendar.getInstance().getTimeInMillis();
        Directory dir = FSDirectory.open(Paths.get(indexPath));

        final ArrayList<String> stopWords = IO.loadStopWords();
        final CharArraySet stopSet = new CharArraySet(stopWords, true);
        Analyzer analyzer = new EnglishAnalyzer(stopSet);

        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setSimilarity(new ClassicSimilarity());

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
        TextField mainContent = new TextField("mainContent", fullSearchableText, Field.Store.NO);
        luceneDoc.add(mainContent);

        if (indexWriter.getConfig().getOpenMode() == OpenMode.CREATE) {
            try {
                indexWriter.addDocument(luceneDoc);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } 
    }
}
