package myLuceneApp;

import java.util.ArrayList;

import Utils.IO;

public class MyLuceneApp {

    public static ArrayList<String> qRelsIds;
    public static void main(String[] args) throws Exception {
        String indexPath = ("index");

        qRelsIds = IO.fixQrels();

        float lamda = 0.6f;

        CreateIndex myBM25Index = new CreateIndex(indexPath + "BM25", "BM25Similarity", lamda);

        CreateIndex myLMJelinekMercerIndex = new CreateIndex(indexPath + "LMJelinekMercer", "LMJelinekMercerSimilarity", lamda);

        SearchIndex myBM25Searcher = new SearchIndex(indexPath + "BM25", "mainContent", "BM25Similarity", lamda);

        SearchIndex myLMJelinekMercerSearcher = new SearchIndex(indexPath + "LMJelinekMercer", "mainContent", "LMJelinekMercerSimilarity", lamda);
    }
}
