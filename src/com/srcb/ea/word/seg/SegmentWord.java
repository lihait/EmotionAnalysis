package com.srcb.ea.word.seg;

import java.io.File;
import java.util.HashSet;
import java.util.List;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;
import com.huaban.analysis.jieba.WordDictionary;
import com.huaban.analysis.jieba.JiebaSegmenter.SegMode;
import com.srcb.ea.conf.Config;

/**
 * 使用jieba开源分词工具分词
 * 
 * @author srcb04161
 * 
 */
public class SegmentWord {

	private static JiebaSegmenter segmenter = null;
	
	public static HashSet<String> wordset = new HashSet<String>();

	public static void init() {
		if (segmenter == null) {
			String userPath = Config.SEG_PATH;
			System.out.println(userPath);
			WordDictionary.getInstance().init(new File(userPath));
			segmenter = new JiebaSegmenter();

		}
	}

	public static String[] segment(String sentence) {
		init();
		List<SegToken> words = segmenter.process(sentence, SegMode.SEARCH);
		int iSize = words.size();
		String[] word = new String[iSize];
		for (int i = 0; i < iSize; i++) {
			SegToken seg = words.get(i);
			word[i] = sentence.substring(seg.startOffset, seg.endOffset);
		}
		
		return word;
	}

}
