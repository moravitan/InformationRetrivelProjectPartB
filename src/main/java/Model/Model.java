package Model;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Model extends Observable {
    private ReadFile readFile;
    private Engine engine;
    private long startTime;
    private long endTime;
    private double totalSecondsToIndex;

    public Model() {
        engine = new Engine();
    }

    /**
     * create new index from given corpus path and save it to given index destination
     * @param pathOfCorpus
     * @param pathOfIndexDestination
     * @param stemming
     */
    public void loadPath(String pathOfCorpus, String pathOfIndexDestination, boolean stemming) {
        //add filekind so it would know if its what corpus to parse or where to save the index

        startTime = System.nanoTime();
        //CHANGE
        //readFile = new ReadFile(pathOfCorpus,pathOfIndexDestination,stemming);
        //try {
            engine.setParameters(pathOfCorpus, pathOfIndexDestination, stemming);
            engine.start();
            endTime = System.nanoTime();
            totalSecondsToIndex = (endTime - startTime) / 1000000000.0;
        //} catch (Exception e){ throw e;}
    }

    //this func will delete all posting and dictionary files that have been saved
    //the posting files will be at the same path as been given while creating them
    //also need to clear main memory in the program
    public void resetAll() {
        //readFile.getParser().getIndexer().reset();
        engine.reset();
        readFile = null;
        System.gc();
    }

    /**
     * this func will return list of all documents languages sorted
     * @return
     */
    public TreeSet<String> getDocumentsLanguages() {
        return ReadFile.corpusLanguages;

    }

    public TreeSet<String> dictionaryToString() {
        //return readFile.getParser().getIndexer().toString();
        return engine.dictionaryToString();
        //create a func which return a string which represent the dictionary SORTED like this:
        // term, numberOfPerformances
        // dad, 50
        // family, 12
        // Mom, 52
    }


    public void uploadDictionaryToMem() {
        engine.setDictionary();
    }

    public int getNumberOfDocs(){ return engine.getNumberOfDocuments(); }

    public int getNumberOfTerms(){ return engine.getNumberOfTerms(); }

    public double getTotalTimeToIndex(){
        return totalSecondsToIndex;
    }

    public TreeMap<Integer, Vector<String>> processQuery(String queriesFile, HashSet<String> cities, boolean isSemantic) {
        return engine.searchSingleQuery(queriesFile,cities,isSemantic);
    }

    public TreeMap<Integer, Vector<String>> processQuery(File queriesFile, HashSet<String> cities, boolean isSemantic) {
        return engine.searchMultipleQueries(queriesFile,cities, isSemantic);
    }


    public void setPathToSaveIndex(String absolutePath) {
        engine.setPathToSaveIndex(absolutePath);
    }

    public void setIsStemming(boolean isStemming) {
        engine.setStemming(isStemming);
    }

    public TreeSet<String> readDocumentsCities() {
        return engine.readDocumentsCities();
    }

    public boolean checkValidPath(){
        return engine.checkValidPath();
    }

    public void saveQueryResultToFile(String path){
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
