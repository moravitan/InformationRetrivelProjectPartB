package Model;

/**
 * This class saves the details of the terms in the program
 */
public class TermRankDetails {

    // pointer to the row number in the posting file
    private String term;
    // number of appearance in the entire corpus
    private int TF;

    public TermRankDetails(String term, int TF) {
        this.term = term;
        this.TF = TF;
    }

    public String getTerm() {
        return term;
    }

    public int getTF() {
        return TF;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public void setTF(int TF) {
        this.TF = TF;
    }
}

