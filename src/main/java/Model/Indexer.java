package Model;


import com.google.code.externalsorting.ExternalSort;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Indexer {

    public static HashMap<String,TermDetails> dictionary = new HashMap<>(); // <Term, df, totalTF, ptr>
    HashMap<String, ArrayList<PostingDetails>> posting = new HashMap<>(); // <Term, doc_id,TF>
    HashMap <String,CityDetails> citiesDictionary = new HashMap<>(); // <CityName, countryName, currency, populationSize>
    HashMap <String, StringBuilder> citiesPosting  = new HashMap<>(); // <CityName, doc_id, positionListToString>
    // size of posting map
    int numDocInPosting;
    // index for number of posting file has been created
    int numOfPosting;
    File folder;
    Vector<File> numbersPosting;
    Vector<File> upperPosting;
    Vector<File> lowerPosting;
    Vector<File> cityPosting;
    ThreadPoolExecutor threadPoolExecutor;
    // path to save all the files created in this class
    int numberOfTerms;
    String pathToSaveIndex;
    // total length of all files
    long totalLength;

    /**
     *
     * @param pathToSaveIndex - path to save all the files created in the index process
     * @param isStemming
     */
    public Indexer(String pathToSaveIndex, boolean isStemming) {
        if (isStemming) {
            folder = new File(pathToSaveIndex + "\\stemmingPosting");
        }
        else{
            folder = new File(pathToSaveIndex + "\\posting");
        }
        folder.mkdir();
        this.pathToSaveIndex = folder.toString();
        this.numbersPosting = new Vector<>();
        this.upperPosting = new Vector<>();
        this.lowerPosting = new Vector<>();
        this.cityPosting = new Vector<>();
        this.numOfPosting = 0;
        int threadPoolSize = Runtime.getRuntime().availableProcessors() * 2;
        this.threadPoolExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        threadPoolExecutor.setCorePoolSize(threadPoolSize);
    }

    /**
     *
     * @param docId
     * @param termsMapPerDocument
     * @param citiesMap
     * set all the dictionaries and posting with the value receiving from the Parse class
     */
    public void setAll(String docId,HashMap <String, Integer> termsMapPerDocument,HashMap<String, ArrayList<Integer>> citiesMap){
        numDocInPosting++;
        setDictionaryAndPosting(docId, termsMapPerDocument);
        setCitiesDictionaryAndPosting(docId,citiesMap);
        if (numDocInPosting == 2500){
            // write to disk
            writeDataToDisk();
            posting = new HashMap<>();
            citiesPosting = new HashMap<>();
            numDocInPosting = 0;
        }
    }

    /**
     * This method copy the value of the given maps to the dictionary and posting field
     * @param docId
     * @param termsMapPerDocument
     */
    public void setDictionaryAndPosting(String docId, HashMap <String, Integer> termsMapPerDocument) {
        for (Map.Entry<String, Integer> entry : termsMapPerDocument.entrySet()) {
            // if term doesn't exist in dictionary
            if (!dictionary.containsKey(entry.getKey())) {
                // save the term as key and tf and df as value
                dictionary.put(entry.getKey(), new TermDetails(entry.getValue(), 1));

            }
            // if the term exist
            else {
                // add to df
                dictionary.get(entry.getKey()).addDocumentFrequency();
                // add total tf
                dictionary.get(entry.getKey()).setTotalTF(entry.getValue());
            }
            // if the term doesn't exist in posting file
            PostingDetails postingDetails = new PostingDetails(docId, entry.getValue());
            if (!posting.containsKey(entry.getKey())) {
                // doc_id,TF
                ArrayList<PostingDetails> list = new ArrayList<>();
                // save the term as key and doc_id and tf of the term in the document as value
                list.add(postingDetails);
                posting.put(entry.getKey(), list);
            } else {
                posting.get(entry.getKey()).add(postingDetails);
            }
        }
        setTopFiveEntities(docId,termsMapPerDocument);
    }

    private void setTopFiveEntities(String docId, HashMap<String, Integer> termsMapPerDocument) {
        List<Map.Entry<String, Integer>> list = new ArrayList<>(termsMapPerDocument.entrySet());
        Comparator <Map.Entry<String,Integer>> comparator = new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        };
        Collections.sort(list,comparator);
        Map<String, Integer> result = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        //sorted.putAll(termsMapPerDocument);
        TreeMap<String, Integer> topFiveEntities = new TreeMap<>();
        for (Map.Entry<String, Integer> entry : result.entrySet()) {
            if (entry.getKey().length() > 0 && Character.isUpperCase(entry.getKey().charAt(0))) {
                topFiveEntities.put(entry.getKey().toUpperCase(), entry.getValue());
                if (topFiveEntities.size() == 5)
                    break;
            }
        }
        list = new ArrayList<>(topFiveEntities.entrySet());
        Comparator <Map.Entry<String,Integer>> comparator1 = new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        };
        Collections.sort(list,comparator1);
        LinkedHashMap<String, Integer> sortedTopFiveEntites = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        ReadFile.mapOfDocs.get(docId).setTopFiveEntities(sortedTopFiveEntites);
    }

    /**
     This method copy the value of the given maps to the dictionary and posting of the cities field
     * @param docId
     * @param citiesMap
     */
    private void setCitiesDictionaryAndPosting(String docId, HashMap<String, ArrayList<Integer>> citiesMap) {
        for (Map.Entry<String,ArrayList<Integer>> entry: citiesMap.entrySet()){
            if (!citiesDictionary.containsKey(entry.getKey())){
                CityDetails cityDetails = getCityDetails(entry.getKey());
                citiesDictionary.put(entry.getKey(), cityDetails);
            }
            if (!citiesPosting.containsKey(entry.getKey())){
                // doc_id,TF
                StringBuilder postingNode = new StringBuilder("<" + docId + "," + toString(entry.getValue()) + ">");
                citiesPosting.put(entry.getKey(),postingNode);
            }
            else{
                citiesPosting.get(entry.getKey()).append("<" + docId + "," + toString(entry.getValue()) + ">");
            }
        }
    }

    /**
     *
     * @param city
     * @return the city details (country name, currency, population) from API
     * using API - "http://getcitydetails.geobytes.com/GetCityDetails?fqcn="
     */
    private CityDetails getCityDetails(String city){
        String link = "http://getcitydetails.geobytes.com/GetCityDetails?fqcn=";
        URL url;
        try {
            url = new URL(link + city);
            URLConnection conn = url.openConnection();
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = br.readLine();
            String currency = line.substring(line.indexOf("\"geobytescurrencycode\":") + 24, line.indexOf("geobytestitle") - 3);
            String countryName = line.substring(line.indexOf("\"geobytescountry\":") + 19, line.indexOf("geobytesregionlocation") - 3);
            String population = line.substring(line.indexOf("\"geobytespopulation\":") + 22, line.indexOf("geobytesnationalityplural") - 3);
            String capital = line.substring(line.indexOf("\"geobytescapital\":") + 19, line.indexOf("\"geobytestimezone\"") - 2);
            population = parsePopulation(population);
            return new CityDetails(countryName,currency,population);

        } catch (Exception e) { }
        return null;
    }

    /**
     * This function parse the populationSize by the rules of numbers
     * @param populationSize
     * @return
     */
    private String parsePopulation(String populationSize){
        StringBuilder temp = new StringBuilder(populationSize);
        if (populationSize.length() >= 4 && populationSize.length() < 7){
            temp.insert(populationSize.length() - 3, ".");
            temp.delete(populationSize.indexOf(".") + 3, populationSize.length());
            temp.append("K");
        }
        else if (populationSize.length() >=7 && populationSize.length() < 9){
            temp.insert(populationSize.length() - 6,".");
            temp.delete(populationSize.indexOf(".") + 3, populationSize.length());
            temp.append("M");

        }
        else if (populationSize.length() >=10){
            temp.insert(populationSize.length() - 9,".");
            temp.delete(populationSize.indexOf(".") + 3, populationSize.length());
            temp.append("B");

        }
        return temp.toString();
    }

    /**
     * This method write the two posing field and document map to the disk
     */
    public void writeDataToDisk(){
        writeDocumentDetailsToFile();
        writePostingToDisk();
        writeCityPosingToDisk();
    }

    /**
     * This method start creating the inverted index
     * First merge all the temp posting file were created during the program,
     * Then check if the same word exist both in lower and upper case posing files
     * Then divide the posing files to posting files for each letter and for each number
     */
    public void createInvertedIndex() {
        mergePostingFile();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                checkUpperCase();
            }
        });
        //checkUpperCase();
        thread.start();
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                dividePostingFile();
            }
        });
        //dividePostingFile();
        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                dividePostingFileNumbers();
            }
        });
        thread2.start();
        try {
            thread.join();
        } catch (InterruptedException e) { }
        thread1.start();
        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) { }

        //dividePostingFileNumbers();
        Thread thread3 = new Thread(new Runnable() {
            @Override
            public void run() {
                createDictionaryFile();
            }
        });
        //createDictionaryFile();
        Thread thread4 = new Thread(new Runnable() {
            @Override
            public void run() {
                createCityInvertedIndex();
            }
        });
        //createCityInvertedIndex();
        thread3.start();
        thread4.start();
        try {
            thread3.join();
            thread4.join();
        } catch (InterruptedException e) { }
        ReadFile.mapOfDocs.clear();
        ReadFile.stopWords.clear();
        createDocumentDetailsFile();



    }

    /**
     * This method create file to save the number of document in the corpus and their average length
     */
    private void createDocumentDetailsFile() {
        try {
            FileWriter writer = new FileWriter(pathToSaveIndex + "\\DetailsForRank.txt");
            long averageLengthOfDocuments = totalLength/Parse.numberOfDocuments;
            writer.write(Parse.numberOfDocuments + "," + averageLengthOfDocuments);
            writer.flush();
            writer.close();
        } catch (IOException e) { }

    }

    /**
     * This method write the content of the read file document map to the disk each 5000 documents
     * doc details written to file in this format:
     * docId,fileName,language,date,numberOfDistinctWords,max term frequency,{term1:tf1,term2:tf2,...,term5:tf5,}
     */
    private void writeDocumentDetailsToFile() {
        try {
            File file = new File(pathToSaveIndex + "\\documentsDetails.txt");
            if (!file.exists())
                file.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(file,true));
            for (Map.Entry<String,DocumentDetails> entry : ReadFile.mapOfDocs.entrySet()){
                // docId, length, fileName,language,date,numberOfDistinctWords,max term frequency,{term1:tf1,term2:tf2,...,term5:tf5,}
                writer.write(entry.getKey() + "," + entry.getValue().getLength() + "," + entry.getValue().getFileName() + "," +
                        entry.getValue().getLanguage() + "," + entry.getValue().getDate() + "," +
                        entry.getValue().getNumberOfDistinctWords() + "," + entry.getValue().getMaxTermFrequency() + ",");
                //writer.write("{");
                for (Map.Entry<String,Integer> entry1:entry.getValue().getTopFiveEntities().entrySet()){
                    writer.write(entry1.getKey() + ":" + entry1.getValue() + ",");
                }
                totalLength+=entry.getValue().getLength();
//                writer.write("}");
                writer.write("\n");
            }
            writer.flush();
            writer.close();
        }catch (IOException e) { }
        ReadFile.mapOfDocs = new HashMap<>();
    }

    /**
     * This method create three posting files: for upper case words, lower case words and numbers
     * and write the content of the posting field to those files
     */
    public void writePostingToDisk(){
        TreeMap<String,ArrayList<PostingDetails>> sortedPosting = new TreeMap<>(posting);
        String pathForNumbers = pathToSaveIndex + "\\posting" + numOfPosting + "numbers.txt";
        String pathForLower = pathToSaveIndex + "\\posting" + numOfPosting + "upperCase.txt";
        String pathForUpper = pathToSaveIndex + "\\posting" + numOfPosting + "lowerCase.txt";
        //numOfPosting++;
        try{
            File numbers = new File(pathForNumbers);
            File upper = new File(pathForLower);
            File lower = new File(pathForUpper);
            FileWriter writerNumbers = new FileWriter(numbers);
            FileWriter writerUpper = new FileWriter(upper);
            FileWriter writerLower = new FileWriter(lower);
            numbersPosting.add(numbers);
            upperPosting.add(upper);
            lowerPosting.add(lower);
            for (Map.Entry<String, ArrayList<PostingDetails>> entry : sortedPosting.entrySet()) {
                // write the term to the posting file
                String key = entry.getKey();
                if (key.length() == 0) continue;
                if (Character.isUpperCase(key.charAt(0))) {
                    writeToPostingFile(writerUpper,key,entry.getValue());
                    continue;
                }
                if (Character.isLowerCase(key.charAt(0))) {
                    writeToPostingFile(writerLower,key,entry.getValue());
                } else {
                    writeToPostingFile(writerNumbers,key,entry.getValue());
                }
            }
            writerNumbers.flush();
            writerUpper.flush();
            writerLower.flush();
            writerNumbers.close();
            writerUpper.close();
            writerLower.close();

        }catch (Exception e){ }
    }

    /**
     * This method write the given entry to the right posting file using the given writer
     * @param writer
     * @param key
     * @param entry
     */
    private void writeToPostingFile(FileWriter writer,String key, ArrayList<PostingDetails> entry) {
        try {
            writer.write(key + " ");
            for (PostingDetails postingDetails: entry) {
                // write doc detailed for each document in the format: doc_Id, TF
                writer.write("<" + postingDetails.getDocId() + "," + postingDetails.getTF() + ">");
            }
            writer.write("\n");
        } catch (IOException e) { }
    }

    /**
     * This method write city posting to dist=k
     */
    public void writeCityPosingToDisk() {
        TreeMap<String,StringBuilder> sortedPostingCity = new TreeMap<>(citiesPosting);
        String path = pathToSaveIndex + "\\posting" + numOfPosting + "city.txt";
        numOfPosting++;
        try {
            File postingCity = new File(path);
            FileWriter writer = new FileWriter(postingCity);
            cityPosting.add(postingCity);
            for (Map.Entry<String, StringBuilder> entry : sortedPostingCity.entrySet()) {
                // write the city term to posting in the format: key <doc_id,positions>
                String key = entry.getKey();
                writer.write(key + entry.getValue());
                writer.write("\n");
            }
            writer.flush();
            writer.close();
        }catch (Exception e) { }

    }

    /**
     * This method merge all the temporary posting file created in the process
     */
    public void mergePostingFile() {
        try {
            File postingNumbersFile = new File(pathToSaveIndex + "\\mergePostingNumbersTemp.txt");
            File postingLowerFile = new File(pathToSaveIndex + "\\mergePostingLowerCaseTemp.txt");
            File postingUpperFile = new File(pathToSaveIndex + "\\mergePostingUpperCaseTemp.txt");
            File postingCity = new File(pathToSaveIndex + "\\mergePostingCityTemp.txt");
            postingNumbersFile.createNewFile();
            postingLowerFile.createNewFile();
            postingUpperFile.createNewFile();
            postingCity.createNewFile();
            Comparator<String> cmp = new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
                }
            };
            Thread t1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ExternalSort.mergeSortedFiles(numbersPosting, postingNumbersFile, cmp, Charset.defaultCharset(), false);
                    } catch (IOException e) { }
                }
            });
            Thread t2 = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ExternalSort.mergeSortedFiles(lowerPosting, postingLowerFile, cmp, Charset.defaultCharset(), false);
                    } catch (IOException e) { }
                }
            });
            Thread t3 = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ExternalSort.mergeSortedFiles(upperPosting, postingUpperFile, cmp, Charset.defaultCharset(), false);
                    } catch (IOException e) { }
                }
            });
            Thread t4 = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ExternalSort.mergeSortedFiles(cityPosting, postingCity, cmp, Charset.defaultCharset(), false);
                    } catch (IOException e) { }
                }
            });
            t1.start();
            t2.start();
            t3.start();
            t4.start();
            t1.join();
            t2.join();
            t3.join();
            t4.join();
            threadPoolExecutor.execute(new RunnableMerge(postingNumbersFile,pathToSaveIndex + "\\mergePostingNumbers.txt"));
            threadPoolExecutor.execute(new RunnableMerge(postingLowerFile,pathToSaveIndex + "\\mergePostingLowerCase.txt"));
            threadPoolExecutor.execute(new RunnableMerge(postingUpperFile,pathToSaveIndex + "\\mergePostingUpperCase.txt"));
            threadPoolExecutor.execute(new RunnableMerge(postingCity,pathToSaveIndex + "\\mergePostingCities.txt"));
            threadPoolExecutor.shutdown();
            while (threadPoolExecutor.getActiveCount() != 0);
        } catch (Exception e) { }

    }

    /**
     * This function merge all the duplicate lines created in the merge
     * @param postingFile
     * @param postingFilesNames
     */
    private void mergeDuplicateLines(File postingFile, String postingFilesNames) {
        try {
            File newMergeFile = new File(postingFilesNames);
            FileWriter writer = new FileWriter(newMergeFile);
            BufferedReader bf = new BufferedReader(new FileReader(postingFile));
            String firstLine = bf.readLine();
            String secondLine = bf.readLine();
            while (firstLine != null){
                writer.write(firstLine);
                String [] firstLineSplit = firstLine.split(" <");
                if (secondLine == null)
                    break;
                String [] secondLineSplit = secondLine.split(" <");
                while (secondLine != null && firstLineSplit[0].equals(secondLineSplit[0])){
                    if (secondLine.length() > secondLineSplit[0].length() + 1)
                        writer.write(secondLine.substring(secondLineSplit[0].length() + 1));
                    secondLine = bf.readLine();
                    if (secondLine != null)
                        secondLineSplit = secondLine.split(" <");
                }
                writer.write("\n");
                firstLine = secondLine;
                secondLine = bf.readLine();
            }
            bf.close();
            writer.flush();
            writer.close();
            postingFile.delete();
        } catch (IOException e) { }
    }


    /**
     * This methods iterate over the two posting files (lower and upper) and checks if a term exist in both files
     * If does, remove it from upper case posting and adding it to the lower case posting
     */
    private void checkUpperCase() {
        try {
            File newLower = new File(pathToSaveIndex + "\\mergePostingLower.txt");
            File newUpper = new File(pathToSaveIndex + "\\mergePostingUpper.txt");
            newLower.createNewFile();
            newUpper.createNewFile();
            FileWriter writerLower = new FileWriter(newLower);
            FileWriter writerUpper = new FileWriter(newUpper);
            BufferedReader bfUpper = new BufferedReader(new FileReader(pathToSaveIndex + "\\mergePostingUpperCase.txt"));
            BufferedReader bfLower = new BufferedReader(new FileReader(pathToSaveIndex + "\\mergePostingLowerCase.txt"));
            String lineUpper = bfUpper.readLine();
            String lineLower = bfLower.readLine();

            while (lineLower != null && lineUpper != null){
                String [] lineUpperSplit = lineUpper.split(" ");
                String [] lineLowerSplit = lineLower.split(" ");
                // if line in upperCase file is bigger than line in lowerCase file, read the next upper line
                if (lineUpperSplit[0].toLowerCase().compareTo(lineLowerSplit[0]) < 0){
                    writerUpper.write(lineUpper);
                    //writerLower.write(lineLower);
                    lineUpper = bfUpper.readLine();
                    //lineLower = bfLower.readLine();
                    writerUpper.write("\n");
                    continue;
                }
                // if line in upperCase file is smaller than line in lowerCase file, read the next lower line
                if (lineUpperSplit[0].toLowerCase().compareTo(lineLowerSplit[0]) > 0){
                    writerLower.write(lineLower);
//                    writerUpper.write(lineUpper);
                    lineLower = bfLower.readLine();
//                    lineUpper = bfUpper.readLine();
                    writerLower.write("\n");
                    continue;
                }
                // if the term exist also in upper case, copy the content of the line to the new lower words posting file
                if (lineUpperSplit[0].toLowerCase().equals(lineLowerSplit[0])){
                    lineUpper = lineUpper.substring(lineUpperSplit[0].length());
                    writerLower.write(lineLower);
                    writerLower.write(lineUpper);
                    synchronized (dictionary) {
                        if (dictionary.containsKey(lineUpperSplit[0])) {
                            int numberOfAppearanceUpper = dictionary.get(lineUpperSplit[0]).getTotalTF();
                            //int numberOfAppearanceLower = dictionary.get(lineLowerSplit[0]).getTotalTF();
                            dictionary.get(lineLowerSplit[0]).setTotalTF(numberOfAppearanceUpper);
                            dictionary.remove(lineUpperSplit[0]);
                        }
                    }
                    writerLower.write("\n");
                }

                lineUpper = bfUpper.readLine();
                lineLower = bfLower.readLine();
            }
            bfLower.close();
            bfUpper.close();
            writerLower.flush();
            writerUpper.flush();
            writerLower.close();
            writerUpper.close();
            File file = new File(pathToSaveIndex + "\\mergePostingUpperCase.txt");
            file.delete();
            file = new File(pathToSaveIndex + "\\mergePostingLowerCase.txt");
            file.delete();

        } catch (IOException e) { }
    }



    /**
     * This method divide the posting file of number to 10 posting files for each number [0-9]
     */
    private void dividePostingFileNumbers(){

        try{
            char c = '0';
            int lineNumber = 1;
            File posting = new File (pathToSaveIndex + "\\posting0.txt");
            File numbers = new File(pathToSaveIndex + "\\mergePostingNumbers.txt");
            BufferedReader bf = new BufferedReader(new FileReader(numbers));
            FileWriter writer = new FileWriter(posting);
            String line = bf.readLine();

            while (true){
                while (line != null && !Character.isDigit(line.charAt(0))){
                    String key = line.substring(0,line.indexOf("<") - 1);
                    synchronized (dictionary) {
                        if(dictionary.get(key) != null)
                            dictionary.remove(key);
                    }
                    line = bf.readLine();
                }
                while (line != null && line.charAt(0) == c){
                    writer.write(line.substring(line.indexOf("<")).trim());
                    writer.write("\n");
                    String key = line.substring(0,line.indexOf("<") - 1);
                    synchronized (dictionary) {
                        if(dictionary.get(key) != null)
                            dictionary.get(key).setPtr(lineNumber);
                    }
                    lineNumber++;
                    line = bf.readLine();
                }
                c++;
                lineNumber = 1;
                if (c == ':')
                    break;
                posting = new File (pathToSaveIndex + "\\posting" + c + ".txt");
                writer.flush();
                writer.close();
                writer = new FileWriter(posting);
            }
            writer.flush();
            writer.close();
            bf.close();
            numbers.delete();
        } catch (IOException e) { }


    }

    /**
     * This method divide the lower and upper case posting file to 26 posting files
     * Each posting file will contain all the terms which start in the same word
     * For example, all terms start with A/a will be write to one posting file which will be named PostingA
     */
    private void dividePostingFile(){

        try{
            char c = 'A';
            int lineNumber = 1;
            File posting = new File (pathToSaveIndex + "\\postingA.txt");
            File newLower = new File(pathToSaveIndex + "\\mergePostingLower.txt");
            File newUpper = new File(pathToSaveIndex + "\\mergePostingUpper.txt");
            BufferedReader bfUpper = new BufferedReader(new FileReader(newUpper));
            BufferedReader bfLower = new BufferedReader(new FileReader(newLower));
            FileWriter writer = new FileWriter(posting);
            String lineUpper = bfUpper.readLine();
            String lineLower = bfLower.readLine();

            while (true){
                while (lineUpper != null && lineUpper.charAt(0) == Character.toUpperCase(c)){
                    String key = lineUpper.substring(0,lineUpper.indexOf("<") - 1);
                    synchronized (dictionary){
                        if (!dictionary.containsKey(key)){
                            lineUpper = bfUpper.readLine();
                            continue;
                        }
                    }
                    writer.write(lineUpper.substring(lineUpper.indexOf("<")).trim());
                    writer.write("\n");
                    synchronized (dictionary) {
                        if(dictionary.get(key) != null)
                            dictionary.get(key).setPtr(lineNumber);
                    }
                    lineNumber++;
                    lineUpper = bfUpper.readLine();
                }
                while (lineLower != null && Character.toUpperCase(lineLower.charAt(0)) == c){
                    String key = lineLower.substring(0,lineLower.indexOf("<") - 1);
                    synchronized (dictionary) {
                        if (!dictionary.containsKey(key)) {
                            lineLower = bfLower.readLine();
                            continue;
                        }
                    }
                    writer.write(lineLower.substring(lineLower.indexOf("<")).trim());
                    writer.write("\n");
                    //String key = lineLower.substring(0,lineLower.indexOf("<") - 1);
                    synchronized (dictionary) {
                        if(dictionary.get(key) != null)
                            dictionary.get(key).setPtr(lineNumber);
                    }
                    lineNumber++;
                    lineLower = bfLower.readLine();
                }
                c++;
                lineNumber = 1;
                if (c == '[')
                    break;
                posting = new File (pathToSaveIndex + "\\posting" + c + ".txt");
                writer.flush();
                writer.close();
                writer = new FileWriter(posting);
            }
            writer.flush();
            writer.close();
            bfLower.close();
            bfUpper.close();
            newLower.delete();
            newUpper.delete();
        } catch (IOException e) { }
    }


    /**
     * This method create the dictionary file by writing the content to the dictionary to a file countryName 'Dictionary.txt'.
     */
    private void createDictionaryFile(){
        try{
            File dictionaryFile = new File(this.folder.toString() + "\\Dictionary.txt");
            dictionaryFile.createNewFile();
            FileWriter fileWriter = new FileWriter(dictionaryFile);
            dictionaryToText(fileWriter);
            fileWriter.flush();
            fileWriter.close();
            numberOfTerms = dictionary.size();
            dictionary.clear();
        } catch (IOException e) { }

    }


    /**
     * This method create the cities inverted index (dictionary and posting)
     * by reading the @citiesDictionary and @citiesPosting fields
     */
    private void createCityInvertedIndex(){
        try {
            File posting = new File(pathToSaveIndex + "\\cityPosting.txt");
            File postingCity = new File(pathToSaveIndex + "\\mergePostingCities.txt");
            BufferedReader bf = new BufferedReader(new FileReader(postingCity));
            String line = bf.readLine();
            FileWriter postingWriter = new FileWriter(posting);
            File dictionary = new File(pathToSaveIndex + "\\cityDictionary.txt");
            FileWriter dictionaryWriter = new FileWriter(dictionary);
            TreeMap<String,CityDetails> citiesDictionarySorted = new TreeMap<>(citiesDictionary);
            int lineNumber = 1;
            for (Map.Entry<String, CityDetails> cityDetails : citiesDictionarySorted.entrySet()) {
                // write to cities dictionary: cityName,country name,currency,population|line number
                dictionaryWriter.write(cityDetails.getKey() + "," + cityDetails.getValue().toString() + "|" + lineNumber + "\n");
                lineNumber++;
                // write cities to posting: <doc_id, list of positions>
                //postingWriter.write("<" + citiesPosting.get(cityDetails.getKey()).toString() + ">" + "\n");
                if (line != null) {
                    postingWriter.write(line.substring(line.indexOf("<")));
                    postingWriter.write("\n");
                    line = bf.readLine();
                }
            }
            postingWriter.flush();
            dictionaryWriter.flush();
            postingWriter.close();
            dictionaryWriter.close();
            bf.close();
            postingCity.delete();
            citiesDictionary.clear();
        }catch (IOException e) {}
    }




    /**
     * This function split the given line by the given delimiter
     * @param line
     * @param delimiter
     * @return
     */
    private String[] splitByDelimiter( String line, char delimiter) {
        if(line.equals(" ")) {
            String[] result = {""};
            return result;
        }
        CharSequence[] temp = new CharSequence[(line.length() / 2) + 1];
        int wordCount = 0;
        int i = 0;
        int j = line.indexOf(delimiter, 0); // first substring

        while (j >= 0) {
            String word = line.substring(i,j);
            word = word.trim();
            temp[wordCount++] = word;
            i = j + 1;
            j = line.indexOf(delimiter, i); // rest of substrings
        }

        temp[wordCount++] = line.substring(i); // last substring

        String[] result = new String[wordCount];
        System.arraycopy(temp, 0, result, 0, wordCount);

        return result;
    }


    /**
     *
     * @return the dictionary in a string format:  term, number of appearance, row number in the posting file
     */
    private void dictionaryToText(FileWriter fileWriter) {
        File file = new File(pathToSaveIndex + "\\dictionaryToDisplay.txt");
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            StringBuilder ans = new StringBuilder(" ");
            TreeMap <String,TermDetails> map = new TreeMap<>(dictionary);
            for (Map.Entry<String,TermDetails> entry: map.entrySet()){
                if (entry.getValue().getPtr() == 0){
                    synchronized (dictionary) {
                        dictionary.remove(entry.getKey());
                    }
                    continue;
                }
                // term,number of appearance,line number
                fileWriter.write(entry.getKey() + "," + entry.getValue().getTotalTF() + "," + entry.getValue().getDocumentFrequency() + "," + entry.getValue().getPtr() +  "\n");
                writer.write(entry.getKey() + "," + entry.getValue().getTotalTF() + "\n");
            }
            writer.flush();
            writer.close();
        } catch (IOException e) { }
    }


    /**
     *
     * @return the dictionary in a string in the format: term, number of appearance (for each term)
     */
    public TreeSet<String> dictionaryToString(){
        TreeSet<String> list = new TreeSet<String>();
        try {
            FileInputStream file = new FileInputStream(pathToSaveIndex + "\\dictionaryToDisplay.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(file));
            String termLine = br.readLine();
            while (termLine!=null){
                list.add(termLine);
                termLine = br.readLine();
            }
            br.close();
            return list;
        } catch (IOException e) { }
        return null;
    }
    /**
     *
     * @param arrayList
     * @return string builder representing the array list
     */
    private StringBuilder toString(ArrayList<Integer> arrayList) {
        Iterator<Integer> it = arrayList.iterator();
        StringBuilder arrayListToString = new StringBuilder();
        while (it.hasNext()) {
            Integer e = it.next();
            arrayListToString.append(e + " ");
        }
        return arrayListToString;
    }


    /**
     * This method deletes all the posting and dictionary files created in the program
     */
    public void reset(){
        for(File file: folder.listFiles())
            if (!file.isDirectory())
                file.delete();
        folder.delete();
    }


    /**
     *
     * @return the  number of terms parsed and indexed (the @dictionary size)
     */
    public int getNumberOfTerms() {
        return numberOfTerms;
    }

    public String getPathToSaveIndex() {
        return pathToSaveIndex;
    }

    /**
     *
     */

    private class RunnableMerge implements Runnable{

        File file;
        String fileName;

        public RunnableMerge(File file, String fileName) {
            this.file = file;
            this.fileName = fileName;
        }

        @Override
        public void run() {
            mergeDuplicateLines(file,fileName);
        }
    }

}
