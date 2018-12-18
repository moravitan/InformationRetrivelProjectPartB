package Model;

/**
 * This class saves the details of the terms in the program
 */
public class TermDetails {

    // pointer to the row number in the posting file
    private int ptr;
    // number of appearance in the entire corpus
    private int totalTF;
    // number of documents the term appears in
    private int documentFrequency;


    public TermDetails(int totalTF, int documentFrequency) {
        this.totalTF = totalTF;
        this.documentFrequency = documentFrequency;
    }

    //<editor-fold desc="Setters">
    public void setPtr(int ptr) {
        this.ptr = ptr;
    }

    public void setTotalTF(int totalTF) {
        this.totalTF = this.totalTF + totalTF;
    }

    public void addDocumentFrequency() {
        this.documentFrequency++;
    }

    public void setDocumentFrequency(int documentFrequency) {
        this.documentFrequency = documentFrequency;
    }


    //</editor-fold>

    //<editor-fold desc="Getters">
    public int getPtr() {
        return ptr;
    }

    public int getTotalTF() {
        return totalTF;
    }

    public int getDocumentFrequency() {
        return documentFrequency;
    }


    //</editor-fold>
}

