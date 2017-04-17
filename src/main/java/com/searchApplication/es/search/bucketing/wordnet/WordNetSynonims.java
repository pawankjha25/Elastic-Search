package com.searchApplication.es.search.bucketing.wordnet;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.searchApplication.es.search.bucketing.AttributeBucketer;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.morph.WordnetStemmer;

public class WordNetSynonims {

	private IDictionary dict;
	private WordnetStemmer stemmer;
	private static String path = "data/dict";
	private static final Logger LOGGER = LoggerFactory.getLogger(WordNetSynonims.class);

	private static final WordNetSynonims INSTANCE = new WordNetSynonims();

	public static WordNetSynonims fromPath() {
		return new WordNetSynonims();
	}

	public static WordNetSynonims getSingleton() {
		return INSTANCE;
	}

	private WordNetSynonims() {
		try {
			URL url = new URL("file", null, path);
			this.dict = new Dictionary(url);
			dict.open();
			this.stemmer = new WordnetStemmer(dict);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<String> testDictionary(String query) throws IOException {
		List<String> synonims = new ArrayList<String>();
		try {
			query = stemmer.findStems(query, POS.NOUN).get(0);
		} catch (Exception e) {
			query = "";
		}
		try {
			IIndexWord idxWord = dict.getIndexWord(query, POS.NOUN);
			for (IWordID wordID : idxWord.getWordIDs()) {
				IWord word = dict.getWord(wordID);
				ISynset synset = word.getSynset();
				for (IWord w : synset.getWords())
					if (!w.getLemma().equals(query) && !synonims.contains(w.getLemma())) {
						synonims.add(w.getLemma());
					}
			}
		} catch (Exception e) {
			LOGGER.info("No synonims for {}", query);
		}
		return synonims;
	}

}
