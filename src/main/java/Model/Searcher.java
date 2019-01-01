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
    HashMap<String,Integer> notRelevant;
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
        ranker.rank(this.query,null, cities, "111");
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
            String narrContent = "";
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
                if (st.contains("<narr>")){
                    st= br.readLine();
                    narrContent = st;
                    st = br.readLine();
                    while(!st.contains("</top>")){
                        narrContent =  narrContent +" "+ st;
                        st = br.readLine();
                    }

                }

                if (!id.equals("") && !descContent.equals("")) {
                    if(isSemantic)
                       queryContent =  handleSemantic(queryContent);
                    queryContent = queryContent + " " + descContent;
                    parse.parsing(id, queryContent, false);
                    query = parse.getTermsMapPerDocument();
                    String parsedNarr = parseNarr(narrContent);
                    parse.parsing(id, parsedNarr, false);
                    notRelevant= parse.getTermsMapPerDocument();
                   // ranker.rank(query,notRelevant,cities, id);
                    id = "";
                    queryContent = "";
                    descContent = "";
                    narrContent = "";
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
            int indexOfMakaf = APIQuery.indexOf('-');
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


    private static String parseNarr(String narr){
        String ans = "";
        try {
            //if ________ not relevant.
            if (narr.contains("not relevant.")) {
                int notRelIndex = narr.indexOf("not relevant.");
                if (notRelIndex != -1) {
                    narr = narr.substring(0, notRelIndex + 12);
                    int dotIndex = narr.lastIndexOf(".");
                    if (dotIndex != -1)
                        return (narr.substring(dotIndex + 1, notRelIndex - 1).trim());
                    else {
                        return (narr.substring(0, notRelIndex - 1).trim());
                    }
                }
            }
            //if not relevant: _______
            if (narr.contains("not relevant:")) {
                int notRelIndex = narr.indexOf("not relevant:");
                narr = narr.substring(notRelIndex + 13).trim();
                //index of '('
                int openIndex = narr.indexOf('(');
                if (openIndex != -1) {
                    /// index of ')'
                    int closeIndex = narr.indexOf(')');
                    if (closeIndex != -1) {
                        String before = narr.substring(0, openIndex - 1);
                        String after = narr.substring(closeIndex + 1);
                        return before + " " + after;
                    }
                }

                int dotaimIndex = narr.lastIndexOf(":");
                return (narr.substring(dotaimIndex + 1));
            }

            //if ________ not relevant.
            if (narr.contains("non-relevant.")) {
                int nonRelIndex = narr.indexOf("non-relevant.");
                if (nonRelIndex != -1) {
                    narr = narr.substring(0, nonRelIndex + 12);
                    int dotIndex = narr.lastIndexOf(".");
                    if (dotIndex != -1)
                        return (narr.substring(dotIndex + 1, nonRelIndex - 1).trim());
                    else {
                        return (narr.substring(0, nonRelIndex - 1).trim());
                    }
                }
            }
            //if not relevant: _______
            if (narr.contains("non-relevant:")) {
                int nonRelIndex = narr.indexOf("non-relevant:");
                narr = narr.substring(nonRelIndex + 13).trim();
                //index of '('
                int openIndex = narr.indexOf('(');
                if (openIndex != -1) {
                    /// index of ')'
                    int closeIndex = narr.indexOf(')');
                    if (closeIndex != -1) {
                        String before = narr.substring(0, openIndex - 1);
                        String after = narr.substring(closeIndex + 1);
                        return before + " " + after;
                    }
                }

                int dotaimIndex = narr.lastIndexOf(":");
                return (narr.substring(dotaimIndex + 1));
            }

            //if ________ not relevant.
            if (narr.contains("not relevant,")) {
                int notRelIndex = narr.indexOf("not relevant,");
                if (notRelIndex != -1) {
                    narr = narr.substring(0, notRelIndex + 12);
                    int dotIndex = narr.lastIndexOf(".");
                    if (dotIndex != -1)
                        return (narr.substring(dotIndex + 1, notRelIndex - 1).trim());
                    else {
                        return (narr.substring(0, notRelIndex - 1).trim());
                    }
                }
            }
            //if not relevant ___________.
            if (narr.contains("non-relevant")) {
                int nonRelIndex = narr.indexOf("non-relevant");
                if ((narr.charAt(nonRelIndex + 12) == ',' || narr.charAt(nonRelIndex + 12) == '.')) {
                    return "";
                } else {
                    narr = narr.substring(nonRelIndex);
                    int dotIndex = narr.indexOf('.');
                    return narr.substring(nonRelIndex + 12, dotIndex);
                }
            }
            //if not relevant ___________.
            if (narr.contains("not relevant")) {
                int notRelIndex = narr.indexOf("not relevant");
                if ((narr.charAt(notRelIndex + 12) == ',' || narr.charAt(notRelIndex + 12) == '.')) {
                    return "";
                } else {
                    narr = narr.substring(notRelIndex);
                    int dotIndex = narr.indexOf('.');
                    return narr.substring(notRelIndex + 12, dotIndex);
                }
            }
        }catch (Exception e) { }
        return ans;
    }

}
