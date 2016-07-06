package com.srcb.ea.parser;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.stanford.nlp.graph.Graph;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.CollocationFinder;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.trees.international.pennchinese.ChineseGrammaticalStructure;

/**
 * 使用stanford-parser进行词性标注和依存句法分析
 * 
 * @author srcb04161
 * 
 */
public class StanfordParser {

	public static String modelPath = "edu/stanford/nlp/models/parser/nndep/CTB_CoNLL_params.txt.gz";
	public static String taggerPath = "conf/models/chinese-nodistsim.tagger";
	public static String lexiPath = "edu/stanford/nlp/models/lexparser/chineseFactored.ser.gz";

	public static MaxentTagger tagger = null;
	public static DependencyParser parser = null;
	public static LexicalizedParser lp = null;

	public static int countParserTimes = 0;

	public static void initParser(boolean bLex) {
		if (tagger == null) {
			tagger = new MaxentTagger(taggerPath);
		}
		if (parser == null) {
			parser = DependencyParser.loadFromModelFile(modelPath);
		}
		if (bLex && lp == null) {
			// String[] options = {"-maxLength", "5000", "-MAX_ITEMS","500000"};
			String[] options = { "-maxLength", "5000", "-MAX_ITEMS", "500000",
					"-fastFactored" };
			lp = LexicalizedParser.loadModel(lexiPath, options);
		}
	}

	public static void unInitParser() {
		tagger = null;
		parser = null;
		lp = null;
		countParserTimes = 0;
	}

	public static Map<Integer, String> getPhraseTag(List<HasWord> sentence) {
		if (tagger == null || parser == null || sentence == null || lp == null) {
			return null;
		}
		Map<Integer, String> phraseTag = new HashMap<Integer, String>();

		Tree parse = null;
		try {
			parse = lp.apply(sentence);
		} catch (Exception e) {
			e.printStackTrace();
			return phraseTag;
		}

		if (parse == null) {
			return phraseTag;
		}

		List<String> keyValue = new ArrayList<String>();
		keyValue = parserTree(parse, keyValue, null, null);
		// parse.pennPrint();

		for (int i = 0; i < keyValue.size(); i++) {
			phraseTag.put(i + 1, keyValue.get(i));
		}
		return phraseTag;
	}

	public static GrammaticalStructure ParserWord(List<HasWord> sentence) {
		if (tagger == null || parser == null || sentence == null) {
			return null;
		}

		List<TaggedWord> tagged = tagger.tagSentence(sentence);
		GrammaticalStructure gs = parser.predict(tagged);
		System.err.println(gs);

		return gs;
	}

	public static List<HasWord> getWordList(String[] arrWord) {
		if (arrWord == null || arrWord.length == 0) {
			return null;
		}
		List<HasWord> sentence = new ArrayList<HasWord>();
		for (int i = 0; i < arrWord.length; i++) {
			HasWord hWord = new Word();
			hWord.setWord(arrWord[i]);
			sentence.add(hWord);
		}
		return sentence;
	}

	private static List<String> parserTree(Tree parse, List<String> keyValue,
			String grandpa, String father) {
		Tree[] children = parse.children();
		int ilen = children.length;
		for (Tree child : children) {
			// System.out.println(child.value());
			keyValue = parserTree(child, keyValue, father, parse.value());
		}
		if (ilen == 0) {
			// System.out.println(parse.value()+":"+grandpa);
			keyValue.add(grandpa);
		}
		return keyValue;
	}

	public static GrammaticalStructure parse(String[] wordList) {
		initParser(false);
		return ParserWord(getWordList(wordList));
	}

	public static StanfordParserStr parseOnly(String[] wordList) {
		countParserTimes++;
		if (countParserTimes >= 3000) {
			unInitParser();
		}

		StanfordParserStr parseTag = new StanfordParserStr();
		initParser(false);
		List<HasWord> sentence = getWordList(wordList);
		parseTag.gs = ParserWord(sentence);
		parseTag.phraseTag = new HashMap<Integer, String>();

		return parseTag;
	}

	public static StanfordParserStr parseAndLexi(String[] wordList) {
		countParserTimes++;
		if (countParserTimes >= 3000) {
			unInitParser();
		}

		StanfordParserStr parseTag = new StanfordParserStr();
		initParser(true);
		List<HasWord> sentence = getWordList(wordList);
		parseTag.gs = ParserWord(sentence);
		parseTag.phraseTag = getPhraseTag(sentence);

		return parseTag;
	}
}
