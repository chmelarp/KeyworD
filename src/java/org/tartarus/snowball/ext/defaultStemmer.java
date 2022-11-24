/*
 * Stemmer that doesn't stemm (for default language)
 * @author chmelarp
 */

package org.tartarus.snowball.ext;

/**
 *
 * @author petr
 */
public class defaultStemmer extends org.tartarus.snowball.SnowballStemmer {

    @Override
    public boolean stem() {
        // NOOP
        return true;
    }

}
