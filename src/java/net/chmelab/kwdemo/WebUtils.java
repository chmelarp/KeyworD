/* 
 *  Class      : WebUtils
 *  Created on : Apr 5, 2013 (re-created)
 *  Author     : chmelarp
 *  Copyright (C) 2013  Petr Chmelar
 */

package net.chmelab.kwdemo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.FileNameMap;
import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection; 
import java.util.logging.Level;
import java.util.logging.Logger;

//ifdef pdfbox
//import org.apache.pdfbox.cos.COSDocument;
//import org.apache.pdfbox.pdfparser.PDFParser;
//import org.apache.pdfbox.pdmodel.PDDocument;
//import org.apache.pdfbox.util.PDFTextStripper;

import org.apache.commons.validator.routines.UrlValidator;

/**
 * A couple of mostly static HTTP and WWW utils
 * @author chmelarp
 */
public class WebUtils {
    public static final int MAXlength = 1024*1024;
    
    /**
     * URL Validation routine schemes = {"http", "https", "ftp"}
     *
     * @param urlString
     * @return valid
     */
    public static boolean isValidURL(String urlString) {
        UrlValidator urlValidator = new UrlValidator();
        if (urlValidator.isValid(urlString)) {
            return true;
        } else {
            return false;
        }
    }
    
    
/*  // Oracle sample
    <html lang="en-US" xmlns="http://www.w3.org/1999/xhtml">
        <head>
            <title>Lesson: Regular Expressions (The Java&trade; Tutorials &gt; Essential Classes)</title>
            <meta name="description" content="This Java tutorial describes exceptions, basic input/output, concurrency, regular expressions, and the platform environment" />
            <meta name="keywords" content="java programming, learn java, java sample code, java exception, java input output, java threads, java regex, regular expressions, path, classpath, environment variable" />
        
        <style type="text/css"> ... </style>
*/
    /**
     * 
     * @param text
     * @return 
     */
    public static String getMetaTags(String text) {
        // get meta keywords description
        String meta = "";
        int b, e;    // begin, end index
        int bodyLen = text.indexOf("</head>"); // header length
        if (bodyLen < 0) bodyLen = text.length();

        // get title
        b = text.indexOf("<title>");
        if (b >= 0 && b <= bodyLen) {
            e = text.indexOf("</title>", b);
            if (e > b && e < text.length()) meta += text.substring(b+7, e) + "<br>\n";
        }
        
        // get description
        b = text.indexOf("name=\"description\"");
        while (b >= 0 && b <= bodyLen) {
            b = text.indexOf("content=", b+20);
            b = text.indexOf("\"", b+8);
            e = text.indexOf("\"", b+1);
            if (e > b && e < text.length()) meta += text.substring(b+1, e) + "<br>\n";
            
            b = text.indexOf("name=\"description\"", b); // for a next round
        }
        
        // get keywords
        b = text.indexOf("name=\"keywords\"");
        while (b >= 0 && b <= bodyLen) {
            b = text.indexOf("content=", b+20);
            b = text.indexOf("\"", b+8);
            e = text.indexOf("\"", b+1);
            if (e > b && e < text.length()) meta += text.substring(b+1, e) + "<br>\n";
            
            b = text.indexOf("name=\"keywords\"", b); // for a next round
        }
        
        return meta.trim();
    }

    
    /**
     * Simplest ever XML/HTML parser
     * The file should be +-valid
     * Extract meta keywords and description first!
     * @param xhtmlString
     * @return text parsed
     * @throws IOException 
     */
    public static String getTextFromXHTML(String xhtmlString) {
        String parsedText = xhtmlString.replaceAll("<style.*?style>|<script.*?script>", "");
        parsedText = parsedText.replaceAll("<body|<h\\d|<p|<br|<hr|<li|<th|<tr"
                + "|<pre|<div|<caption|<code|<form|<figcaption|<footer|<header|"
                + "<iframe|<section|<menu|<nav|<noscript|<wbr", "\n<");         // make some culture :)
        parsedText = parsedText.replaceAll("<.*?>", " ");                       // all tags, no discussion
        parsedText = parsedText.replaceAll("[ \\t\\x0B\\f\\r]+", " ");          // == \s except \n :)
        parsedText = parsedText.replaceAll("\\n\\s", "\n");                     // trim margin spaces :(
        parsedText = parsedText.replaceAll("\\n+", "\n");                       // multiple \n except \n :)
        return parsedText;
    }
    


    public static String getFromURL(String urlString) {
        String content = "";
        int length = 0; // TODO: MAX
        BufferedReader reader = null;

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // conn.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
            conn.connect();
            
            // Map<String, List<String>> headerFields = conn.getHeaderFields();
            String contentEncoding = conn.getContentEncoding();
            if (contentEncoding == null) {              // there is just "Content-Type", never "Content-Encoding" :(
                String contentType = conn.getContentType();
                if (contentType.indexOf("=") > 0) {     // google.cz: "Content-Type => [text/html; charset=ISO-8859-2]"	
                    contentEncoding = contentType.split("=")[1];
                }
                else contentEncoding = "UTF-8"; // fall to default :(
            }
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), contentEncoding));

            String inputLine;
            while ((inputLine = reader.readLine()) != null && length < MAXlength) {
                length += inputLine.length();
                content += inputLine + " ";
//                System.out.println(inputLine);
            }
        } catch (IOException ex) {
            Logger.getLogger(WebUtils.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {            
                if (reader != null) reader.close();
            } catch (IOException ex) {
                Logger.getLogger(WebUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        if (content.isEmpty()) content = null;
        return content;
    }
    
        
    /**
     * Gets the MIME type for the specified file using the header field
     * A slow, but reliable variant
     * @param fileName
     * @return type MIME
     */
    public static String getMIMEfromHeader(String fileName) {
        String type = null;
        try {
            URL url = new URL(fileName);
            URLConnection uc = url.openConnection();
            type = uc.getContentType();
        } catch (IOException ex) {
            Logger.getLogger(WebUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return type;
    }
    
    /**
     * Gets the MIME type for the specified file name
     * A fast, but unreliable variant
     * @param fileName
     * @return type MIME
     */
    public static String getMIMEfromName(String fileName) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String type = fileNameMap.getContentTypeFor(fileName);
        return type;    
    }
    
    
    /**
     * getTextFromPDF
     *
     * @param fileName
     * @return parsed text
     *
    public static String getTextFromPDF(String fileName) {
        String parsedText = null;

        File pdfFile = new File(fileName);
        if (!pdfFile.isFile()) {
            Logger.getLogger(WebUtils.class.getName()).log(Level.SEVERE, null, "File " + fileName + " not found.");
            return null;
        }

        PDFParser parser;
        try {
            parser = new PDFParser(new FileInputStream(pdfFile));

            parser.parse();
            COSDocument cosDoc = parser.getDocument();
            PDFTextStripper pdfStripper = new PDFTextStripper();
            PDDocument pdDoc = new PDDocument(cosDoc);
            parsedText = pdfStripper.getText(pdDoc);
        } catch (IOException ex) {
            Logger.getLogger(WebUtils.class.getName()).log(Level.SEVERE, null, ex);
        }

        return parsedText;
    }
    */
}
