package Model;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class testInbar {

    public static void main(String[] args) throws IOException {
        // System.out.println("something");
        // System.out.println("git is shit");
        //String str = "<num> Number: 352 ";
        //String str2 = "<num> Number: 367";
        // System.out.println(str2.substring(14,17));
        //String ans  = handleSemantic("Inbar");
       // System.out.println(ans);
        processQuery(new File("C:\\Users\\Inbar\\Documents\\סמסטר ה'\\איחזור מידע\\פרויקט חלק ב'\\queries.txt"));
        String text = "According to Lower House Foreign Relations Committee \n" +
                "chairman Florencio Acenolaza, the existence of oil around the \n" +
                "Malvinas has not been proven and if any were to be found, the \n" +
                "amount would be so small and the difficulties of exploitation so \n" +
                "extraordinary that it would not be commercially viable. \n" +
                "  The existence of oil in the Malvinas has been a source of \n" +
                "conflict between Argentina and Britain. The UK (as published by \n" +
                "the Herald) recently announced a major oil industry impact study \n" +
                "but the Argentine deputy denounces the existence of oil as \"a \n" +
                "lie and a political maneuver to manipulate Argentina when \n" +
                "discussing fisheries or sovereignty. This (manipulation) is not \n" +
                "only encouraged by the Foreign Office but also by people in this \n" +
                "country who wish to change current Argentine policy.\" \n" +
                "  Acenolaza believes that the existing oil basins are all \n" +
                "under \n" +
                "Argentine territorial jurisdiction and exploitation conditions \n" +
                "are the most difficult in the world -- so difficult that oil \n" +
                "prices would be as high as $50 per barrel when world prices are \n" +
                "under 15. \n" +
                "  \"The existence of oil is an invention of British authorities \n" +
                "as a strategy to gain a better position when discussing \n" +
                "fisheries or sovereignty. It is untrue and a blatant lie that \n" +
                "there is oil in the Malvinas. The UK is manipulating \n" +
                "information aimed at confusing public opinion over the real \n" +
                "possibility of finding oil in the islands.\" \n" +
                "  Deputy Acenolaza, himself a geologist, underlines that \n" +
                "former \n" +
                "state oil company YPF had prospected in the Malvinas and the \n" +
                "existence of oil could not be proven. On this subject, YPF \n" +
                "spokesman Juan Carlos Ferrari declined comment, explaining that \n" +
                "the company \"does not have or give any information regarding oil \n" +
                "in the Malvinas.\" He also added that in 1933 a member of the \n" +
                "Foreign Office had said that there was no oil in Saudi Arabia. \n" +
                "  \"Geologists know that geological conditions to certify the \n" +
                "existence of oil-fields in the Malvinas do not exist. Both \n" +
                "islands are made of Paleozoic rock, which lack the natural raw \n" +
                "material to generate hydrocarbons. The stratographic base, made \n" +
                "of crystalline rocks, has a natural incapacity to hold \n" +
                "hydrocarbons. It is equally impossible with the quartz or sandy \n" +
                "rocks which make up the greater part of the territory.\" \n" +
                "  The deputy also underlined that in the South Atlantic and \n" +
                "the \n" +
                "British exclusion zone there are two oil basins dating back to \n" +
                "Jurassic times, which correspond to the geological formation of \n" +
                "the Patagonian oil basins. \n" +
                "  He recognizes the existence of three oil basins with \n" +
                "insignificant amounts of oil. The \"Cuenca de Malvinas\" basin to \n" +
                "the southwest of the islands was surveyed by a concession which \n" +
                "discovered oil and gas only in two wells (Ciclon XI and Salmon \n" +
                "XII) but in such small quantities that it did not justify \n" +
                "exploitation. The geological information of the \"Banco Burwood\" \n" +
                "basin to the Northwest is reportedly so limited that no \n" +
                "guarantee for the existence of hydrocarbons can be given. In \n" +
                "the third basin -- \"Cuenca de San Julian\" -- the sedimentary \n" +
                "depths hardly reach 2,500 meters and the existence of \n" +
                "\"generating rocks\" for oil has not been proven. \n" +
                "  According to the deputy, the barometric and weather \n" +
                "conditions of the three basins together with the low price of \n" +
                "oil make the possibilities of finding oil in the Malvinas \n" +
                "irrelevant.";
      //  text = text.replaceAll(" ", "%20");
       // FileWriter fr = new FileWriter(new File("C:\\Users\\Inbar\\Documents\\סמסטר ה'\\איחזור מידע\\פרויקט חלק א'\\text.txt"));
       // fr.write(text);
       // fr.flush();
       // fr.close();
    }

    public static void processQuery(File file){
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String st;
            String id = "";
            String queryContent = "";
            String descContent = "";
            String narrContent = "";
            while ((st = br.readLine()) != null) {
                if (st.contains("<num> Number:")) {
                    id = st.substring(14, 17);
                }
                if (st.contains("<title> ")) {
                    queryContent = st.substring(8).replaceAll("\\s+$", "");
                }
                if (st.contains("<desc> ")) {
                    st = br.readLine();
                    descContent = st;
                    st = br.readLine();
                    while (!st.contains("<narr>")) {
                        descContent = descContent + " " + st;
                        st = br.readLine();
                    }
                    descContent = descContent.replaceAll("\\s+$", "");
                }
                if (st.contains("<narr>")) {
                    st = br.readLine();
                    narrContent = st;
                    st = br.readLine();
                    while (!st.contains("</top>")) {
                        narrContent = narrContent + " " + st;
                        st = br.readLine();
                    }
                    System.out.println(id +" "+parseNarr(narrContent) +'\n');

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        }

        private static String parseNarr(String narr){
            String ans = "";
            //if ________ not relevant.
            if(narr.contains("not relevant.") ){
                int notRelIndex = narr.indexOf("not relevant.");
                if(notRelIndex!=-1){
                    narr= narr.substring(0,notRelIndex+12);
                    int dotIndex = narr.lastIndexOf(".");
                    if(dotIndex!=-1)
                     return(narr.substring(dotIndex+1, notRelIndex-1).trim());
                    else{
                        return(narr.substring(0, notRelIndex-1).trim());
                    }
                }
            }
            //if not relevant: _______
            if(narr.contains("not relevant:")){
                int notRelIndex = narr.indexOf("not relevant:");
                narr= narr.substring(notRelIndex+13).trim();
                //index of '('
               int openIndex = narr.indexOf('(');
               if(openIndex!=-1){
                   /// index of ')'
                   int closeIndex= narr.indexOf(')');
                   if(closeIndex!=-1){
                     String before = narr.substring(0,openIndex-1);
                     String after = narr.substring(closeIndex+1);
                     return before+ " " + after;
                   }
               }

                int dotaimIndex = narr.lastIndexOf(":");
                return(narr.substring(dotaimIndex+1));
            }

            //if ________ not relevant.
            if(narr.contains("non-relevant.") ){
                int nonRelIndex = narr.indexOf("non-relevant.");
                if(nonRelIndex!=-1){
                    narr= narr.substring(0,nonRelIndex+12);
                    int dotIndex = narr.lastIndexOf(".");
                    if(dotIndex!=-1)
                        return(narr.substring(dotIndex+1, nonRelIndex-1).trim());
                    else{
                        return(narr.substring(0, nonRelIndex-1).trim());
                    }
                }
            }
            //if not relevant: _______
            if(narr.contains("non-relevant:")){
                int nonRelIndex = narr.indexOf("non-relevant:");
                narr= narr.substring(nonRelIndex+13).trim();
                //index of '('
                int openIndex = narr.indexOf('(');
                if(openIndex!=-1){
                    /// index of ')'
                    int closeIndex= narr.indexOf(')');
                    if(closeIndex!=-1){
                        String before = narr.substring(0,openIndex-1);
                        String after = narr.substring(closeIndex+1);
                        return before+ " " + after;
                    }
                }

                int dotaimIndex = narr.lastIndexOf(":");
                return(narr.substring(dotaimIndex+1));
            }

            //if ________ not relevant.
            if(narr.contains("not relevant,") ){
                int notRelIndex = narr.indexOf("not relevant,");
                if(notRelIndex!=-1){
                    narr= narr.substring(0,notRelIndex+12);
                    int dotIndex = narr.lastIndexOf(".");
                    if(dotIndex!=-1)
                        return(narr.substring(dotIndex+1, notRelIndex-1).trim());
                    else{
                        return(narr.substring(0, notRelIndex-1).trim());
                    }
                }
            }
            //if not relevant ___________.
            if(narr.contains("non-relevant")){
                int nonRelIndex = narr.indexOf("non-relevant");
                if((narr.charAt(nonRelIndex+12)==',' || narr.charAt(nonRelIndex+12)=='.')){
                    return "";
                }
                else{
                    narr = narr.substring(nonRelIndex);
                    int dotIndex = narr.indexOf('.');
                    return narr.substring(nonRelIndex+12 , dotIndex);
                }
            }
            //if not relevant ___________.
            if(narr.contains("not relevant")){
                int notRelIndex = narr.indexOf("not relevant");
                if((narr.charAt(notRelIndex+12)==',' || narr.charAt(notRelIndex+12)=='.')){
                    return "";
                }
                else{
                    narr = narr.substring(notRelIndex);
                    int dotIndex = narr.indexOf('.');
                    return narr.substring(notRelIndex+12 , dotIndex);
                }
            }
            return ans;
        }

        private static String handleSemantic (String query){
                String APIQuery = query.replaceAll("\\s", "+");
                StringBuilder querySB = new StringBuilder(query + " ");
                try {
                    String urlContent = "https://api.datamuse.com/words?ml=" + APIQuery;
                    URL url = new URL(urlContent);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    BufferedReader br = new BufferedReader(new InputStreamReader((con.getInputStream())));
                    String line = br.readLine();
                    while (line != null) {
                        int wordCounter = 0;
                        //////////////////////
                        while (line.length() > 0) {
                            if (wordCounter == 10)
                                break;
                            querySB.append(StringUtils.substringBetween(line, "\"word\":\"", "\",\"score\"") + " ");
                            wordCounter++;
                            int index = line.indexOf('}');
                            line = line.substring(index + 1);
                        }
                        line = br.readLine();
                    }
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                querySB.deleteCharAt(querySB.length() - 1);
                return querySB.toString();
            }





}
