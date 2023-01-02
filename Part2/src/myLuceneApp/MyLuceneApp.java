package myLuceneApp;

import java.util.ArrayList;

import Utils.IO;

public class MyLuceneApp {

    public static ArrayList<String> qRelsIds;
    public static void main(String[] args) throws Exception {
        String indexPath = ("index");

        qRelsIds = IO.fixQrels();

        CreateIndex myIndex = new CreateIndex();

        myIndex.createPhaseTwoIndex(indexPath);

        ArrayList<Double[]> termXdocMatrix = myIndex.createTermXDocMatrix(indexPath, "mainContent");

        ArrayList<Double[]> queriesXterm = myIndex.createQueriesAsTermArray(indexPath);

        IO.write2dArrayToTxt("termXdocArray.txt", termXdocMatrix);

        IO.write2dArrayToTxt("termXqueries.txt", queriesXterm);
    }
}
