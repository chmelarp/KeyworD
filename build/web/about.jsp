<%-- 
    Document   : about
    Created on : Apr 4, 2013
    Author     : chmelarp
    Copyright (C) 2013  Petr Chmelar
--%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="Copyright" content="(C) 2013  Petr Chmelar">
        <link rel="stylesheet" type="text/css" href="style.css">
        <title>KeyworDemo</title>
    </head>
    <body>
        <div style="float:right;"><a href="index.jsp">back</a></div>

        <h3>&nbsp;<a href="index.jsp">KeyworDemo</a></h3>
        <p><pre>
    Copyright (C) 2013  Petr Chmelar

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see &lt;http://www.gnu.org/licenses/&gt;.
        </pre></p>
        <p>&nbsp; Additional rights may apply. <a href="sources.tar.bz2">Download</a> 
            the source code to see copyright notices. 
        </p>
        <p>&nbsp;</p>
        
        <h3>&nbsp;Technology</h3>  
        <ul>
            <li>There are generated up to 20 resulting keywords or 
                <a href="http://en.wikipedia.org/wiki/Bigram">bigrams</a>
                according to the text size and word probabilities. 
                If there are less then 20 words or 100 characters, algorithms
                may not work properly.<br>
                Dansk, Deutsch and English only is supported at the moment.
            </li>
            <li>The automatic language detection of Czech, Danish, Dutch, 
                English, French, German, Italian, Polish, Portuguese, Spanish, 
                Swedish, Turkish works well for larger documents (pages).
                It is based on:
                <ul>
                    <li>(100+) most frequent words in each language 
                        (<a href="https://en.wikipedia.org/wiki/Stop_word" target="_blank">stop words</a>)</li>
                    <li><a href="https://en.wikipedia.org/wiki/Letter_frequency" target="_blank">Letter frequency</a> (77*2 letters)</li>
                    <li>Client's agent <a href="https://en.wikipedia.org/wiki/Locale" target="_blank">locale</a> 
                        (browser settings; NOT USED for testing purposes)</li>
                </ul>
                Non-latin alphabets (Hebrew, Russian, Korean, ...) and programming 
                languages (C++ Java, Python, shell, ...) will be trivial then.
            </li>
            <li>Automatic detection of URL, HTML and XML transformation to text.</li>
        </ul>

        <p>&nbsp;</p>
        
        <h3>&nbsp;TODO</h3>        
        <ul>
            <li>If there is just an URL, avoid navigation (bunch of links?).</li>
            <li>If text less then 100 characters (or 20 words), use dictionaries 
                to match best language by each word.</li>
            <li>Do not forget C++ Java, Python and shell :)</li>
            <li>Process ODT and DOCx documents (XML).</li>
            <li>Use pdfbox-app-1.8.0.jar to extract text from PDF.</li>
            <li>Make lists of countries (states), cities according to IATA airport codes; 
                employ GeoIP (for locale check too).</li>
            <li>Make lists of companies, brands - cars, IT, chemical-pharma, 
                food and restaurants, banks -> currencies.</li>
            <li>Make lists of names (first and surnames) -> countries (+ connect all together).</li>
            <li>Use BabelNet to translate all to English; topic classification by 
                WordNet distance needed.</li>
            <li>Join BabelNet to add Danish (and Czech :) languages.</li>
            <li>Learn word (and letter) probabilities from NLP corpuses.</li>
            <li>Employ user feedback by similarity search (keyword and image features).</li>
        </ul>

    </body>
</html>
