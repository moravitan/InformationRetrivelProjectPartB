package Model;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Ranker {

    private HashSet<String> cityDocuments;
    private HashMap<String, ArrayList<TermRankDetails>> posting; // <docId, term,tf>
    private TreeMap<String,Double> ranksPerDocument; // <docId,rank>




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
            // sort rank result in desanding  order
            //HashMap<String,Double> rankFinal = new HashMap<>(ranksPerDocument);
            //rankFinal.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            //        .forEachOrdered(x -> ranksPerDocument.put(x.getKey(), x.getValue()));
            HashMap<String,Double> rankFinal = ranksPerDocument.entrySet()
                    .stream()
                    .sorted((Map.Entry.<String, Double>comparingByValue().reversed()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
            int counter = 0;
            Vector result = new Vector();
            // get the 50 documents with the highest rank
            for(Map.Entry<String,Double> entry: rankFinal.entrySet()){
                if (counter == 50)
                    break;
                result.add(entry.getKey());
                counter++;
            }
            // update search map in the rank result
            Searcher.result.put(Integer.valueOf(queryId),result);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    /**
     *
     * @param cities
     */
    private void getCitiesDocuments(HashSet<String> cities){
        this.cityDocuments = new HashSet<>();
        try {
            HashSet<Integer> pointers = new HashSet<>();
            BufferedReader bf = new BufferedReader(new FileReader(Engine.pathToSaveIndex + "\\cityDictionary"));
            String line = bf.readLine();
            while (line != null){
                // get all the pointers of the cities which exist in @cities
                int indexOf = line.indexOf("|");
                String ptr = line.substring(indexOf + 1);
                int integerPtr = Integer.valueOf(ptr);
                pointers.add(integerPtr);
                line = bf.readLine();
            }
            bf.close();
            bf = new BufferedReader(new FileReader(Engine.pathToSaveIndex + "\\cityPosting"));
            int lineNumber = 0;
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param termsForQueries
     */
    private void getTermsPosting(HashMap<String,Integer> termsForQueries){
        try {
            for (Map.Entry<String, Integer> entry : termsForQueries.entrySet()) {
                int ptr = Engine.dictionary.get(entry.getKey()).getPtr();
                char c = Character.toUpperCase(entry.getKey().charAt(0));
                BufferedReader bf = new BufferedReader(new FileReader(Engine.pathToSaveIndex + "\\posting" + c + ".txt"));
                String line = bf.readLine();
                int lineNumber = 0;
                while(true){
                    if (lineNumber == ptr){
                        while (line.length() > 0){
                            // get the doc id
                            String docId = StringUtils.substringBetween(line,"<",",");
                            if (cityDocuments == null || cityDocuments.contains(docId)){
                                // get the tf of the term in the document
                                String tf = StringUtils.substringBetween(line,",",">");
                                int numberTF = Integer.valueOf(tf);
                                TermRankDetails termRankDetails = new TermRankDetails(entry.getKey(),numberTF);
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
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void calculateBM25(HashMap<String, Integer> termsForQueries, double numberOfDocumets, double averageLength) {
        double rank = 0;
        double k = 2;
        double b = 0.75;
        // for each document in posting
        for (Map.Entry<String,ArrayList<TermRankDetails>> entry:posting.entrySet()){
            try {
                BufferedReader bf = new BufferedReader(new FileReader(Engine.pathToSaveIndex + "\\documentsDetails.txt"));
                double docLength = 0;
                String line = bf.readLine();
                while (line != null){
                    String docId = line.substring(0,line.indexOf(","));
                    line = line.substring(line.indexOf(","));
                    if (docId.equals(entry.getKey())){
                        docLength = Integer.valueOf(StringUtils.substringBetween(line,",",","));
                        break;
                    }
                    line = bf.readLine();
                }
                // for each term in the document
                for (TermRankDetails termRankDetails:entry.getValue()) {
                    double partA = termsForQueries.get(termRankDetails.getTerm());
                    double partB = ((k+1) * termRankDetails.getTF());
                    double partC = termRankDetails.getTF()+ k * (1-b + b *(docLength/averageLength));
                    double partD = Math.log((numberOfDocumets + 1)/ (double) Engine.dictionary.get(termRankDetails.getTerm()).getDocumentFrequency());
                    rank = rank + (partA * (partB/partC) * partD);
                }
                ranksPerDocument.put(entry.getKey(),rank);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    public static <K, V extends Comparable<? super V>> HashMap<String, Double> sortByValue(Map<String, Double> map) {
        List<Map.Entry<String, Double>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());

        HashMap<String, Double> result = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
