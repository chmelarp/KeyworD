/* 
 *  Class      : ProcessBean
 *  Created on : Apr 4, 2013
 *  Author     : chmelarp
 *  Copyright (C) 2013  Petr Chmelar
 */

package net.chmelab.beans;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession; 

import net.chmelab.kwdemo.Commons;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import net.chmelab.kwdemo.LanguageUtils;
import net.chmelab.kwdemo.StemMap;
import net.chmelab.kwdemo.WebUtils;
import net.chmelab.kwdemo.WordMap;

/**
 * Bean class related to main JSP request
 * @author chmelarp
 */
public class ProcessBean {
    public static final int MAXlength = 1024*1024;
    
    private Commons commons;
    private HttpServletRequest request;
    
    // user-specified JSP text (set on URL mode)
    private String text;
    // user-specified JSP language select (possibly set)
    private String language;
    // generated rest of the JSP page (keywords, gallery)
    private String page;     
    
    
    /**
     * Constructor
     */
    public ProcessBean () {
        this.commons = null;
        this.request = null;
        
        this.text = "";
        this.language = LanguageUtils.DEFAULTlanguage;
        this.page = "";
        // /mnt/data/datasets/ImageNet/www.image-net.org/api/text
    }

    
    /**
     * This is the main worker
     */ 
    public boolean main() {
        this.page = "";
        if (language.length() > 1024) language = language.substring(0, 1024);
        
        if (!this.text.isEmpty()) {
            Timestamp start = new Timestamp(new Date().getTime());
            if (text.length() > MAXlength) text = text.substring(0, MAXlength);
            
            // Process URL if present
            String meta = "";
            if (WebUtils.isValidURL(text.trim())) {
                this.page += "<p><b>URL</b>: &nbsp; "+ this.text +"</p>\n";
                String webPage = WebUtils.getFromURL(this.text);
                if (webPage != null && !webPage.isEmpty()) {
                    meta = WebUtils.getMetaTags(webPage);
                    this.page += "<p><b>Meta</b>: &nbsp; "+ meta +"</p>\n";
                    
                    this.text = WebUtils.getTextFromXHTML(webPage);
                }
                else {
                    this.page += "<p>Well, these Internets are restless seas...</p>\n";
                }
            }

            // Autodetect language
            String lastLanguage = commons.languageDetector.detectLanguage(meta + "\n" + this.text);
            if (this.language == null || this.language.compareTo(LanguageUtils.DEFAULTlanguage) == 0) { // Aboriginal (auto :)
                this.language = commons.languageDetector.getLanguage(lastLanguage);
            }
            
            // Process known languages only
            // StemMap keywords = null;
            if (this.language.compareTo("Dansk") == 0) {
                this.page += "<p><b>Danish keywords</b> (no images available)<br>\n";
                // keywords = commons.languageDetector.processKeywords(text, "da");
                this.page += commons.languageDetector.getKeywords(text, "da") +"</p>\n";
            }
            else if (this.language.compareTo("Deutsch") == 0) {
                this.page += "<p><b>German keywords</b> (no images available)<br>\n";
                // keywords = commons.languageDetector.processKeywords(text, "de");
                this.page += commons.languageDetector.getKeywords(text, "de") +"</p>\n";
            }
            else if (this.language.compareTo("English") == 0 || lastLanguage.compareTo("en") == 0) {
                this.page += "<p><b>English keywords</b> (images comming soon)<br>\n";
                // keywords = commons.languageDetector.processKeywords(text, "en");
                this.page += commons.languageDetector.getKeywords(text, "en") + "</p>\n";           
            }
            else {
                this.page += "<p>I don't understand the speech of your tribe :( just Dansk, Deutsch and English :)</p>\n";
            }
                    
            
            // Just FYI log the process
            try {
                // OWASP-safe query to log the request
                String insertQuery = "INSERT INTO keywordemo.requests(\"IP\", \"SID\", \"start\", \"end\", \"text\", \"keywords\", \"images\") "+
                                    " VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement stmt = commons.conn.prepareStatement(insertQuery);
                stmt.setString   (1, this.getRequest().getRemoteHost());
                stmt.setString   (2, this.getSession().getId());
                stmt.setTimestamp(3, start);
                stmt.setTimestamp(4, new Timestamp(new Date().getTime()));
                stmt.setString   (5, this.text);
                stmt.setString   (6, "<p>Detected language: "+ commons.languageDetector.getLastLanguage() + "</p>\n" + this.page); // TODO: main.getTsVector?
                stmt.setArray    (7, null);
                stmt.executeUpdate();

                if (stmt.getUpdateCount() < 0) commons.error(this, "INSERT failed :(");
            } catch (SQLException ex) {
                commons.logger.log(Level.SEVERE, null, ex);
            }
        }
       
        // return the best you can if something (not) happend
        if (this.text == null || this.text.isEmpty()) {
            this.page = "<p>Just roll in some text or http://www... :)</p>\n";
        }
        else if (this.page == null || this.page.isEmpty()) {
            this.page = "<p>Something really bad happend. Try to roll in more text or nicer http://www... :)</p>\n";
        }
        
        return true;
    }
    
    
    /**
     * @return 
     * <select name="language"> 
     *   <option>Autodetect</option>
     *   <option>C++</option>
     *   <option>Česky</option>
     *   <option>Dansk</option selected="true">
     *   <option>Deutsch</option>
     *   <option>English</option>
     *   <option>Español</option>
     *   <option>Français</option>
     *   <option>Italiano</option>
     *   <option>Nederlands</option>
     *   <option>Polski</option>
     *   <option>Português</option>
     *   <option>Svenska</option>
     *   <option>Türkçe</option>
     * </select>
     */
    public String languageSelect() {
        // Introduce the select
        String select = "                            language:";
        if (this.language != null && this.language.compareTo(LanguageUtils.DEFAULTlanguage) != 0 &&
            commons.languageDetector.getLastLanguage() != null && !commons.languageDetector.getLastLanguage().isEmpty() &&
            this.language.compareTo(commons.languageDetector.getLanguage(commons.languageDetector.getLastLanguage())) != 0) {
            select = "                            " + commons.languageDetector.getLanguage(commons.languageDetector.getLastLanguage()) + "? instead of:";
        }
        
        select += " <select name=\"language\"> ";
        Map<String, String> languages = commons.languageDetector.getLanguages();
        for (String local : languages.keySet()) {
            select += "<option";
            if (languages.get(local).compareTo(language) == 0) {
                select += " selected=\"true\"";
            }
            select += ">"+ languages.get(local) + "</option> ";
        }
        
        return select +"</select>";
    }
    

    /**
     * @return the rest of the page
     */
    public String getPage() {
        return this.page;
    }
    

    /**
     * @return the articleText
     */
    public String getText() {
        return text;
    }

    /**
     * @param text the articleText to set
     */
    public void setText(String text) {
        this.text = text;
    }
    

    /**
     * @return the request
     */
    public HttpServletRequest getRequest() {
        return request;
    }

    /**
     * @param request the request to set
     */
    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }
    
    
    /**
     * @return session
     */
    public HttpSession getSession() {
        return request.getSession();
    }

    
    /**
     * @return the commons even if don't exist
     */
    public Commons getCommons() {
        if (commons == null) commons = new Commons();
        return commons;
    }
    
    /**
     * @return the commons even if don't exist
     */
    public Commons getCommons(Locale agentLocale) {
        if (commons == null) commons = new Commons(agentLocale);
        return commons;
    }
    
        

    /**
     * @param commons the commons to set
     */
    public void setCommons(Commons commons) {
        this.commons = commons;
    }

    /**
     * @return the language
     */
    public String getLanguage() {
        return language;
    }

    
    /**
     * @param language the language to set
     */
    public void setLanguage(String language) {
        this.language = language;
    }
      
}
