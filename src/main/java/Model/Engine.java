package Model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.TreeSet;

public class Engine {


    private ReadFile readFile;
    private Parse parse;
    private Indexer indexer;
    private HashMap<String,TermDetails> dictionary;// <Term, df, totalTF, ptr>
    private String pathToSaveIndex;




    public Engine(String pathToParse,String pathToSaveIndex,  boolean isStemming) {
        this.pathToSaveIndex = pathToSaveIndex;
        indexer = new Indexer(pathToSaveIndex,isStemming);
        parse = new Parse(isStemming,indexer);
        readFile = new ReadFile(pathToParse,parse);
    }

    public void start() throws Exception {
            readFile.startReading();
            // write last data to disk
            indexer.writeDataToDisk();
            // creating inverted index
            indexer.createInvertedIndex();
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




}
