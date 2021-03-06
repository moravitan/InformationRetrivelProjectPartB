package Model;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Ranker {

    private HashSet<String> cityDocuments;
    // save for each doc all the common terms with the query
    private HashMap<String, ArrayList<TermRankDetails>> posting; // <docId, term,tf>
    private HashSet<String> notRelevantDocments; // <docId, term,tf>
    private TreeMap<String,Double> ranksPerDocument; // <docId,rank>
    private  int i;


    /**
     * This method rank each document in the corpus against the asked query.
     * The query is represented in the hash map: termsForQueries
     * @param termsForQueries
     * @param cities
     * @param queryId
     */
    public void rank(HashMap<String,Integer> termsForQueries, HashSet<String> cities, String queryId){
        this.posting = new HashMap<>();
        this.ranksPerDocument = new TreeMap<>();
        if(cities.size() > 0)
            getCitiesDocuments(cities);
        getTermsPosting(termsForQueries);
        try {
            BufferedReader bf = new BufferedReader(new FileReader(Engine.pathToSaveIndex + "\\DetailsForRank.txt"));
            String line = bf.readLine();
            String [] lineDetails = line.split(",");
            int numberOfDocuments = Integer.valueOf(lineDetails[0]);
            int averageLength = Integer.valueOf(lineDetails[1]);
            bf.close();
            calculateBM25(termsForQueries,numberOfDocuments,averageLength);
            calculateInnerProduct();
            List<Map.Entry<String, Double>> list = new ArrayList<>(ranksPerDocument.entrySet());
            Comparator <Map.Entry<String,Double>> comparator = new Comparator<Map.Entry<String, Double>>() {
                @Override
                public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                    return o2.getValue().compareTo(o1.getValue());
                }
            };
            Collections.sort(list,comparator);
            Map<String, Double> rankFinal = new LinkedHashMap<>();
            for (Map.Entry<String, Double> entry : list) {
                rankFinal.put(entry.getKey(), entry.getValue());
            }

            int counter = 0;
            Vector<String> result = new Vector<>();
            // get the 50 documents with the highest rank
            for(Map.Entry<String,Double> entry: rankFinal.entrySet()){
                if (counter == 50)
                    break;
                result.add(entry.getKey());
                counter++;
            }
            // update search map in the rank result
            Searcher.result.put(Integer.valueOf(queryId),result);
            System.out.println(queryId + " done");
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    /**
     * This method retrieve all the documents which at least one the the cities inside the hashSet cities appears in them
     * @param cities
     */
    private void getCitiesDocuments(HashSet<String> cities){
        this.cityDocuments = new HashSet<>();
        try {
            HashSet<Integer> pointers = new HashSet<>();
            BufferedReader bf = new BufferedReader(new FileReader(Engine.pathToSaveIndex + "\\cityDictionary.txt"));
            String line = bf.readLine();
            while (line != null){
                String city = line.substring(0,line.indexOf(","));
                if (cities.contains(city)) {
                    // get all the pointers of the cities which exist in @cities
                    int indexOf = line.indexOf("|");
                    String ptr = line.substring(indexOf + 1);
                    int integerPtr = Integer.valueOf(ptr);
                    pointers.add(integerPtr);
                }
                line = bf.readLine();
            }
            bf.close();
            bf = new BufferedReader(new FileReader(Engine.pathToSaveIndex + "\\cityPosting.txt"));
            int lineNumber = 1;
            line = bf.readLine();
            while (pointers.size() > 0){
                if (pointers.contains(lineNumber)){
                    // get the doc id from the city posting file
                    while (line.length() > 0){
                        cityDocuments.add(StringUtils.substringBetween(line,"<",","));
                        line = line.substring(line.indexOf(">") + 1);
                    }
                    pointers.remove(lineNumber);
                }
                line = bf.readLine();
                lineNumber++;
            }
        } catch (Exception e) { }
    }


    /**
     * This method retrieve all the documents which contains at least one of the terms inside @param termsForQueries
     * @param termsForQueries - all the terms in the query, saves is the format <term,tf>
     */
    private void getTermsPosting(HashMap<String,Integer> termsForQueries){
        try {
            for (Map.Entry<String, Integer> entry : termsForQueries.entrySet()) {
                int ptr = 0;
                String key = entry.getKey();
                if(Engine.dictionary.containsKey(entry.getKey().toUpperCase())) {
                    ptr = Engine.dictionary.get(entry.getKey().toUpperCase()).getPtr();
                    key = key.toUpperCase();
                }
                else if (Engine.dictionary.containsKey(entry.getKey().toLowerCase())) {
                    ptr = Engine.dictionary.get(entry.getKey().toLowerCase()).getPtr();
                    key = key.toLowerCase();
                }
                else continue;
                char c = Character.toUpperCase(entry.getKey().charAt(0));
                BufferedReader bf = new BufferedReader(new FileReader(Engine.pathToSaveIndex + "\\posting" + c + ".txt"));
                String line = bf.readLine();
                int lineNumber = 1;
                while(true){
                    if (lineNumber == ptr){
                        while (line.length() > 0){
                            // get the doc id
                            String docId = StringUtils.substringBetween(line,"<",",");
                            if ((cityDocuments == null || cityDocuments.contains(docId)) /*&& !notRelevantDocments.contains(docId)*/){
                                // get the tf of the term in the document
                                String tf = StringUtils.substringBetween(line,",",">");
                                int numberTF = Integer.valueOf(tf);
                                TermRankDetails termRankDetails = new TermRankDetails(key,numberTF);
                                if (!posting.containsKey(docId)){
                                    ArrayList<TermRankDetails> list = new ArrayList<>();
                                    list.add(termRankDetails);
                                    posting.put(docId,list);
                                }
                                else
                                    posting.get(docId).add(termRankDetails);
                            }
                            line = line.substring(line.indexOf(">") + 1).trim();
                        }
                        bf.close();
                        break;
                    }
                    line = bf.readLine();
                    lineNumber++;
                }

            }
        }
        catch (IOException e) { }
    }

    /**
     * This method calculate for each document in @posting the bm25 rank for the query
     * @param termsForQueries - all the terms in the query, saves is the format <term,tf>
     * @param numberOfDocuments - number of documents in the corpus
     * @param averageLength - the average length of all documnets in the corpus
     */
    private void calculateBM25(HashMap<String, Integer> termsForQueries, double numberOfDocuments, double averageLength) {
        double rank = 0;
        double k = 1.2;
        double b = 0.75;
        // for each document in posting
        for (Map.Entry<String,ArrayList<TermRankDetails>> entry:posting.entrySet()){
            double docLength = Engine.mapOfDocs.get(entry.getKey()).getLength();
            // for each term in the document
            for (TermRankDetails termRankDetails:entry.getValue()) {
                double partA = 0.0;
                // get the number of appearance of the term in the query
                if (termsForQueries.containsKey(termRankDetails.getTerm().toLowerCase()))
                    partA = termsForQueries.get(termRankDetails.getTerm().toLowerCase());
                else
                    partA = termsForQueries.get(termRankDetails.getTerm().toUpperCase());
                // get the number of appearance the term has in the document
                double partB = ((k+1) * termRankDetails.getTF());
                double partC = termRankDetails.getTF()+ k * (1-b + b *(docLength/averageLength));
                double partD = Math.log((numberOfDocuments + 1)/ (double) Engine.dictionary.get(termRankDetails.getTerm()).getDocumentFrequency());
                // calculate the bm25 formula
                rank = rank + (partA * (partB/partC) * partD);
            }
            ranksPerDocument.put(entry.getKey(),rank);
            rank = 0.0;
        }
    }

    /**
     * This method calculate for each document in @posting the inner product rank for the query
     * and adding it to the bm25 rank calculated before
     */
    private void calculateInnerProduct() {
        double rank = 0.0;
        for (Map.Entry<String, ArrayList<TermRankDetails>> entry : posting.entrySet()) {
            double docLength = Engine.mapOfDocs.get(entry.getKey()).getLength();
            // for each term in the document
            for (TermRankDetails termRankDetails : entry.getValue()) {
                double termWeight = termRankDetails.getTF()/docLength;
                rank = rank + termWeight;
            }
            // get the previous rank in the ranksPerDocument hash map
            double previousRank = ranksPerDocument.get(entry.getKey());
            ranksPerDocument.put(entry.getKey(),rank + previousRank);
            rank = 0.0;
        }
    }
}
