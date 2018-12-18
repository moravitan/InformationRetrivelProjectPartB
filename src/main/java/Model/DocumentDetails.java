package Model;

import java.util.TreeMap;

/**
 * This class saves all the details of each document in the corpus
 */
public class DocumentDetails {

    String fileName;
    String language;
    String date;
    int maxTermFrequency;
    int numberOfDistinctWords;
    TreeMap<Integer,String> topFiveEntities;



    /**
     * Constructor
     * @param date
     * @param fileName
     */
    public DocumentDetails(String fileName,String date, String language) {
        this.fileName = fileName;
        this.date = date;
        this.language = language;
    }


    //<editor-fold desc="Getters">

    public String getFileName() {
        return fileName;
    }


    public String getLanguage() {
        return language;
    }

    public String getDate() {
        return date;
    }

    public int getMaxTermFrequency() {
        return maxTermFrequency;
    }

    public int getNumberOfDistinctWords() {
        return numberOfDistinctWords;
    }

    public TreeMap<Integer, String> getTopFiveEntities() {
        return topFiveEntities;
    }
    //</editor-fold>


    //<editor-fold desc="Setters">


    public void setLanguage(String language) {
        this.language = language;
    }


    public void setMaxTermFrequency(int maxTermFrequency) {
        this.maxTermFrequency = maxTermFrequency;
    }

    public void setNumberOfDistinctWords(int numberOfDistinctWords) {
        this.numberOfDistinctWords = numberOfDistinctWords;
    }

    public void setTopFiveEntities(TreeMap<Integer, String> topFiveEntities) {
        this.topFiveEntities = topFiveEntities;
    }
    //</editor-fold>
}
