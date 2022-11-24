/* 
 *  Class      : StemMap
 *  Created on : Apr 9, 2013
 *  Author     : chmelarp
 *  Copyright (C) 2009-2013  Petr Chmelar
 */

package net.chmelab.kwdemo;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.util.Comparator;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.danishStemmer;
import org.tartarus.snowball.ext.defaultStemmer;
import org.tartarus.snowball.ext.englishStemmer;
import org.tartarus.snowball.ext.germanStemmer;


    
/**
 * StemMap extends Map<stem, WordMap>
 * which is Map<stem, Map<word, List<position>>> (TreeMap<String, TreeMap<String, ArrayList<Integer>>>)
 * Hint: be used to parse Postgresql ts_vector
 * TODO: stem word net :)
 */
public class StemMap extends TreeMap<String, WordMap> {
    // Unicode words delimiter ... http://docs.oracle.com/javase/1.5.0/docs/api/java/util/regex/Pattern.html
    public static final String wordDELIMITER = "[^\\p{L}]+"; // unicode [^a-z\\d]+
    public static final String sentenceDELIMITER = "[\\n()\\!\\?\\.,:;]+"; // end of sentence
    
    private String language;    // ISO language code (= locale)
    private int words;      // number of words
    private int goWords;      // number of non-stop words
    private int goBigrams;      // number of non-stop bigrams
    private SnowballStemmer stemmer;
    private TreeSet<String> stops;
    
    public static final boolean biGRAMs = true; /** Use bigrams if true */
    private String previous;    // previous word (not stem) for bigram construction

    public StemMap(String language, TreeSet<String> stopWords) {
        this.language = language;
        this.words = 0;
        this.goWords = 0;
        this.goBigrams = 0;
        this.previous = "";
        
        // create stemmer according to the language
        if (language.compareTo("en") == 0) {
            stemmer = (SnowballStemmer) (new englishStemmer());
        } else if (language.compareTo("de") == 0) {
            stemmer = (SnowballStemmer) (new germanStemmer());
        } else if (language.compareTo("da") == 0) {
            stemmer = (SnowballStemmer) (new danishStemmer());
        } else {   // unsupported language
            stemmer = (SnowballStemmer) (new defaultStemmer());
        }

        // WordMap stopwords
        this.stops = new TreeSet<String>();
        for (String stopWord : stopWords) {
            String sw = stem(stopWord);
            if (!sw.isEmpty()) {
                this.stops.add(sw);
            }
        }
//        System.out.println("Stop stems: "+ this.stops);
    }

    
    /**
     * This is the most usable function and may be used right after the constructor
     * @param text
     * @return kewords extracted from the text
     */
    public String getKeywords(String text) {
        String keywordStr = "";
        
        if (text != null && !text.isEmpty()) {
            this.putText(text);
        }
        
        Map keyMap = WordMap.sortByValue(this);
        int numKeywords = (int)Math.ceil(Math.sqrt(this.words)) + 3;
        if (numKeywords > 20) numKeywords = 20;
        
        int i = 0;
        for (Object w : keyMap.keySet()) {
            if (++i > numKeywords) break;
            keywordStr += ((WordMap)keyMap.get(w)).getShortestWord() + ", ";
        }        
        
        if (keywordStr.length() > 2) {
            return keywordStr.substring(0, keywordStr.length()-2);
        }
        else return keywordStr;
    }
    
    
    /**
     * This function is suitable only after the text is put
     * @deprecated 
     * @return keywords
     */
    public String getKeywords() {    
        return this.getKeywords(null);
    }
    
    /**
     * Processes the text
     * @param text
     * @return success if there are some keywords
     */
    public boolean putText(String text) {
        // TODO: split by \n\t.,;" - in case of bigrams
        String[] sentences = text.split(StemMap.sentenceDELIMITER); // \t\\x0B?
        
        for (String sentence : sentences) {
            Scanner scanner = new Scanner(sentence).useDelimiter(StemMap.wordDELIMITER);  // unicode [^a-z]+
            while (scanner.hasNext()) {
                String word = scanner.next(); // language-sensitive processing :)
                putWord(word);
            }
        }
        
        if (this.size() > 0) return true;
        else return false;
    }

    
    /**
     * Processes a word
     * @param word
     * @return word just as it entered
     */
    public String putWord(String word) {
        this.words++;   // counting all words, including stopwords
        String s = stem(word);
        
        // Kill all fuck-ups and stop words
        if (s != null && !s.isEmpty() && !isStop(s)) {
            this.goWords++;
            
            // add the word
            if (this.containsKey(s)) {  // stem is there
                if (this.get(s).containsKey(word)) {   // word is there
                    this.get(s).get(word).add(this.words);   // add position
                } else { // add word
                    ArrayList<Integer> positions = new ArrayList<Integer>();
                    positions.add(this.words);
                    this.get(s).put(word, positions);
                }
            } else { // add stem and the word
                WordMap wordMap = new WordMap();
                ArrayList<Integer> positions = new ArrayList<Integer>();
                positions.add(this.words);
                wordMap.put(word, positions);
                this.put(s, wordMap);
            }
        
            // process bigrams (both words were not stopped)
            if (biGRAMs && !previous.isEmpty()) {
                this.goBigrams++;
                
                String biword = previous +" "+ word;
                String prevs = stem(previous);
                String bis = prevs +" "+ s;
                
                // add the biword
                if (this.containsKey(bis)) {  // stem is there
                    if (this.get(bis).containsKey(biword)) {   // biword is there
                        this.get(bis).get(biword).add(this.words -1);   // add position
                    } else { // add biword
                        ArrayList<Integer> positions = new ArrayList<Integer>();
                        positions.add(this.words -1);
                        this.get(bis).put(biword, positions);
                    }
                } else { // add stem and the biword
                    WordMap wordMap = new WordMap();
                    ArrayList<Integer> positions = new ArrayList<Integer>();
                    positions.add(this.words -1);
                    wordMap.put(biword, positions);
                    this.put(bis, wordMap);
                }
            } 
            previous = word;
        }
        else {
            previous = "";
        }
        
        return word;
    }
        
       
    /**
     * Reducing inflected (or sometimes derived) words to their stem.
     *
     * @param word
     * @return stem
     */
    public String stem(String word) {
        stemmer.setCurrent(word.toLowerCase());
        stemmer.stem();
        return stemmer.getCurrent();
    }

    
    /**
     * Checks the word if it is in the most common words dictionary (stop list)
     * @param stem
     * @return success if found
     */
    public boolean isStop(String stem) {
        return this.stops.contains(stem);
    }

   
    /**
     * Removes all of the stems from this class. The map will be empty after
     * this call returns.
     */
    @Override
    public void clear() {
        super.clear();
    }

    /**
     * @return the language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * @return the words
     */
    public Integer getWords() {
        return words;
    }

    /**
     * @return the goWords
     */
    public int getGoWords() {
        return goWords;
    }

    /**
     * @return the goBigrams
     */
    public int getGoBigrams() {
        return goBigrams;
    }
}
