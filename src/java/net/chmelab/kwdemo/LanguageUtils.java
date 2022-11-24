/* 
 *  Class      : LanguageUtils
 *  Created on : Apr 4, 2013
 *  Author     : chmelarp
 *  Copyright (C) 2013  Petr Chmelar
 */
//
//  Usage Note (Danish letter frequency)
//  http://www.sttmedia.com/characterfrequency-danish
//
//  The list may only be used together with the publishing of the internet adress 
//  stefantrost.de and the prior consent of Stefan Trost (contact). If you need 
//  additional lists, for example in a different sorting or on the basis of another 
//  text, we can provide this list for only five euros. Simply write to us.    
//    
//  Czech letter frequencies come from
//  KRÁLÍK, Jan. Czech Alphabet. The Czech Language [online]. 2001 [cit. 2013-04-04]. 
//  Available at WWW: http://www.czech-language.cz/alphabet/alph-prehled.html
//
//  Rest letter frequencies are from Wikipedia http://en.wikipedia.org/wiki/Letter_frequency
//   
package net.chmelab.kwdemo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.chmelab.kwdemo.StemMap;

/**
 * This class encapsulates several language utils persistent for each session
 * All probabilities in this class are in percents - probability *100(%) 
 * All words "parsed" and characters are lowercase letters in unicode (UTF-8) 
 * End of line and () together with .,;:!? are considered sentence borders.
 * Gets Math.ceil(sqrt(this.lastWordCount)) keywords (< 20 :)
 *
 * @author chmelarp
 */
public class LanguageUtils {

    // A default language - must be a agentLocale value from the DB (see below)
    public static final String DEFAULTlocale = ""; // Autodetect where possible
    public static final String DEFAULTlanguage = "Autodetect";
    // Supported languages Autodetect, C++, Czech, Danish, Dutch, English, 
    // French, German, Italian, Polish, Portuguese, Spanish, Swedish, Turkish
    // Key is agentLocale "", ++, cs, da, nl, en, fr, de, it, pl, pt, es, sv, tr
    private Map<String, String> languages;
    // User-agent locale (session-wide)
    private Locale agentLocale;
    // this is an arbitrary large value to limit the frequency analysis
    public static final int MINletters = 20;
    // Letter frequencies for perspective languages
    private Map<String, TreeMap<Character, Float>> letterFrequencies;
    private Integer lastLetterCount;    // count in countLetterFrequencies
    
    // this is an arbitrary large value to limit the common-word analysis
    public static final int MINwords = 1;
    // Stop lists for perspective languages
    private Map<String, TreeSet<String>> stopWords;
    private Integer lastWordCount;      // count in ????????????????
    private Map<String, Integer> lastWords; // last words including stopwords
    // Resulting probabilities of last text counted for each language
    private Map<String, Float> lastProbabilities;   // count in countProbabilities
    // The language with the highest probability from the above (differs from the JSP language!)
    private String lastLanguage;    // count in detectLanguage

    // Finally, here are all the keywords
    private StemMap lastKeywords; // processed in processKeywords

    /**
     * Constructor loads languages, letter frequencies and stop words (the most
     * frequent words) for each language
     *
     * @param commons
     */
    public LanguageUtils(Commons commons, Locale agentLocale) {
        this.agentLocale = agentLocale;

        // init ...
        this.lastLanguage = DEFAULTlocale;
        this.languages = new TreeMap<String, String>();
        this.letterFrequencies = new TreeMap<String, TreeMap<Character, Float>>();
        this.stopWords = new TreeMap<String, TreeSet<String>>();

        try {
            Statement stmt = commons.conn.createStatement();
            String query = "SELECT locale, language, letters, percentages, stopwords "
                    + "  FROM keywordemo.languages;";
            //  + "  ORDER by language";
            ResultSet rset = stmt.executeQuery(query);

            while (rset.next()) {
                String locale = rset.getString("locale");
                String language = rset.getString("language");

                // fill in the language
                this.languages.put(locale, language);

                String let = rset.getString("letters");     // aux
                String per = rset.getString("percentages");
                String sto = rset.getString("stopwords");

                if (let != null && per != null && sto != null) {
                    // parse "{a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,à,á,å,ä,ą,æ,ç,ĉ,ć,č,œ,ď,è,é,ê,ë,ę,ě,ĝ,ğ,ĥ,î,ì,ï,í,ı,ĵ,ø,ł,ñ,ń,ň,ò,ö,ó,ř,ŝ,ş,ś,š,ß,ť,ù,ú,ŭ,ü,ů,ý,ź,ż,ž}"
                    String[] letters = let.substring(1, let.length() - 1).split(",");
                    String[] percentStrs = per.substring(1, per.length() - 1).split(",");

                    if (letters.length == percentStrs.length) {
                        TreeMap<Character, Float> letterMap = new TreeMap<Character, Float>();

                        // parse floats, add to the wordCounts
                        for (int i = 0; i < percentStrs.length; i++) {
                            letterMap.put(new Character(letters[i].charAt(0)), Float.parseFloat(percentStrs[i]));
                        }

                        // fill the percentages
                        this.letterFrequencies.put(locale, letterMap);
                    }

                    // parse 
                    String[] stopStrs = sto.substring(2, sto.length() - 2).split("','");
                    TreeSet<String> wordSet = new TreeSet<String>();
                    boolean addAll = wordSet.addAll(Arrays.asList(stopStrs));
                    // fill the percentages
                    if (addAll) {
                        this.stopWords.put(locale, wordSet);
                    }
                }
            }

//            // Debug 4 languages x 4 letters
//            for (String lan : this.wordCounts.keySet()) {
//                TreeMap<Character, Float> m = this.wordCounts.get(lan);
//                System.out.println("\n"+ lan + ":");
//                for (Character c : m.keySet()) {
//                    System.out.println(c + ": " + m.get(c)); 
//                }
//            }
        } catch (SQLException ex) {
            Logger.getLogger(LanguageUtils.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Detects language to print (best 2 matches) or Aboriginal Parses the text
     * and fills the
     *
     * @param text
     * @return Map of results
     */
    public Map<String, Float> countProbabilities(String text) {
        this.lastProbabilities = new TreeMap<String, Float>();

        // Count textLetterFrequencies as square sums 
        Map<String, Float> letterProbs = null;
        TreeMap<Character, Float> textLetterFrequencies = countLetterFrequencies(text);

        if (textLetterFrequencies != null) {
            letterProbs = new TreeMap<String, Float>();

            // the range of vector similarity (~100%)
            double textProbabilityOne = 0.0;

            // For all languages count the square sums
            for (String local : this.getLanguages().keySet()) {
//                // Uncomment to print debug
//                String header = "Letter;";
//                String line;
                if (local.compareTo(DEFAULTlocale) == 0) {
//                    line = "Text;";
                    for (Character ch : textLetterFrequencies.keySet()) {  // go through text characters
                        textProbabilityOne += textLetterFrequencies.get(ch) * textLetterFrequencies.get(ch); // count the max. number
//                        header += ch + ";";
//                        line += textLetterFrequencies.get(ch)+ ";";
                    }
//                    System.out.println(header);
//                    System.out.println(line);                
                } else if (this.letterFrequencies.get(local) != null) {
//                    line = local +";";
                    float square = (float) 0; // init

                    for (Character ch : textLetterFrequencies.keySet()) {  // go through text characters
                        if (this.letterFrequencies.get(local).get(ch) == null) {
//                            line += "0;";
                        } else {
                            square += (textLetterFrequencies.get(ch) - this.letterFrequencies.get(local).get(ch)) * (textLetterFrequencies.get(ch) - this.letterFrequencies.get(local).get(ch));
//                            line += this.wordCounts.get(local).get(ch)+ ";";
                        }
                    }

                    letterProbs.put(local, square);
//                    System.out.println(line);
                }
            }

            // for all languages normalize square sums
            for (String loca : this.getLanguages().keySet()) {
                if (letterProbs.get(loca) != null && letterProbs.get(loca) > 0) {
                    letterProbs.put(loca, new Float(textProbabilityOne / letterProbs.get(loca)));
                }
            }
//            System.out.println("Letter probabilities: "+letterProbs);
        }


        // Count textWordCounts as sums
        TreeMap<String, Integer> textWordCounts = countWordCounts(text);
        Map<String, Float> wordProbs = new TreeMap<String, Float>();
        // For all languages count the square sums
        for (String local : this.stopWords.keySet()) {
            int wordsCount = 0;
            if (textWordCounts != null) {
                for (String word : textWordCounts.keySet()) {
                    // if the word is in an approprite language
                    if (this.stopWords.get(local).contains(word)) {
                        wordsCount += textWordCounts.get(word);
                    }
                }
            }
            // normalize the word count to 100 and multiply by the probabilities
            wordProbs.put(local, new Float((double) 100 / this.lastWordCount * 100 * wordsCount / stopWords.get(local).size()));
        }
//        System.out.println("Word probabilities: "+ wordProbs);


        // agentLocale ISO code
        String agLang = this.agentLocale.getLanguage(); // TODO: put below if found!

        // Summarize together :)
        for (String lang : wordProbs.keySet()) {
                if (letterProbs != null && letterProbs.containsKey(lang)) {
                    this.lastProbabilities.put(lang, wordProbs.get(lang) + letterProbs.get(lang));
                } else {
                    this.lastProbabilities.put(lang, wordProbs.get(lang));
                }
        }
//        System.out.println("Language probabilities: "+ printLastProbabilities());

        return this.lastProbabilities;
    }

    /**
     * Detects most probable language
     *
     * @param text
     * @return Map of results
     */
    public String detectLanguage(String text) {
        // count the language probabilities... watch the null :)
        this.lastProbabilities = countProbabilities(text);
        if (this.lastProbabilities == null || this.lastProbabilities.isEmpty()) {
            return DEFAULTlocale;
        }

        // get the maximal probability
        String maxLanguage = "";
        float maxProbability = 0;
        for (String lan : this.lastProbabilities.keySet()) {
            if (this.lastProbabilities.get(lan) > maxProbability) {
                maxLanguage = lan;
                maxProbability = this.lastProbabilities.get(lan);
            }
        }

        // Find the best fitting language 
        return this.lastLanguage = maxLanguage;
    }

    /**
     * Detects language to print (best 2+ matches) or Aboriginal
     *
     * @param text
     * @return String as "English (95%); German (78%)"
     */
    public String printLastProbabilities() {
        return this.lastProbabilities.toString();
    }

    /**
     * Prints out frequencies of UTF-8 input characters (in percent)
     *
     * @param text input file
     * @return map of letters with relative counts
     */
    public TreeMap<Character, Float> countLetterFrequencies(String text) {
        TreeMap<Character, Float> letterCounts = new TreeMap<Character, Float>();

        this.lastLetterCount = 0;
        for (int i = 0; i < text.length(); i++) { // just << max_int :(
            this.lastLetterCount++;
            Character ch = Character.toLowerCase(text.charAt(i));
            if (Character.isLetter(ch)) {
                if (letterCounts.get(ch) == null) {
                    letterCounts.put(ch, new Float(1));
                } else {
                    letterCounts.put(ch, new Float(letterCounts.get(ch) + 1));
                }
            }
        }

//        System.out.println("Letter counts: " + this.lastLetterCount); // debug
//        System.out.println("Sum: " + this.lastLetterCount); // debug

        // normalize counts into frequencies
        for (Character ch : letterCounts.keySet()) {
            letterCounts.put(ch, (100 * letterCounts.get(ch) / (float) this.lastLetterCount));
        }
//        System.out.println("Letter probabilities: " + this.lastLetterCount); // debug

        if (this.lastLetterCount < MINletters) {
            return null;    // this is an arbitrary large value
        } else {
            return letterCounts;
        }
    }

    /**
     * Prints out frequencies of words (in actual numbers)
     *
     * @param text input file
     * @return map of words with counts
     */
    public TreeMap<String, Integer> countWordCounts(String text) {
        TreeMap<String, Integer> wordCounts = new TreeMap<String, Integer>();
        this.lastWordCount = 0;

        // 4 all words
        Scanner scanner = new Scanner(text.toLowerCase()).useDelimiter(StemMap.wordDELIMITER);  // unicode [^a-z]+
        while (scanner.hasNext()) {
            this.lastWordCount++;
            String word = scanner.next(); // TODO: language-sensitive processing!

            // add the word
            if (wordCounts.get(word) == null) {
                wordCounts.put(word, 1);
            } else {
                wordCounts.put(word, wordCounts.get(word) + 1);
            }
        }

//        System.out.println("Word counts: "+ wordCounts);

        if (this.lastWordCount < MINwords) {
            return null;    // this is an arbitrary large value
        } else {
            return wordCounts;
        }
    }

    /**
     * Process keywords in da, de, en (default)
     *
     * @param text
     * @return keywords
     */
    public StemMap processKeywords(String text, String language) {
        if (language == null || (language.compareTo("de") != 0 && language.compareTo("da") != 0)) {
            language = "en";
        }
        
        this.lastKeywords = new StemMap(language, this.stopWords.get(language));
        this.lastKeywords.putText(text);
        this.lastWordCount = lastKeywords.getWords();
//        System.out.println("All keywords: "+ this.lastKeywords);
        
        // 4 keywords delete redundant, keep just some (sqrt)
        // return Math.ceil(sqrt(this.lastWordCount)) keywords; 
        
        return this.lastKeywords;
    }
    
    
    public String getKeywords(String text, String language) {
        this.processKeywords(text, language);
        return lastKeywords.getKeywords();
    }


    /**
     * @return the agentLocale
     */
    public Locale getAgentLocale() {
        return agentLocale;
    }

    /**
     * @param agentLocale the agentLocale to set
     */
    public void setAgentLocale(Locale agentLocale) {
        this.agentLocale = agentLocale;
    }

    /**
     * @return the languages
     */
    public Map<String, String> getLanguages() {
        return languages;
    }

    /**
     * @param languages the languages to set
     */
    public void setLanguages(Map<String, String> languages) {
        this.languages = languages;
    }

    /**
     * @return the lastLetterCount
     */
    public Integer getLastLetterCount() {
        return lastLetterCount;
    }

    /**
     * @return the lastWordCount
     */
    public Integer getLastWordCount() {
        return lastWordCount;
    }

    /**
     * @return the lastProbabilities
     */
    public Map<String, Float> getLastProbabilities() {
        return lastProbabilities;
    }

    /**
     * @return the lastLanguage
     */
    public String getLastLanguage() {
        return lastLanguage;
    }

    /**
     * @return the language based on the code
     */
    public String getLanguage(String localeCode) {
        return this.languages.get(localeCode);
    }

}
