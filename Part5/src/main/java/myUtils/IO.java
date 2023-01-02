package myUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class IO {
    public static ArrayList<MyDoc> loadCacmDatabaseAsListOfDocs() throws IOException{
        String databasePath = "cacm/cacm.all";
        BufferedReader bufferedReader = new BufferedReader(new FileReader(databasePath));
        String line = null;
        ArrayList<MyDoc> cacmDocs = new ArrayList<>();
        String currentCategory = "";
        System.out.println("Start reading database....");
        while ((line = bufferedReader.readLine()) != null) {
            if(line.startsWith(".I"))
            {
                MyDoc currentDoc = new MyDoc();
                cacmDocs.add(currentDoc);
                if(line.split(" ")[1].length() == 1) currentDoc.setId("000" + line.split(" ")[1]);
                if(line.split(" ")[1].length() == 2) currentDoc.setId("00" + line.split(" ")[1]);
                if(line.split(" ")[1].length() == 3) currentDoc.setId("0" + line.split(" ")[1]);
                if(line.split(" ")[1].length() == 4) currentDoc.setId(line.split(" ")[1]);
            }

            if (currentCategory.equals(".A") && !line.startsWith("."))
            {
                cacmDocs.get(cacmDocs.size()-1).setAuthors(cacmDocs.get(cacmDocs.size()-1).getAuthors() + line.replaceAll("[\"*\';]", "").replaceAll("[()]", "").replaceAll("/", "") + " ");
            }else if(currentCategory.equals(".B") && !line.startsWith("."))
            {
                cacmDocs.get(cacmDocs.size()-1).setDate(cacmDocs.get(cacmDocs.size()-1).getDate() + line.replaceAll("[\"*\']", "").replaceAll("[()]", "").replaceAll("/", "") + " ");
            }else if(currentCategory.equals(".C") && !line.startsWith("."))
            {
                cacmDocs.get(cacmDocs.size()-1).setContent(cacmDocs.get(cacmDocs.size()-1).getContent() + line.replaceAll("[\"*\']", "").replaceAll("[()]", "").replaceAll("/", "") + " ");
            }else if(currentCategory.equals(".K") && !line.startsWith("."))
            {
                cacmDocs.get(cacmDocs.size()-1).setKeywords(cacmDocs.get(cacmDocs.size()-1).getKeywords() + line.replaceAll("[\"*\']", "").replaceAll("[()]", "").replaceAll("/", "") + " ");
            }else if(currentCategory.equals(".N") && !line.startsWith("."))
            {
                cacmDocs.get(cacmDocs.size()-1).setEntrydate(cacmDocs.get(cacmDocs.size()-1).getEntrydate() + line.replaceAll("[\"*\']", "").replaceAll("[()]", "").replaceAll("/", "") + " ");
            }else if(currentCategory.equals(".T") && !line.startsWith("."))
            {
                cacmDocs.get(cacmDocs.size()-1).setTitle(cacmDocs.get(cacmDocs.size()-1).getTitle() + line.replaceAll("[\"*\']", "").replaceAll("[()]", "").replaceAll("/", "") + " ");
            }else if(currentCategory.equals(".W") && !line.startsWith("."))
            {
                cacmDocs.get(cacmDocs.size()-1).setAbstractText(cacmDocs.get(cacmDocs.size()-1).getAbstractText() + line.replaceAll("[\"*\']", "").replaceAll("[()]", "").replaceAll("/", "") + " ");
            }else if(currentCategory.equals(".X") && !line.startsWith("."))
            {
                cacmDocs.get(cacmDocs.size()-1).setReferences(cacmDocs.get(cacmDocs.size()-1).getReferences() + line.replaceAll("\\s+", " ") + " ");
            }


            if(line.startsWith(".A"))
            {
                currentCategory = ".A";
            }else if(line.startsWith(".B"))
            {
                currentCategory = ".B";
            }else if(line.startsWith(".C"))
            {
                currentCategory = ".C";
            }else if(line.startsWith(".K"))
            {
                currentCategory = ".K";
            }else if(line.startsWith(".N"))
            {
                currentCategory = ".N";
            }else if(line.startsWith(".T"))
            {
                currentCategory = ".T";
            }else if(line.startsWith(".W"))
            {
                currentCategory = ".W";
            }else if(line.startsWith(".X"))
            {
                currentCategory = ".X";
            }
        }

        System.out.println("\nReading database was successfully!");
        System.out.println("Store " + cacmDocs.size() + " documents in a list!");

        bufferedReader.close();
        return cacmDocs;
    }


    public static ArrayList<MyQuery> loadQueries() throws IOException
    {
        String databasePath = "cacm/query.text";
        BufferedReader bufferedReader = new BufferedReader(new FileReader(databasePath));
        String line = null;
        String currentCategory = "";
        ArrayList<MyQuery> cacmQueries = new ArrayList<>();
        while ((line = bufferedReader.readLine()) != null) {
            if(line.startsWith(".I"))
            {
                MyQuery currentQuery = new MyQuery();
                cacmQueries.add(currentQuery);
                if(line.split(" ")[1].length() == 1) currentQuery.setId("0" + line.split(" ")[1]);
                if(line.split(" ")[1].length() == 2) currentQuery.setId(line.split(" ")[1]);
            }

            if (currentCategory.equals(".W") && !line.startsWith("."))
            {
                cacmQueries.get(cacmQueries.size()-1).setQuestion(cacmQueries.get(cacmQueries.size()-1).getQuestion() + line.replaceAll("[\"*\']", "").replaceAll("[()]", "").replaceAll("/", "") + " ");
            }else if (currentCategory.equals(".A") && !line.startsWith("."))
            {
                cacmQueries.get(cacmQueries.size()-1).setAuthors(cacmQueries.get(cacmQueries.size()-1).getAuthors() + line.replaceAll("[\"*\']", "").replaceAll("[()]", "").replaceAll("/", "") + " ");
            }else if(currentCategory.equals(".N") && !line.startsWith("."))
            {
                cacmQueries.get(cacmQueries.size()-1).setQuerist(cacmQueries.get(cacmQueries.size()-1).getQuerist() + line.replaceAll("[\"*\']", "").replaceAll("[()]", "").replaceAll("/", "") + " ");
            }

            if(line.startsWith(".W"))
            {
                currentCategory = ".W";
            }else if(line.startsWith(".A"))
            {
                currentCategory = ".A";
            }else if(line.startsWith(".N"))
            {
                currentCategory = ".N";
            }
        }

        System.out.println("\nReading queries was successfully!");
        System.out.println("Store " + cacmQueries.size() + " queries in a list!");

        bufferedReader.close();
        return cacmQueries;
    }

    public static ArrayList<String> fixQrels() throws IOException
    {
        ArrayList<String> qRelsIds = new ArrayList<>();
        String databasePath = "cacm/qrels.text";
        BufferedReader bufferedReader = new BufferedReader(new FileReader(databasePath));
        PrintWriter qrelsWriter = new PrintWriter("trec_eval/fixQrels.test");
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            String[] temp = line.split(" ");
            qRelsIds.add(temp[0]);
            if(temp.length > 1) qrelsWriter.println(temp[0] + " 0 " + temp[1] + " 1");
        }
        bufferedReader.close();
        qrelsWriter.close();

        return qRelsIds;
    }

    public static ArrayList<String> loadStopWords() throws IOException
    {
        ArrayList<String> stopWords = new ArrayList<>();
        String stopWordsPath = "cacm/common_words";
        BufferedReader bufferedReader = new BufferedReader(new FileReader(stopWordsPath));
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            stopWords.add(line);
        }
        bufferedReader.close();

        return stopWords;
    }
}
