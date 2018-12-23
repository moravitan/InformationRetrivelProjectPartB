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
     * @param parse
     */
    public Searcher(Parse parse) {
        this.parse = parse;
        this.ranker = new Ranker();
    }

    /**
     *
     * @param query
     * @param cities
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
     *
     * @param file
     * @param cities
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
                if (st.contains("<num> Number:")) {
                    id = st.substring(14, 17);
                }
                if (st.contains("<title> ")){
                    queryContent = st.substring(8).replaceAll("\\s+$","");
                }
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
                    if(isSemantic)
                       queryContent =  handleSemantic(queryContent);
                    queryContent = queryContent + " " + descContent;
                    parse.parsing(id, queryContent, false);
                    query = parse.getTermsMapPerDocument();
                    ranker.rank(query, cities, id);
                    id = "";
                    queryContent = "";
                    descContent = "";
                }

            }
        } catch (IOException e){
            System.out.println(e.getMessage());
        }
        return result;
    }


    private static String handleSemantic(String query){
        String [] queryWords = query.split(" ");
        HashSet<String> APIwords = new HashSet<>();
        StringBuilder querySB = new StringBuilder(query + " ");
        for (int i = 0; i <queryWords.length ; i++) {
            String APIQuery = queryWords[i];
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
                        if(wordCounter==5)
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
                            //querySB.append(StringUtils.substringBetween(line,"\"word\":\"","\",\"score\"")+ " ");
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
