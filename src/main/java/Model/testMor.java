package Model;

import java.io.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Vector;

public class testMor {

    public static void main(String[] args) {
        testQueries();
//        testEngine();
//        splitQruels();

    }

    public static void testEngine(){
        String pathToParse = "C:\\Users\\איתן אביטן\\Downloads\\לימודים\\אחזור מידע\\פרויקט מנוע חיפוש\\corpus\\corpus";
        String pathToSaveIndex = "C:\\Users\\איתן אביטן\\Downloads\\לימודים\\אחזור מידע\\פרויקט מנוע חיפוש\\indexer";
        Engine engine = new Engine();
        engine.setParameters(pathToParse,pathToSaveIndex,false);
        try {
            engine.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void splitQruels(){
        try {
            BufferedReader bf = new BufferedReader(new FileReader("C:\\Users\\איתן אביטן\\Downloads\\לימודים\\אחזור מידע\\פרויקט מנוע חיפוש\\qrels.txt"));
            String line = bf.readLine();
            String fileName = "";
            String[] split = line.split(" ");
            fileName = split[0];
            FileWriter writer = new FileWriter(new File("C:\\Users\\איתן אביטן\\Downloads\\לימודים\\אחזור מידע\\פרויקט מנוע חיפוש\\" + fileName + ".txt"));
            while (line != null) {
                if (split[3].equals("1"))
                    writer.write(split[2] + "\n");
                line = bf.readLine();
                if (line == null) break;
                split = line.split(" ");
                if (!split[0].equals(fileName)){
                    fileName = split[0];
                    writer.flush();
                    writer.close();
                    writer = new FileWriter(new File("C:\\Users\\איתן אביטן\\Downloads\\לימודים\\אחזור מידע\\פרויקט מנוע חיפוש\\" + fileName + ".txt"));
                }
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void testQueries(){
        HashSet<String> cities = new HashSet<>();
        Engine engine = new Engine();
        engine.setStemming(true);
        engine.setPathToSaveIndex("C:\\Users\\איתן אביטן\\Downloads\\לימודים\\אחזור מידע\\פרויקט מנוע חיפוש\\indexer");
        engine.setDictionary();
       // engine.readDocumentsCities();
        engine.searchMultipleQueries(new File("C:\\Users\\איתן אביטן\\Downloads\\לימודים\\אחזור מידע\\פרויקט מנוע חיפוש\\queries.txt"),cities,true);
        setResult();
        //saveQueryResultToFile("");
    }

    private static void setResult() {
        int totalCounter = 0;
        int counter = 0;
        for(Map.Entry<Integer, Vector<String>> entry: Searcher.result.entrySet()){
            HashSet<String> resules = new HashSet<>();
            try {
                BufferedReader bf = new BufferedReader(new FileReader("C:\\Users\\איתן אביטן\\Downloads\\לימודים\\אחזור מידע\\פרויקט מנוע חיפוש\\spliter qrels\\" + entry.getKey() + ".txt"));
                String line = bf.readLine();
                while (line != null){
                    resules.add(line);
                    line = bf.readLine();
                }
                bf.close();
                for(String str : entry.getValue()) {
                    if (resules.contains(str)) {
                        counter++;
                        totalCounter++;
                    }
                }
                System.out.println("Number of matches for query: " + entry.getKey() + " " + counter + " from " + resules.size()) ;
                counter = 0;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Number of total matches : " + totalCounter);
    }

    public static void saveQueryResultToFile(String path){
        try {
            FileWriter fileForTrecEval = new FileWriter (new File(path + "\\fileForTrecEval.txt"));
            for(Map.Entry<Integer,Vector<String>> entry: Searcher.result.entrySet()){
                String queryId = String.valueOf(entry.getKey());
                Vector<String> resultsForQuery = entry.getValue();
                for (String docId: resultsForQuery){
                    fileForTrecEval.write(queryId + " 0 " + docId + " 0 0.0 mt" + "\n");
                }
            }
            fileForTrecEval.flush();
            fileForTrecEval.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
