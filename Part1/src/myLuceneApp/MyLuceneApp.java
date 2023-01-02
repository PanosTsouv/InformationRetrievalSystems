package myLuceneApp;

import java.util.ArrayList;

import Utils.IO;

public class MyLuceneApp {

    public static ArrayList<String> qRelsIds;
    public static void main(String[] args) throws Exception {
        String indexPath = ("index");

        qRelsIds = IO.fixQrels();

        CreateIndex myIndex = new CreateIndex(indexPath);

        SearchIndex mySearcher = new SearchIndex(indexPath, "mainContent");
    }
}
