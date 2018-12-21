package Model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Engine {


    private ReadFile readFile;
    private Parse parse;
    private Indexer indexer;
    private Searcher searcher;
    private Boolean isStemming;
    public static HashMap<String,TermDetails> dictionary;// <Term, df, totalTF, ptr>
    public static HashMap<String,DocumentDetails> mapOfDocs = new HashMap<>();
    public static String pathToSaveIndex;

    public Engine() {
        this.parse = new Parse(false,null);
        searcher = new Searcher(parse);
    }

    /**
     *
     * @param pathToParse
     * @param pathToSaveIndex
     * @param isStemming
     */
    public void setParameters(String pathToParse, String pathToSaveIndex, boolean isStemming) {
        Engine.pathToSaveIndex = pathToSaveIndex;
        this.isStemming = isStemming;
        indexer = new Indexer(pathToSaveIndex,isStemming);
        parse.setStemming(isStemming);
        parse.setIndexer(indexer);
        readFile = new ReadFile(pathToParse,parse);
    }

    /**
     *
     */
    public void start() {
        try {
            readFile.startReading();
            // write last data to disk
            indexer.writeDataToDisk();
            // creating inverted index
            indexer.createInvertedIndex();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void reset() {
        indexer.reset();
    }

    /**
     * This method write to content of the file 'Dictionary.txt' to the Dictionary field
     */
    public void setDictionary(){
        dictionary = new HashMap<>();
        try{
            File dictionaryFile = new File(pathToSaveIndex + "\\Dictionary.txt");
            BufferedReader bf = new BufferedReader(new FileReader(dictionaryFile));
            String line = bf.readLine();
            while (line != null){
                int[] details = getDetail(line);
                TermDetails termDetails = new TermDetails(details[1], details[2]);
                termDetails.setPtr(details[3]);
                dictionary.put(line.substring(0,details[0]), termDetails);

                line = bf.readLine();
            }
            bf.close();
            setDocumentDetails();
        } catch (IOException e) { }
    }

    public int getNumberOfDocuments(){
        return parse.numberOfDocuments;
    }

    public int getNumberOfTerms(){
        return indexer.getNumberOfTerms();
    }

    public TreeSet<String> dictionaryToString() {
        return indexer.dictionaryToString();
    }

    public TreeMap<Integer, Vector<String>> searchSingleQuery(String query, HashSet<String> cities){
        return searcher.processQuery(query,cities);
    }

    public TreeMap<Integer, Vector<String>> searchMultipleQueries(File query, HashSet<String> cities){
        return searcher.processQuery(query,cities);
    }

    public HashMap<String, TermDetails> getDictionary() {
        return dictionary;
    }

    private static int [] getDetail(String line){
        int i;
        int counter = 0;
        int [] ans = new int [4];
        for (i = line.length() - 1; i > -1 ; i--) {
            if (line.charAt(i) == ','){
                counter++;
            }
            if (counter == 3) break;

        }
        line = line.substring(i+1);
        String [] split = line.split(",");
        ans[0] = i;
        ans[1] = Integer.valueOf(split[0]);
        ans[2] = Integer.valueOf(split[1]);
        ans[3] = Integer.valueOf(split[2]);
        return ans;
    }


    public void setPathToSaveIndex(String absolutePath) {
        if (!isStemming)
            Engine.pathToSaveIndex = absolutePath + "\\posting";
        else
            Engine.pathToSaveIndex = absolutePath + "\\postingStemming";
    }

    public void setStemming(Boolean stemming) {
        isStemming = stemming;
        parse.setStemming(stemming);
    }

    public TreeSet<String> readDocumentsCities() {
        TreeSet cities = new TreeSet();
        try{
            File dictionaryFile = new File(pathToSaveIndex + "\\cityDictionary.txt");
            BufferedReader bf = new BufferedReader(new FileReader(dictionaryFile));
            String line = bf.readLine();
            while (line != null){
                String city = line.substring(0,line.indexOf(","));
                cities.add(city);
                line = bf.readLine();
            }
            bf.close();
        } catch (IOException e) { }
        return cities;
    }

    public void setDocumentDetails(){
        try{
            File dictionaryFile = new File(pathToSaveIndex + "\\documentsDetails.txt");
            BufferedReader bf = new BufferedReader(new FileReader(dictionaryFile));
            String line = bf.readLine();
            while (line != null){
                // docId, length, fileName,language,date,numberOfDistinctWords,max term frequency,{term1:tf1,term2:tf2,...,term5:tf5,}
                String [] details = line.split(",");
                DocumentDetails documentDetails = new DocumentDetails(details[2],details[4],details[3]);
                documentDetails.setLength(Integer.valueOf(details[1]));
                documentDetails.setNumberOfDistinctWords(Integer.valueOf(details[5]));
                documentDetails.setMaxTermFrequency(Integer.valueOf(details[6]));
                mapOfDocs.put(details[0], documentDetails);
                line = bf.readLine();
            }
            bf.close();
        } catch (IOException e) { }
    }
}
