package Model;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

public class ReadFile{

    File folder;
    Parse parser;
    String pathToParse;
    public static HashMap<String,DocumentDetails> mapOfDocs = new HashMap<>();
    public static HashSet<String> cities = new HashSet<>();
    public static TreeSet<String> corpusLanguages = new TreeSet<>();
    public static HashSet<String> stopWords = new HashSet<>();


    /**
     * Constructor
     * @param pathToParse
     */
    public ReadFile(String pathToParse, Parse parser) {
        this.pathToParse = pathToParse;
        folder = new File(pathToParse);
        this.parser = parser;
    }

    /**
     * This method start the reading process of the given pathToParse
     * First read the entire corpus and find all the cities between the tags - <F P 104></F>
     * Then load the stop words to the hash set
     * and than start to read all the files in the path and moving in to the parse object
     */
    public void startReading () throws Exception {
        // get cities
        getTagsDetails(folder);
        // set stop words
        setStopWords();
        // read the entire corous
        listFilesForFolder(folder);
        // notify parser that all the files has been readed
        //parser.notifyDone();

    }

    /**
     *
     * @param folder
     */
    private void getTagsDetails(File folder) throws Exception {
        for (File fileEntry : folder.listFiles()) {
            if (fileEntry.getName().equals(pathToParse + "\\stop_words.txt"))
                continue;
            if (fileEntry.isDirectory()) {
                getTagsDetails(fileEntry);
            } else {
                Document document = null;
                document = Jsoup.parse(new String(Files.readAllBytes(fileEntry.toPath())));
                Elements elements = document.getElementsByTag("DOC");
                for (Element e : elements) {
                    String docText = e.toString();
                    // get doc city if exist
                    String cityName = StringUtils.substringBetween(docText, "<f p=\"104\">\n ", "</f> \n");
                    if (cityName != null) {
                        cityName = cityName.trim();
                        int indexOfSpace = cityName.indexOf(" ");
                        if (indexOfSpace != -1)
                            cityName = cityName.substring(0, indexOfSpace);
                        cities.add(cityName.toUpperCase());
                    }
                }

            }
        }
    }

    /**
     * This method load the stop words to hash set
     */
    private void setStopWords() throws IOException {
        File file = new File(pathToParse + "/stop_words.txt");
        BufferedReader bf = new BufferedReader(new FileReader(file));
        String line = bf.readLine();
        while (line != null) {
            stopWords.add(line);
            line = bf.readLine();
        }


    }
    /**
     * This method run recursively over the given folder
     * If the file founded is of type file, split the file to docs by tags <DOC> </DOC>
     * If the file founded is of type folder, call the function recursively with file.
     * @param folder
     */
    private void listFilesForFolder(File folder) {
        for (File fileEntry : folder.listFiles()) {
            // load stop words into hashSet
            if (fileEntry.getName().equals(pathToParse + "/stop_words.txt")) continue;
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                Document document = null;
                try {
                    document = Jsoup.parse(new String (Files.readAllBytes(fileEntry.toPath())));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Elements elements = document.getElementsByTag("DOC");
                for (Element e: elements) {
                    String docText = e.toString();
                    // get doc id
                    String docId = e.getElementsByTag("DOCNO").text();
                    // get doc date
                    String docDate = e.getElementsByTag("DATE1").text();
                    if (docDate != null) {
                        docDate = docDate.trim();
                        docDate = docDate.replaceAll("\n", "");
                    }
                    // get doc language
                    String language = StringUtils.substringBetween(docText, "<f p=\"105\">\n ","</f>");
                    if (language != null){
                        language = language.trim();
                        int indexOfSpace = language.indexOf(" ");
                        if (indexOfSpace != -1)
                            language = language.substring(0,indexOfSpace);
                        int indexOfComma = language.indexOf(",");
                        if (indexOfComma != -1)
                            language = language.substring(0,indexOfComma);
                        if (StringUtils.isAlpha(language))
                            corpusLanguages.add(language);
                    }
                    mapOfDocs.put(docId,new DocumentDetails(fileEntry.getName(),docDate,language));
                    parser.parsing(docId,docText,true);
                }
            }
        }
    }

    /**
     *
     * @return the field parser
     */
    public Parse getParser() {
        return parser;
    }

    public TreeSet<String> getCorpusLanguages() {
        if(corpusLanguages!=null)
            return corpusLanguages;
        return null;
    }
}
