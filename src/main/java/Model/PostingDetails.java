package Model;

public class PostingDetails {

    private String docId;
    private int TF;
    // normalized tf
    private int normalizedTf;

    public PostingDetails(String docId, int TF) {
        this.docId = docId;
        this.TF = TF;
    }

    public String getDocId() {
        return docId;
    }

    public int getTF() {
        return TF;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public void setTF(int TF) {
        this.TF = TF;
    }
}
