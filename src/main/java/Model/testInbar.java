package Model;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

public class testInbar {

    public static void main(String[] args) {
        // System.out.println("something");
        // System.out.println("git is shit");
        String str = "<num> Number: 352 ";
        String str2 = "<num> Number: 367";
        // System.out.println(str2.substring(14,17));
        String ans  = handleSemantic("Inbar");
        System.out.println(ans);
    }



    private static String handleSemantic(String query){
        String APIQuery = query.replaceAll("\\s","+");
        StringBuilder querySB = new StringBuilder(query + " ");
        try{
            String urlContent = "https://api.datamuse.com/words?ml=" + APIQuery;
            URL url = new URL(urlContent);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            BufferedReader br =new BufferedReader(new InputStreamReader((con.getInputStream())));
            String line = br.readLine();
            while(line!=null){
                int wordCounter =0;
                //////////////////////
                while(line.length()>0){
                    if(wordCounter==10)
                        break;
                    querySB.append(StringUtils.substringBetween(line,"\"word\":\"","\",\"score\"")+ " ");
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
