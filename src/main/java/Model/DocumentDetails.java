package Model;

import java.util.TreeMap;

/**
 * This class saves all the details of each document in the corpus
 */
public class DocumentDetails {

    private String fileName;
    private String language;
    private String date;
    private int maxTermFrequency;
    private int numberOfDistinctWords;
    private int length;
    private TreeMap<Integer,String> topFiveEntities;



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

    public int getLength() {
        return length;
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

    public void setLength(int length) {
        this.length = length;
    }
    //</editor-fold>
}
