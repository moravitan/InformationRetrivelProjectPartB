package Model;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.Vector;

public class Searcher {

    Ranker ranker;
    Parse parse;
    HashMap<String,Integer> query;
    HashSet<String> cities;
    static TreeMap<Integer, Vector<String>> result;
    boolean isSemantic;

    /**
     * constructor
     * @param parse - parser of the program
     */
    public Searcher(Parse parse) {
        this.parse = parse;
        this.ranker = new Ranker();
    }

    /**
     *  return a Tree Map of <queryId, vector of relevant documents>
     * @param query - given single query
     * @param cities - set of all cities in corpus under <F P=104>  </F> tag
     */
    public TreeMap<Integer, Vector<String>> processQuery(String query, HashSet<String> cities, boolean isSemantic){
        this.cities=cities;
        result = new TreeMap<Integer, Vector<String>>();
        this.query = new HashMap<>();
        if(isSemantic)
          query = handleSemantic(query);
        parse.parsing("111",query,false);
        this.query = parse.getTermsMapPerDocument();
        ranker.rank(this.query, cities, "111");
        this.isSemantic = isSemantic;
        return result;
    }

    /**
     *  return a Tree Map of <queryId, vector of relevant documents>
     * @param file - file of queries
     * @param cities - set of all cities in corpus under <F P=104>  </F> tag
     */
    public TreeMap<Integer, Vector<String>> processQuery(File file, HashSet<String> cities,boolean isSemantic){
        this.cities=cities;
        result = new TreeMap<Integer, Vector<String>>();
        query = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String st;
            String id="";
            String queryContent="";
            String descContent = "";
            while ((st = br.readLine()) != null ){
                //catch query id
                if (st.contains("<num> Number:")) {
                    id = st.substring(14, 17);
                }
                //catch query content
                if (st.contains("<title> ")){
                    queryContent = st.substring(8).replaceAll("\\s+$","");
                }
                //catch query description
                if (st.contains("<desc> ")){
                    st= br.readLine();
                    descContent = st;
                    st = br.readLine();
                    while(!st.contains("<narr>")){
                        descContent =  descContent +" "+ st;
                        st = br.readLine();
                    }
                    descContent = descContent.replaceAll("\\s+$","");
                }

                if (!id.equals("") && !descContent.equals("")) {
                   //doing semantic operation on original query terms
                    if(isSemantic)
                       queryContent = handleSemantic(queryContent);
                    //parse original query terms, semantic terms, description terms
                    queryContent = queryContent + " " + descContent;
                    parse.parsing(id, queryContent, false);
                    //get for each query vector of docs from posting
                    query = parse.getTermsMapPerDocument();
                    //rank the docs from returned vector
                    ranker.rank(query,cities, id);
                    id = "";
                    queryContent = "";
                    descContent = "";
                }

            }
        } catch (Exception e){

        }
        return result;
    }

    /**
     * return string of original query terms + 2-3 semantic terms for each term in query
     * @param query - given query
     * @return
     */
    private static String handleSemantic(String query){
        String [] queryWords = query.split(" ");
        HashSet<String> APIwords = new HashSet<>();
        StringBuilder querySB = new StringBuilder(query + " ");
        for (int i = 0; i <queryWords.length ; i++) {
            String APIQuery = queryWords[i];
            int indexOfMakaf = APIQuery.indexOf('-');
            //if there is terms in query type of: term.1-term.2-..-term.n does semantic on the two words together
            if(indexOfMakaf!=-1)
                APIQuery = APIQuery.replaceAll("-", "+");
            try{
                String urlContent = "https://api.datamuse.com/words?ml=" + APIQuery;
                URL url = new URL(urlContent);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                BufferedReader br =new BufferedReader(new InputStreamReader((con.getInputStream())));
                String line = br.readLine();
                while(line!=null){
                    int wordCounter =0;
                    while(line.length()>0){
                        //takes two-tree semantic terms for each term from query
                        if(wordCounter >= 2)
                            break;
                        String APIterm = StringUtils.substringBetween(line,"\"word\":\"","\",\"score\"");
                        int indexOfWhitespace = APIterm.indexOf(' ');
                        while (indexOfWhitespace!=-1){
                            if (!APIwords.contains(APIterm.substring(0,indexOfWhitespace))) {
                                APIwords.add(APIterm.substring(0, indexOfWhitespace));
                                wordCounter++;
                            }
                            APIterm = APIterm.substring(indexOfWhitespace+1);
                            indexOfWhitespace = APIterm.indexOf(' ');
                        }
                        if (!APIwords.contains(APIterm)) {
                            APIwords.add(APIterm);
                            wordCounter++;
                        }
                        int index = line.indexOf('}');
                        line = line.substring(index+  1);
                    }
                    for (String str: APIwords) {
                        querySB.append(str+ " ");
                    }
                    APIwords.clear();
                    line = br.readLine();
                }
                br.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (querySB.charAt(querySB.length()-1)== ' ')
            querySB.deleteCharAt(querySB.length()-1);
        return querySB.toString();
    }

}
