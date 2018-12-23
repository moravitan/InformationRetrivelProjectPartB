package Model;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;

public class Searcher {

    Ranker ranker;
    Parse parse;
    HashMap<String,Integer> query;
    HashSet<String> cities;
    static TreeMap<Integer, Vector<String>> result;

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
            while ((st = br.readLine()) != null ){
                if (st.contains("<num> Number:")) {
                    id = st.substring(14, 17);
                }
                if (st.contains("<title> ")){
                    queryContent = st.substring(8).replaceAll("\\s+$","");
                }
                if (!id.equals("") && !queryContent.equals("")) {
                    if(isSemantic)
                       queryContent =  handleSemantic(queryContent);
                    parse.parsing(id, queryContent, false);
                    query = parse.getTermsMapPerDocument();
                    ranker.rank(query, cities, id);
                    id = "";
                    queryContent = "";
                }

            }
        } catch (IOException e){
            System.out.println(e.getMessage());
        }
        return result;
    }


    private String handleSemantic(String query){
        String APIQuery = query.replaceAll("\\s","+");
        StringBuilder querySB = new StringBuilder(query);
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
                    if(wordCounter==20)
                        break;
                    querySB.append(StringUtils.substringBetween(line,"\"word\":\"","\",\"score\""));
                    wordCounter++;
                    int index = line.indexOf('}');
                    line = line.substring(index+  1);
                }
                line = br.readLine();
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        querySB.deleteCharAt(querySB.length()-1);
        return querySB.toString();
    }

}
