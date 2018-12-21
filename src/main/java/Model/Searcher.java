package Model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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

    public Searcher(Parse parse) {
        this.parse = parse;
        this.ranker = new Ranker();
    }

    /**
     *
     * @param query
     * @param cities
     */
    public TreeMap<Integer, Vector<String>> processQuery(String query, HashSet<String> cities){
        this.cities=cities;
       result = new TreeMap<Integer, Vector<String>>();
       this.query = new HashMap<>();
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
    public TreeMap<Integer, Vector<String>> processQuery(File file, HashSet<String> cities){
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



}
