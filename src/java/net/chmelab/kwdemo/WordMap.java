/* 
 *  Class      : WordMap
 *  Created on : Apr 10, 2013
 *  Author     : chmelarp
 *  Copyright (C) 2009-2013  Petr Chmelar
 */

package net.chmelab.kwdemo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A structure holding pair Map<word, Array<position>>
 * Comparison is based on the size of array and the shortest word (if longer :)
 * A capital TFW (abbreviation) is multiplied by factor of occurence 1.6, Name by 1.4 (using ceil)
 * This structure is equivalent to the (Postgresql) ts_vector
 * @author chmelarp
 */
public class WordMap extends TreeMap<String, ArrayList<Integer>> implements Comparable {
    public static final double FACTORcapital = 1.6;
    public static final double FACTORname = 1.4;

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     * @param o
     * @return result of the comparison
     */
    @Override
    public int compareTo(Object o) {
        // throw new UnsupportedOperationException("Comparison to other classes is not supported yet.");
        if (o.getClass() != WordMap.class) return -1;    // this object is larger than any else
        WordMap other = (WordMap) o;
        
        int thisCount = 0;
        int thisSize = Integer.MAX_VALUE;
        double thisFactor = 1;
        for (String word : this.keySet()) {
            thisCount += this.get(word).size();
            if (word.length() < thisSize) thisSize = word.length();
            if (thisFactor < FACTORcapital && word.compareTo(word.toUpperCase()) == 0) thisFactor = FACTORcapital;
            else if (thisFactor < FACTORname && word.substring(0,1).compareTo(word.substring(0,1).toUpperCase()) == 0) thisFactor = FACTORname;
        }
        
        int otherCount = 0;
        int otherSize = Integer.MAX_VALUE;        
        double otherFactor = 1;
        for (String word : other.keySet()) {
            otherCount += other.get(word).size();
            if (word.length() < otherSize) otherSize = word.length();            
            if (otherFactor < FACTORcapital && word.compareTo(word.toUpperCase()) == 0) otherFactor = FACTORcapital;
            else if (otherFactor < FACTORname && word.substring(0,1).compareTo(word.substring(0,1).toUpperCase()) == 0) otherFactor = FACTORname;
        }

        // give preference to names and abbreviations
        thisCount = (int)Math.ceil(thisFactor * thisCount);
        otherCount = (int)Math.ceil(otherFactor * otherCount);        
        
        if (thisCount > otherCount) return -1;  // this object is more than other object by count
        else if (thisCount < otherCount) return 1;
        else if (thisSize > otherSize) return -1; // this object is more than other object by word sizes
        else if (thisSize < otherSize) return 1;
        else return 0; // this object is equal to the other
    }
    
    
    /**
     * Returns the shortest word in the map
     * @return word
     */
    public String getShortestWord() {
        String word = null;
        int size = Integer.MAX_VALUE;
        
        for (String w : this.keySet()) {
            if (w.length() < size) {
                size = w.length() ;
                word = w;
            }
        }
        
        return word;
    }
    
    
//
//    http://stackoverflow.com/questions/109383/how-to-sort-a-mapkey-value-on-the-values-in-java  
//    http://stackoverflow.com/posts/2581754/revisions
//    
//    // Test:    
//    Random random = new Random(System.currentTimeMillis());
//    Map<String, Integer> testMap = new HashMap<String, Integer>(1000);
//    for(int i = 0 ; i < 1000 ; ++i) {
//        testMap.put( "SomeString" + random.nextInt(), random.nextInt());
//    }
//
//    testMap = MapUtil.sortByValue( testMap );
//    Assert.assertEquals( 1000, testMap.size() );
//
//    Integer previous = null;
//    for(Map.Entry<String, Integer> entry : testMap.entrySet()) {
//        Assert.assertNotNull( entry.getValue() );
//        if (previous != null) {
//            Assert.assertTrue( entry.getValue() >= previous );
//        }
//        previous = entry.getValue();
//    }    
//    
    
    /**
     * http://stackoverflow.com/posts/2581754/revisions
     * @param <K>
     * @param <V>
     * @param map
     * @return sorted map
     */
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
        
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    } 
     
//    String query = "SELECT video, shot, text, ts_rank(textsearch_" + /*lang +*/ ", query,16) * 1644.93 AS rank " +
//            " FROM hlf_search.tv_speech, to_tsquery('english', '" + /*textQuery +*/ "') query " +
//            " WHERE dataset="+ /*dataset +*/" AND query @@ textsearch_" + /*lang +*/
//            " ORDER BY rank DESC LIMIT 1000";     
}
