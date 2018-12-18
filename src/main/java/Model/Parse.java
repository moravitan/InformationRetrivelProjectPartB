package Model;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.text.BreakIterator;
import java.util.*;


public class Parse {

    Indexer indexer;
    boolean isStemming;
    Stemmer stemmer;
    HashMap <String, Integer> termsMapPerDocument; // <Term, TF>
    // CHANGED HERE
    TreeMap<Integer,String> topFiveEntities;
    ///////////////
    HashMap<String, ArrayList<Integer>> citiesMap; // <CityName, listOfPositions>
    HashMap<String,String> monthDictionary;
    HashMap<String,String> numbersDictionary;
    HashMap<String,String> pricesDictionary;
    HashMap<String,String> weightDictionary;
    HashSet<String> percentSet;
    HashSet<String> dollarSet;
    // the maximum frequency in the current doc being parsed.
    int maxTermFrequency = 0;
    // counter for the distinct words in the document being parsed.
    int numberOfDistinctWords = 0;
    // counter for the position of the word in the document being parsed
    int position = -1;
    // counter for the number of documents has been parsed
    int numberOfDocuments = 0;
    // docId of the current doc being parsed.
    String docId;

    public static int numCounter = 0;


    /**
     *
     * @param isStemming - boolean indicator to know if need to stem or not
     * @param indexer -
     */
    public Parse(boolean isStemming, Indexer indexer) {
        this.termsMapPerDocument = new HashMap<>();
        this.topFiveEntities = new TreeMap<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2.compareTo(o1);
            }
        });
        this.citiesMap = new HashMap<>();
        this.indexer = indexer;
        this.isStemming = isStemming;
        if (isStemming)
            stemmer = new Stemmer();
        // create and fill dictionaries
        this.monthDictionary = new HashMap<>();
        this.numbersDictionary = new HashMap<>();
        this.pricesDictionary = new HashMap<>();
        this.percentSet = new HashSet<>();
        this.dollarSet = new HashSet<>();
        this.weightDictionary = new HashMap<>();
        fillDictionaries();
    }

    /**
     * This function fills of the dictionaries in this class with the right parameters
     */
    private void fillDictionaries() {
        monthDictionary.put("JANUARY", "01");
        monthDictionary.put("January", "01");
        monthDictionary.put("Jan", "01");
        monthDictionary.put("FEBRUARY", "02");
        monthDictionary.put("February", "02");
        monthDictionary.put("Feb", "02");
        monthDictionary.put("MARCH", "03");
        monthDictionary.put("March", "03");
        monthDictionary.put("Mar", "03");
        monthDictionary.put("APRIL", "04");
        monthDictionary.put("April", "04");
        monthDictionary.put("Apr", "04");
        monthDictionary.put("MAY", "05");
        monthDictionary.put("May", "05");
        monthDictionary.put("JUNE", "06");
        monthDictionary.put("June", "06");
        monthDictionary.put("JULY", "07");
        monthDictionary.put("July", "07");
        monthDictionary.put("AUGUST", "08");
        monthDictionary.put("August", "08");
        monthDictionary.put("Aug", "08");
        monthDictionary.put("SEPTEMBER", "09");
        monthDictionary.put("September", "09");
        monthDictionary.put("Sept", "09");
        monthDictionary.put("OCTOBER", "10");
        monthDictionary.put("October", "10");
        monthDictionary.put("Oct", "10");
        monthDictionary.put("NOVEMBER", "11");
        monthDictionary.put("November", "11");
        monthDictionary.put("Nov", "11");
        monthDictionary.put("DECEMBER", "12");
        monthDictionary.put("December", "12");
        monthDictionary.put("Dec", "12");
        // fill numbers dictionary
        numbersDictionary.put("Thousand", "K");
        numbersDictionary.put("Thousand,", "K");
        numbersDictionary.put("Million", "M");
        numbersDictionary.put("Million,", "M");
        numbersDictionary.put("Billion", "B");
        numbersDictionary.put("Billion,", "B");
        numbersDictionary.put("Trillion", "00B");
        numbersDictionary.put("Trillion,", "00B");
        // fill prices dictionary
        pricesDictionary.put("million", " M Dollars");
        pricesDictionary.put("m", " M Dollars");
        pricesDictionary.put("billion", "000 M Dollars");
        pricesDictionary.put("bn", "000 M Dollars");
        pricesDictionary.put("trillion", "000000 M Dollars");
        // fill percent set
        percentSet.add("percent");
        percentSet.add("Percent");
        percentSet.add("Percentage");
        percentSet.add("percentages");
        percentSet.add("Percentages");
        //Dollars|Dollar|dollar|dollars
        dollarSet.add("Dollars");
        dollarSet.add("Dollar");
        dollarSet.add("dollar");
        dollarSet.add("dollars");
        dollarSet.add("Dollars,");
        dollarSet.add("Dollar,");
        dollarSet.add("dollar,");
        dollarSet.add("dollars,");
        //
        weightDictionary.put("grams", " gr");
        weightDictionary.put("kilograms", " kgs");
        weightDictionary.put("tons", " tons");
        weightDictionary.put("grams,", " tons");
        weightDictionary.put("kilograms,", " tons");
        weightDictionary.put("tons,", " tons");


    }

    /**
     * This function get a doc from the ReadFile class and parsing it line by line
     * @param docId
     * @param docText
     */
    public void parsing(String docId, String docText, boolean isCorpus){
        this.docId = docId;
        // every 40 files, move all the data in the term map into indexer and reset the map
        numberOfDocuments++;
        // break the text line by line
        BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
        // get the text between the tags <TEXT> </TEXT>
        Document document = Jsoup.parse(docText);
        String text = document.getElementsByTag("TEXT").text();
        iterator.setText(text);
        int start = iterator.first();
        for (int end = iterator.next();
             end != BreakIterator.DONE;
             start = end, end = iterator.next()) {
            try{
                parsingLine(text.substring(start,end));
            }
            catch (Exception e){ }

        }
        if(isCorpus) {
            // update and reset all the parameters of the current document
            ReadFile.mapOfDocs.get(docId).setMaxTermFrequency(maxTermFrequency);
            ReadFile.mapOfDocs.get(docId).setNumberOfDistinctWords(numberOfDistinctWords);
            ////////CHANGED HERE
            ReadFile.mapOfDocs.get(docId).setTopFiveEntities(topFiveEntities);
            /////////////
            indexer.setAll(docId, termsMapPerDocument, citiesMap);
        }
        maxTermFrequency = 0;
        numberOfDistinctWords = 0;
        this.position = -1;
        termsMapPerDocument = new HashMap<>();
        citiesMap = new HashMap<>();
        topFiveEntities = new TreeMap<>();
    }


    /**
     * This function get a document line from the parsing() function, and parsing it.
     * @param docLine
     */
    private void parsingLine(String docLine){
        String punctuations = ",.";
        //split all words by whitespace into array of words
        //docLine = docLine.replaceAll("[():?!]","");
        //Check if function splitByDelimiter is better
        //String [] wordsInDoc = docLine.trim().split("\\s+");
        String[] wordsInDoc = splitByDelimiter(docLine, ' ');
        //if more than one line, contains "" at wordInDoc[wordInDoc.length -1]
        //find where is the dot closing the sentence and remove it
        if (wordsInDoc.length == 1) {
            if (wordsInDoc[0].equals("") || wordsInDoc[0].equals("."))
                return;
            int indexOfDot = wordsInDoc[0].indexOf('.');
            if (indexOfDot != -1)
                wordsInDoc[0] = wordsInDoc[0].substring(0, indexOfDot);
        } else {
            if (wordsInDoc[wordsInDoc.length - 1].length() < 1) {
                wordsInDoc[wordsInDoc.length - 2] = wordsInDoc[wordsInDoc.length - 2].substring(0, wordsInDoc[wordsInDoc.length - 2].length() - 1);
            } else {
                wordsInDoc[wordsInDoc.length - 1] = wordsInDoc[wordsInDoc.length - 1].substring(0, wordsInDoc[wordsInDoc.length - 1].length() - 1);
            }
        }
        for (int i = 0; i < wordsInDoc.length; i++) {
            this.position++;

            if (wordsInDoc[i].length() == 0 || punctuations.contains(wordsInDoc[i]) || wordsInDoc[i].contains("--"))
                continue;

            // if wordsInDoc[i] start with " or '
            if (wordsInDoc[i].startsWith("\"") || wordsInDoc[i].startsWith("'") || wordsInDoc[i].startsWith("/")) {
                wordsInDoc[i] = wordsInDoc[i].substring(1);
                if (wordsInDoc[i].length() == 0)
                    continue;
            }

            // if wordInDoc[i] start with multiple . or -remove them
            while (wordsInDoc[i].startsWith(".") || wordsInDoc[i].startsWith("-")){
                wordsInDoc[i] = wordsInDoc[i].substring(1);
            }

            //check if the word in the array ends with ','
            boolean isPair = true;
            if (wordsInDoc[i].endsWith(",") || (i + 1 < wordsInDoc.length && wordsInDoc[i+1].startsWith("("))
                    || wordsInDoc[i].endsWith(")")) {
                isPair = false;
                wordsInDoc[i] = wordsInDoc[i].substring(0, wordsInDoc[i].length() - 1);
                if (wordsInDoc[i].endsWith(","))
                    continue;
            }

            // remove () if exist in the beginning and end of the word
            // like (WORD)
            if (wordsInDoc[i].startsWith("(") || wordsInDoc[i].startsWith("["))
                wordsInDoc[i] = wordsInDoc[i].substring(1);
            if (wordsInDoc[i].endsWith(")") || wordsInDoc[i].endsWith("]") || wordsInDoc[i].endsWith(":"))
                wordsInDoc[i] = wordsInDoc[i].substring(0,wordsInDoc[i].length() - 1);

            if (ReadFile.stopWords.contains(wordsInDoc[i].toLowerCase()) && !wordsInDoc[i].equalsIgnoreCase("between") && !wordsInDoc[i].equals("may"))
                continue;



            if (wordsInDoc[i].equalsIgnoreCase("moscow"))
                numCounter++;

            // check if the string in wordInDoc[i] contains only letters
            if (!monthDictionary.containsKey(wordsInDoc[i]) && !wordsInDoc[i].equals("between") && isWord(wordsInDoc[i])){
                // the word doesn't exist in the dictionary starting with LOWER and UPPER
                if (!termsMapPerDocument.containsKey(wordsInDoc[i].toUpperCase()) && !termsMapPerDocument.containsKey(wordsInDoc[i].toLowerCase())) {
                    if (Character.isUpperCase(wordsInDoc[i].charAt(0)))
                        updateTerm(wordsInDoc[i].toUpperCase());
                    else
                        updateTerm(wordsInDoc[i]);
                    continue;
                }
                //WORD starts with UPPER and exist in dic with LOWER, update dic with LOWER CASE of WORD
                if (Character.isUpperCase(wordsInDoc[i].charAt(0)) && termsMapPerDocument.containsKey(wordsInDoc[i].toLowerCase())) {
                    updateTerm(wordsInDoc[i].toLowerCase());
                    continue;
                }

                //WORD starts with LOWER and exist in dic with UPPER, update dic with LOWER CASE of WORD and update key to LOWER
                if (!Character.isUpperCase(wordsInDoc[i].charAt(0)) && termsMapPerDocument.containsKey(wordsInDoc[i].toUpperCase())) {
                    updateTerm(wordsInDoc[i]);
/*                    termsMapPerDocument.get(wordsInDoc[i]).putAll(termsMapPerDocument.get(wordsInDoc[i].toUpperCase()));
                    int currentNumOfAppearance = 0;
                    if (termsMapPerDocument.get(wordsInDoc[i]).containsKey(documentDetails))
                        currentNumOfAppearance = (termsMapPerDocument.get(wordsInDoc[i])).get(documentDetails);
                    if (currentNumOfAppearance > maxTermFrequency)
                        maxTermFrequency = currentNumOfAppearance;
                    (termsMapPerDocument.get(wordsInDoc[i])).put(documentDetails, currentNumOfAppearance + 1);
                    termsMapPerDocument.remove(wordsInDoc[i].toUpperCase());*/
                    continue;
                }

                // if starts with lower and doesnt exist in the dictionary with upper, insert the word to the dictionary in lower
                if (Character.isLowerCase(wordsInDoc[i].charAt(0))){
                    updateTerm(wordsInDoc[i]);
                    continue;
                }
                //covers case of starts only with UPPER
                updateTerm(wordsInDoc[i].toUpperCase());
                continue;

            }

            while (wordsInDoc[i].startsWith("['()\":&!?+]")){
                wordsInDoc[i] = wordsInDoc[i].substring(1);
            }

            //if it is NOT a tag , doesn't starts with '<'
            //if(!((wordsInDoc[i].charAt(0)== '<') || wordsInDoc[i].charAt(wordsInDoc[i].length()-1) == '>')){
            if (!(wordsInDoc[i].contains("<"))) {

                Vector<StringBuilder> numberTerm = new Vector<>();

                //if a term kind: B(/b)etween number and number like: Between 10 and 12
                if (i + 1 < wordsInDoc.length && wordsInDoc[i].equalsIgnoreCase("between") && isInteger(wordsInDoc[i + 1],false)) {
                    int advanceI = i;
                    boolean fraction = false;
                    //Between 1 and 3 || Between 1/3 and 3/4 || Between 1 and 3.5 ,saved at dic as 1-3.5
                    if (i + 3 < wordsInDoc.length && wordsInDoc[i + 2].equalsIgnoreCase("and") && isInteger(wordsInDoc[i + 3],false)) {
                        numberTerm.add(new StringBuilder(wordsInDoc[i + 1]));
                        StringBuilder term1 = parseNumbers(false, numberTerm, fraction);
                        numberTerm.clear();
                        numberTerm.add(new StringBuilder(wordsInDoc[i + 3]));
                        advanceI = i + 3;
                        //Between 1 and 3 1/2
                        if (i + 4 < wordsInDoc.length && isInteger(wordsInDoc[i + 4],false)) {
                            numberTerm.add(new StringBuilder(wordsInDoc[i + 4]));
                            fraction = true;
                            advanceI = advanceI + 1;
                        }
                        StringBuilder term2 = parseNumbers(false, numberTerm, fraction);
                        numberTerm.clear();
                        term1.append("-" + term2);
                        updateTerm(term1.toString());
                        i = advanceI;
                        continue;
                    }
                    //Between 1 1/2 and number || Between 1 Million and number
                    if (i + 4 < wordsInDoc.length && (isInteger(wordsInDoc[i + 2],false) || numbersDictionary.containsKey(wordsInDoc[i + 2])) && wordsInDoc[i + 3].equalsIgnoreCase("and") && isInteger(wordsInDoc[i + 4],false)) {
                        numberTerm.add(new StringBuilder(wordsInDoc[i + 1]));
                        numberTerm.add(new StringBuilder(wordsInDoc[i + 2]));
                        StringBuilder term1 = parseNumbers(false, numberTerm, isInteger(wordsInDoc[i + 2],false));
                        numberTerm.clear();
                        numberTerm.add(new StringBuilder(wordsInDoc[i + 4]));
                        advanceI = i + 4;
                        //Between 1 1/2 and 1 3/4
                        if (i + 5 < wordsInDoc.length && isInteger(wordsInDoc[i + 5],false)) {
                            numberTerm.add(new StringBuilder(wordsInDoc[i + 5]));
                            fraction = true;
                            advanceI = advanceI + 1;
                        }
                        StringBuilder term2 = parseNumbers(false, numberTerm, fraction);
                        numberTerm.clear();
                        term1.append("-" + term2);
                        updateTerm(term1.toString());
                        i = advanceI;
                        continue;
                    }
                }

                //first Month Name then Number
                if (monthDictionary.containsKey(wordsInDoc[i])) {
                    //insert to dic also Month countryName by itself
                    updateTerm(wordsInDoc[i]);
                    if (i + 1 < wordsInDoc.length && isPair) {
                        wordsInDoc[i + 1] = removeLastComma(wordsInDoc[i + 1]);
                        if (i + 1 < wordsInDoc.length && isInteger(wordsInDoc[i + 1],true)) {
                            //change to date format: MM-DD and insert to dic
                            StringBuilder date = convertToDateFormat(wordsInDoc[i], wordsInDoc[i + 1]);
                            if (date != null)
                                updateTerm(date.toString());
                            i = i + 1;
                            continue;
                        }
                    }
                }
                //first Number then Month Name
                if (i + 1 < wordsInDoc.length && isInteger(wordsInDoc[i],true)) {
                    wordsInDoc[i + 1] = wordsInDoc[i + 1].replaceAll(",", "");
                    if (monthDictionary.containsKey(wordsInDoc[i + 1])) {
                        //insert to dic month countryName alone
                        updateTerm(wordsInDoc[i + 1]);
                        //change date like 14 May to 05-14
                        StringBuilder date = convertToDateFormat(wordsInDoc[i + 1], wordsInDoc[i]);
                        if (date != null)
                            updateTerm(date.toString());
                        i = i + 1;
                        continue;
                    }
                }

                //check if it is an Integer like: 0,0 | 0% | $0 | 0.0 | 0/0
                boolean twoNumsCells = false;
                if (wordsInDoc[i].startsWith("$") || wordsInDoc[i].endsWith("%") || isInteger(wordsInDoc[i],false)) {
                    //if the cell i+1 also part of the number
                    if (isPair && i + 1 < wordsInDoc.length) {
                        wordsInDoc[i + 1] = removeLastComma(wordsInDoc[i + 1]);
                        //handle term like: 2 1/2-2 3/4 | 2 Million-4 Million
                        int makafIndex = wordsInDoc[i + 1].indexOf('-');
                        if (makafIndex != -1) {
                            int advanceI = i + 1;
                            String beforeMakaf = wordsInDoc[i + 1].substring(0, makafIndex);
                            String afterMakaf = wordsInDoc[i + 1].substring(makafIndex + 1);
                            //million-22
                            //1/2-22 || 1/2-3/4
                            String numberMakaf = "[0-9]*[0-9/]+[0-9]";
                            if (numbersDictionary.containsKey(beforeMakaf) || beforeMakaf.matches(numberMakaf)) {
                                numberTerm.add(new StringBuilder(wordsInDoc[i]));
                                numberTerm.add(new StringBuilder(beforeMakaf));
                                StringBuilder term1 = parseNumbers(false, numberTerm, beforeMakaf.contains("/"));
                                numberTerm.clear();
                                if (isInteger(afterMakaf,false)) {
                                    numberTerm.add(new StringBuilder(afterMakaf));
                                    if (i + 2 < wordsInDoc.length && (numbersDictionary.containsKey(wordsInDoc[i + 2]) || wordsInDoc[i + 2].matches(numberMakaf))) {
                                        numberTerm.add(new StringBuilder(wordsInDoc[i + 2]));
                                        StringBuilder term2 = parseNumbers(false, numberTerm, wordsInDoc[i + 2].contains("/"));
                                        numberTerm.clear();
                                        term1.append("-" + term2.toString());
                                        updateTerm(term1.toString());
                                        advanceI = advanceI + 1;
                                    } else {
                                        StringBuilder afterMN = parseNumbers(false, numberTerm, false);
                                        term1.append("-" + afterMN);
                                        updateTerm(term1.toString());
                                    }

                                } else {
                                    term1.append("-" + afterMakaf);
                                    updateTerm(term1.toString());
                                }
                                i = advanceI;
                                continue;

                            }
                        }
                        // check if the next cell is a fraction
                        // if it does it part of the number in wordInDoc[i]
                        if (wordsInDoc[i+1].indexOf("/") != -1 && isInteger(wordsInDoc[i + 1],false)) {
                            twoNumsCells = true;
                        }
                    }

                    StringBuilder numTerm = new StringBuilder(wordsInDoc[i]);

                    boolean isDollar = false;

                    if (wordsInDoc[i].charAt(0) == '$') {
                        isDollar = true;
                        twoNumsCells = false;
                        numTerm.delete(0, 1);
                        numberTerm.add(numTerm);
                    }
                    //$price million/billion/trillion
                    if (i + 1 < wordsInDoc.length && isDollar && pricesDictionary.containsKey(wordsInDoc[i + 1])) {
                        numberTerm.add(new StringBuilder(wordsInDoc[i + 1]));
                        updateTerm(wordsInDoc[i + 1].toLowerCase());
                        //call to func which deal with prices
                        StringBuilder finalTerm = parseNumbers(true, numberTerm, twoNumsCells);
                        numberTerm.clear();
                        updateTerm(finalTerm.toString());
                        i = i + 1;
                        continue;
                    }

                    //$price
                    if (isDollar) {
                        //call to func which deal with prices
                        StringBuilder finalTerm = parseNumbers(true, numberTerm, twoNumsCells);
                        updateTerm(finalTerm.toString());
                        numberTerm.clear();
                        continue;
                    }

                    //price Dollars/Dollar/dollar/dollars
                    if (i + 1 < wordsInDoc.length && !isDollar && dollarSet.contains(wordsInDoc[i + 1])) {
                        numberTerm.add(numTerm);
                        numberTerm.add(new StringBuilder(wordsInDoc[i + 1]));
                        //update dic with Dollars/Dollar/dollar/dollars
                        updateTerm(wordsInDoc[i + 1].toLowerCase());
                        //call to func which deal with prices
                        StringBuilder finalTerm = parseNumbers(true, numberTerm, twoNumsCells);
                        numberTerm.clear();
                        updateTerm(finalTerm.toString());
                        i = i + 1;
                        continue;
                    }

                    //number grams/kilograms/tons
                    if (i + 1 < wordsInDoc.length && !isDollar && weightDictionary.containsKey(wordsInDoc[i + 1])) {
                        numberTerm.add(numTerm);
                        numberTerm.add(new StringBuilder(wordsInDoc[i + 1]));
                        //call to func which deal with numbers
                        StringBuilder finalTerm = parseNumbers(false, numberTerm, twoNumsCells);
                        numberTerm.clear();
                        updateTerm(finalTerm.toString());
                        i = i + 1;
                        continue;
                    }

                    //price m/bn/fraction Dollars/Dollar/dollar/dollars
                    if (i + 2 < wordsInDoc.length && !isDollar && dollarSet.contains(wordsInDoc[i + 2])) {
                        if (pricesDictionary.containsKey(wordsInDoc[i + 1]) || twoNumsCells) {
                            numberTerm.add(numTerm);
                            numberTerm.add(new StringBuilder(wordsInDoc[i + 1]));
                            //update dic with Dollars/Dollar/dollar/dollars
                            updateTerm(wordsInDoc[i + 1]);
                            //call to func which deal with prices
                            StringBuilder finalTerm = parseNumbers(true, numberTerm, twoNumsCells);
                            numberTerm.clear();
                            updateTerm(finalTerm.toString());
                            i = i + 2;
                            continue;
                        }
                    }
                    // number million/billion/trillion/fraction grams/kilograms/tons
                    if (i + 2 < wordsInDoc.length && !isDollar && weightDictionary.containsKey(wordsInDoc[i + 2])) {
                        String currWord = Character.toUpperCase(wordsInDoc[i+1].charAt(0)) + wordsInDoc[i+1].substring(1);
                        if (numbersDictionary.containsKey(currWord) || twoNumsCells) {
                            numberTerm.add(numTerm);
                            numberTerm.add(new StringBuilder(currWord));
                            numberTerm.add(new StringBuilder(wordsInDoc[i+2]));
                            //call to func which deal with numbers
                            StringBuilder finalTerm = parseNumbers(false, numberTerm, twoNumsCells);
                            numberTerm.clear();
                            updateTerm(finalTerm.toString());
                            i = i + 2;
                            continue;
                        }
                    }

                    //price million/billion/trillion U.S. Dollars/Dollar/dollar/dollars
                    else if (i + 3 < wordsInDoc.length && !isDollar && pricesDictionary.containsKey(wordsInDoc[i + 1]) && wordsInDoc[i + 2].equals("U.S.") && dollarSet.contains(wordsInDoc[i + 3])) {
                        numberTerm.add(numTerm);
                        numberTerm.add(new StringBuilder(wordsInDoc[i + 1]));
                        //update dic with Dollars/Dollar/dollar/dollars
                        updateTerm(wordsInDoc[i + 1]);
                        //call to func which deal with prices
                        StringBuilder finalTerm = parseNumbers(true, numberTerm, twoNumsCells);
                        numberTerm.clear();
                        updateTerm(finalTerm.toString());
                        i = i + 3;
                        continue;

                    }

                    int cellNum = i;
                    //if like 22 3/4 marge to one cell
                    if (twoNumsCells) {
                        cellNum = i + 1;
                        numTerm.append(" " + wordsInDoc[i + 1]);
                    }

                    //if ends with '%'
                    if (wordsInDoc[cellNum].charAt(wordsInDoc[cellNum].length() - 1) == '%') {
                        String numTermStr = numTerm.toString();
                        updateTerm(numTermStr);
                        if (twoNumsCells)
                            i = i + 1;
                        continue;
                    }

                    //if appears percent / percentage / Percent / Percentage /  percents / percentages / Percents / Percentages at the cell after
                    if (cellNum + 1 < wordsInDoc.length && percentSet.contains(wordsInDoc[cellNum + 1])) {
                        if (twoNumsCells) {
                            i = i + 1;
                        }
                        numTerm.append("%");
                        updateTerm(numTerm.toString());
                        updateTerm(wordsInDoc[cellNum + 1]);

                        i = i + 1;
                        continue;
                    }

                    numberTerm.add(new StringBuilder(wordsInDoc[i]));
                    if (twoNumsCells || (i + 1 < wordsInDoc.length && numbersDictionary.containsKey(wordsInDoc[i + 1]))) {
                        numberTerm.add(new StringBuilder(wordsInDoc[i + 1]));
                    }
                    StringBuilder finalTerm = parseNumbers(false, numberTerm, twoNumsCells);
                    numberTerm.clear();
                    updateTerm(finalTerm.toString());
                    if (twoNumsCells)
                        i = i + 1;
                    continue;
                }

                //handle Word-Word || Word-Word-Word
                int indxMakaf = wordsInDoc[i].indexOf('-');
                if (indxMakaf > 0 && !StringUtils.isNumeric(wordsInDoc[i])) {
                    if (!StringUtils.isNumeric(wordsInDoc[i].substring(indxMakaf + 1))) {
                        updateTerm(wordsInDoc[i]);
                        continue;
                    }
                }

                //handle number-number like 2,200-2,400 || 120-120.5 or number-word like: 2-kids, 2.5-people
                int makafIndex = wordsInDoc[i].indexOf('-');
                if (makafIndex != -1) {
                    String beforeMakaf = wordsInDoc[i].substring(0, makafIndex);
                    String afterMakaf = wordsInDoc[i].substring(makafIndex + 1);
                    //handle number-number like: 1/2-3/4 || 2-2.5
                    if (isInteger(beforeMakaf,false)) {
                        numberTerm.add(new StringBuilder(beforeMakaf));
                        StringBuilder term1 = parseNumbers(false, numberTerm, false);
                        numberTerm.clear();
                        if (isInteger(afterMakaf,false)) {
                            numberTerm.add(new StringBuilder(afterMakaf));
                            //1-1 1/2 || 1-2 Million
                            if (i + 1 < wordsInDoc.length && (isInteger(wordsInDoc[i + 1],false) || numbersDictionary.containsKey(wordsInDoc[i + 1]))) {
                                numberTerm.add(new StringBuilder(wordsInDoc[i + 1]));
                                twoNumsCells = true;
                                i = i + 1;
                            }
                            StringBuilder term2 = parseNumbers(false, numberTerm, twoNumsCells);
                            term1.append("-" + term2);
                            numberTerm.clear();
                            updateTerm(term1.toString());
                            continue;
                        }
                        //number-word like 2-girls
                        else {
                            term1.append("-" + afterMakaf);
                            updateTerm(term1.toString());
                            continue;
                        }
                    }


                }

/*                wordsInDoc[i] = wordsInDoc[i].replaceAll("[^a-zA-Z]", "");
                if (wordsInDoc[i].length() == 0)
                    continue;*/

            }

        }

    }

    /**
     * gets date like: May 15 , Jun 1994 convert to: 05-15 , 1994-01
     * @param monthName variations of month countryName
     * @param number
     * @return date in format: num-num
     */
    private StringBuilder convertToDateFormat(String monthName , String number) {
        StringBuilder date = new StringBuilder();
        int num = Integer.valueOf(number);
        String month = monthDictionary.get(monthName);
        //        // year
        if (num > 31){
            date.append(number + "-" + month);
            return date;
        }
        // day between 0 and 10
        else if (num > 0 && num < 10){
            StringBuilder smallNum = new StringBuilder("0");
            smallNum.append(number);
            date.append(month + "-" + smallNum.toString());
            return date;
        }
        else if (num >= 10){
            date.append(month + "-" + number);
            return date;
        }
        return null;
    }

    /**
     * handle numbers and prices
     * @param isPrice - if the new term is a price definition or just a number
     * @param numberTerm - vector which contains words after split
     * @param twoNumsCells - if the number is : number fraction
     */
    private StringBuilder parseNumbers(boolean isPrice, Vector<StringBuilder> numberTerm, boolean twoNumsCells){
        int priceTermSize = numberTerm.size();
        StringBuilder finalTerm =  new StringBuilder();
        while(true) {
            if (!twoNumsCells && priceTermSize >= 2) {
                String key = numberTerm.elementAt(1).toString();
                if (isPrice) {
                    if (pricesDictionary.containsKey(key))
                        finalTerm.append(numberTerm.elementAt(0)).append(pricesDictionary.get(key));
                    else if (dollarSet.contains(key))
                        finalTerm.append(numberTerm.elementAt(0)).append(" Dollars");
                }
                else{
                    // number million
                    if(numbersDictionary.containsKey(key)) {
                        finalTerm.append(numberTerm.elementAt(0)).append(numbersDictionary.get(numberTerm.elementAt(1).toString()));
                    }
                    //number kilogram
                    else if (weightDictionary.containsKey(key) && priceTermSize == 2)
                        finalTerm.append(numberTerm.elementAt(0)).append(weightDictionary.get(numberTerm.elementAt(1).toString()));
                    // cover weight cases
                    // 13.8 billion kilogram -> 13.8B kgs
                    if (priceTermSize == 3)
                        finalTerm.append(weightDictionary.get(numberTerm.elementAt(priceTermSize-1).toString()));
                }
                break;
            }
            //handle number fraction
            else if(twoNumsCells){
                if(isPrice)
                    finalTerm.append(numberTerm.elementAt(0)).append(" " + numberTerm.elementAt(1)).append(" Dollars");
                else {
                    finalTerm.append(numberTerm.elementAt(0)).append(" " + numberTerm.elementAt(1));
                }
                if (priceTermSize == 3) {
                    finalTerm.append(weightDictionary.get(numberTerm.elementAt(2).toString()));
                }
                break;
            }

            String tmpNum = numberTerm.elementAt(0).toString();
            tmpNum = tmpNum.replaceAll("," , "");

            //handle decimal fraction
            int dotIndex = tmpNum.indexOf('.');
            //1.5 11.5 111.5
            if(dotIndex!=-1 && dotIndex<4) {
                return new StringBuilder(tmpNum);
            }

            //if smaller than million
/*            if (!isPrice && tmpNum.length() < 5){
                finalTerm.append(numberTerm.elementAt(0));
                if (priceTermSize == 2) {
                    finalTerm.append(weightDictionary.get(numberTerm.elementAt(1).toString()));
                }

                break;
            }*/
            if (tmpNum.length() < 7 || dotIndex == 4) {
                if(isPrice)
                    finalTerm.append(numberTerm.elementAt(0)).append(" Dollars");
                else{
                    dotIndex = tmpNum.indexOf(".");
                    if (dotIndex == 4){
                        tmpNum = tmpNum.replaceAll("\\.","");
                        finalTerm.append(tmpNum);
                        finalTerm.insert(dotIndex-3,".");
                        finalTerm = allCharactersZero(finalTerm);
                        finalTerm.append("K");
                        break;
                    }
                    if (tmpNum.contains(".") && dotIndex < 5){
                        finalTerm.append(tmpNum);
                    }
                    else{
                        tmpNum = tmpNum.replaceAll("\\." , "");
                        finalTerm.append(tmpNum);
                        if (dotIndex == -1)
                            finalTerm.insert(finalTerm.length()-3,".");
                        else
                            finalTerm.insert(dotIndex-3,".");
                        finalTerm = allCharactersZero(finalTerm);
                        finalTerm.append("K");
                    }
                    if (priceTermSize == 2) {
                        finalTerm.append(weightDictionary.get(numberTerm.elementAt(1).toString()));
                    }
                }
                break;
            }

            else {
                if(isPrice) {
                    finalTerm.append(tmpNum);
                    finalTerm.insert(finalTerm.length() - 6, ".");
                    finalTerm = allCharactersZero(finalTerm);
                    finalTerm.append(" M Dollars");
                    break;
                }
                else{
                    // if between million and billion
                    if(tmpNum.length() >= 7 && tmpNum.length() < 10){
                        finalTerm.append(tmpNum);
                        finalTerm.insert(finalTerm.length()-6,".");
                        finalTerm = allCharactersZero(finalTerm);
                        finalTerm.append("M");
                    }
                    // if between billion and trillion
                    else if(tmpNum.length() >= 10 && tmpNum.length() < 13){
                        finalTerm.append(tmpNum);
                        finalTerm.insert(finalTerm.length()-6,".");
                        finalTerm = allCharactersZero(finalTerm);
                        finalTerm.append("B");
                    }
                    if (priceTermSize == 2) {
                        finalTerm.append(weightDictionary.get(numberTerm.elementAt(1).toString()));
                    }
                }
                break;
            }
        }
        return finalTerm;
    }

    /**
     * This function checks if a given string contains only zero chars after the dot, and if it does remove all of them
     * @param str
     * @return
     */
    private StringBuilder allCharactersZero(StringBuilder str) {
        for (int i = str.length() - 1; i > -1 ; i--) {
            if (str.charAt(i) != '0')
                break;
            else
                str = str.delete(i,str.length());
        }
        str.delete(str.indexOf(".") + 3,str.length());
        return str;
    }

    /**
     *
     * @param name
     * @return true is countryName contains only letters or the chars ' :
     */
    private boolean isWord(String name) {
        char[] chars = name.toCharArray();

        for (char c : chars) {
            if (c == '\'' || c == ':') continue;
            if(!Character.isLetter(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * This function checks if a given string is in a number format
     * For isDate = false, return true only if all the string contains digits.
     * otherwise return true if the given string is in one of the next format:
     * 1/2, 1, 1,000, 1.5
     * @param str
     * @param isDate
     * @return
     */
    private boolean isInteger(String str, boolean isDate) {
        boolean ans = false;
        int counter = 0;
        if (str == null) {
            return false;
        }
        if (str.isEmpty()) {
            return false;
        }
        int i = 0;
        char c = str.charAt(i);
        char lastChar = str.charAt(str.length()-1);
        if (c < '0' || c > '9' || lastChar < '0' || lastChar >'9')
            return false;
        else
            ans = true;
        i = 1;
        if (isDate) {
            for (; i < str.length() - 1; i++) {
                c = str.charAt(i);
                if (c < '0' || c > '9') {
                    return false;
                }
            }
            ans = true;
        }
        else{
            int indexOfBackslash = str.indexOf('/');
            if (indexOfBackslash != -1) {
                indexOfBackslash = str.indexOf('/', indexOfBackslash + 1);
                if (indexOfBackslash != -1)
                    return false;
            }
            int indexOfDot = str.indexOf('.');
            if (indexOfDot != -1){
                indexOfDot = str.indexOf('.', indexOfDot + 1);
                if (indexOfDot != -1)
                    return false;
            }

            for (; i < str.length() - 1; i++) {
                c = str.charAt(i);
                if (c == '/' || c == '.' || c == ',' || c >= '0' || c <= '9') {
                    ans = true;
                }
                else if(c < '0' || c > '9'){
                    return false;
                }
            }
        }
        return ans;
    }
    /**
     * if the word ends with comma, returns the word without the comma
     * else returns the word
     * for example:word= 1,000,000, returns: 1,000,000
     * @param word
     * @return
     */
    private String removeLastComma(String word){
        if(word.endsWith(",")) {
            word= word.substring(0,word.length()-1);
        }
        return word;
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
        line = line.replaceAll("[\"&!;?+*|^#@]","");
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
     * create term if not exist, update the dictionary of docs
     * @param key - the countryName of city under tag <F P=104>
     */
    private void updateTerm(String key){
        if (isStemming)
            key = stemmer.setTerm(key);
        //check if term not exist in dictionary , add key to TreeMapDic and create it's dic of docs
        if(!termsMapPerDocument.containsKey(key)){
            termsMapPerDocument.put(key,1);
            numberOfDistinctWords = numberOfDistinctWords + 1;
            // if the key is city countryName, save the position of the key in the file
            //key exist , update value.
        } else {
            int currentNumOfAppearance = termsMapPerDocument.get(key);
            termsMapPerDocument.put(key,currentNumOfAppearance + 1);
            // find the max frequency in each doc
            if (currentNumOfAppearance + 1 > maxTermFrequency)
                maxTermFrequency = currentNumOfAppearance;
        }
        if (ReadFile.cities.contains(key.toUpperCase())) {
            if (!citiesMap.containsKey(key.toUpperCase())) {
                ArrayList<Integer> listOfPositions = new ArrayList<>();
                listOfPositions.add(position);
                citiesMap.put(key.toUpperCase(), listOfPositions);
            } else {
                citiesMap.get(key.toUpperCase()).add(position);
            }
        }
        if (Character.isUpperCase(key.charAt(0)))
            setTopFiveEntities(key,termsMapPerDocument.get(key));
    }

    /**
     * This method save the top five terms which appear in upper case and their tf
     * @param term
     * @param tf
     */
    private void setTopFiveEntities(String term,int tf) {
        if (topFiveEntities.size() <= 5)
            topFiveEntities.put(tf,term);
        if (tf < topFiveEntities.lastKey())
            return;
        else{
            int needToDelete = 0;
            for(Map.Entry<Integer,String> entry : topFiveEntities.entrySet()){
                // if the new tf is bigger from one of the keys in the topFiveEntities dictionary
                if (entry.getKey() < tf){
                    needToDelete = entry.getKey();
                }
            }
            topFiveEntities.remove(needToDelete);
            topFiveEntities.put(tf,term);
        }
    }


    /**
     * This function sends  the remaining dictionary to indexer and than call the create inverted file in indexer object
     */
    public void notifyDone() throws InterruptedException {
        //createCityIndex();
        //indexer.buildIndex(termsMapPerDocument);
        indexer.writeDataToDisk();
        indexer.createInvertedIndex();
    }


    //<editor-fold desc="Getters">
    /**
     *
     * @return the termsMapPerDocument dictionary
     */
    public HashMap<String, Integer> getTermsMapPerDocument() {
        return termsMapPerDocument;
    }

    /**
     *
     * @return the corpus languages field
     */
    public TreeSet<String> getCorpusLanguages() {
        return ReadFile.corpusLanguages;
    }


    /**
     *
     * @return the indexer object
     */
    public Indexer getIndexer() {
        return indexer;
    }

    /**
     *
     * @return the number of document has been parsed.
     */
    public int getNumberOfDocuments() {
        return numberOfDocuments;
    }
    //</editor-fold>

}


