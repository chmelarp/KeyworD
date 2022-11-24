/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tartarus.snowball.ext;

import java.text.Normalizer;

/**
 *
 * @author chmelarp
 */
public class czechStemmer extends org.tartarus.snowball.SnowballStemmer {

    @Override
    public boolean stem() {
        /**
         * Works only in case of UTF-8!
         */
        String decomposed = java.text.Normalizer.normalize(this.getCurrent(), Normalizer.Form.NFD);
        decomposed.replaceAll("\\\\p{InCombiningDiacriticalMarks}+", "");
        this.setCurrent(decomposed);
        
        return true;
    }

}
