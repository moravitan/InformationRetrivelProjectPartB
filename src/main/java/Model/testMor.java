package Model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class testMor {

    public static void main(String[] args) {
        testEngine();

    }

    public static void testEngine(){
        String pathToParse = "C:\\Users\\איתן אביטן\\Downloads\\לימודים\\אחזור מידע\\פרויקט מנוע חיפוש\\corpus\\corpus";
        String pathToSaveIndex = "C:\\Users\\איתן אביטן\\Downloads\\לימודים\\אחזור מידע\\פרויקט מנוע חיפוש\\indexer";
        Engine engine = new Engine();
        engine.setParameters(pathToParse,pathToSaveIndex,false);
        try {
            engine.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static boolean  isSemantic = true;

    private static String treatSemantic(String query) {
        if(isSemantic){
            String queryWithPluses=  queryWithPluses(query);
            try{
                String urlString = "https://api.datamuse.com/words?ml=" + queryWithPluses;
                URL url = new URL(urlString);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                InputStreamReader iSR =new InputStreamReader((con.getInputStream()));
                BufferedReader bufferedReader =new BufferedReader(iSR);
                String inputLine;
                StringBuffer similiarSemanticWords= new StringBuffer();
                while((inputLine=bufferedReader.readLine())!=null){
                    int index =0;
                    String [] words = inputLine.substring(2).split("\\{");
                    for(String word: words){
                        if(index==15) break;
                        String [] wordStruct=  word.split(",")[0].split(":");
                        similiarSemanticWords.append(wordStruct[1].substring(1).replace('"',' '));
                        index++;
                    }
                }
                bufferedReader.close();
                iSR.close();
                return similiarSemanticWords.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        else return query;
        return query;
    }


    private static String queryWithPluses(String query) {
        String [] words=query.split(" ");
        String queryWithPluses="";
        for (int i = 0; i < words.length ; i++) {
            if(i==0)
                queryWithPluses=words[i];
            else{
                queryWithPluses+= ("+" + words[i]);
            }
        }
        return query;
    }
}
