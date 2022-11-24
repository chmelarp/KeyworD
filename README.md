# KeyworD
Automatic detection of 12+ languages and (bigram) keyword extraction in English, German and Dansk

## Technology

  * There are generated up to 20 resulting keywords or bigrams according to the text size and word probabilities. If there are less then 20 words or 100 characters, algorithms may not work properly.
    Dansk, Deutsch and English only is supported at the moment.
  * The automatic language detection of Czech, Danish, Dutch, English, French, German, Italian, Polish, Portuguese, Spanish, Swedish, Turkish works well for larger documents (pages). It is based on:
    1.  (100+) most frequent words in each language (stop words)
    2.  Letter frequency (77*2 letters)
    3.  Client's agent locale (browser settings; NOT USED for testing purposes)
    Non-latin alphabets (Hebrew, Russian, Korean, ...) and programming languages (C++ Java, Python, shell, ...) will be trivial then.
  * Automatic detection of URL, HTML and XML transformation to text.

 
## TODO

  * If there is just an URL, avoid navigation (bunch of links?).
  * If text less then 100 characters (or 20 words), use dictionaries to match best language by each word.
  * Do not forget C++ Java, Python and shell :)
  * Process ODT and DOCx documents (XML).
  * Use pdfbox-app-1.8.0.jar to extract text from PDF.
  * Make lists of countries (states), cities according to IATA airport codes; employ GeoIP (for locale check too).
  * Make lists of companies, brands - cars, IT, chemical-pharma, food and restaurants, banks -> currencies.
  * Make lists of names (first and surnames) -> countries (+ connect all together).
  * Use BabelNet to translate all to English; topic classification by WordNet distance needed.
  * Join BabelNet to add Danish (and Czech :) languages.
  * Learn word (and letter) probabilities from NLP corpuses.
  * Employ user feedback by similarity search (keyword and image features).

## License

    Copyright (C) 2013-2022  Petr Chmelar

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.