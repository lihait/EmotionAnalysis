package com.srcb.ea.feature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import com.srcb.ea.conf.Config;
import com.srcb.ea.dict.WordDict;
import com.srcb.ea.parser.StanfordParserStr;

import edu.stanford.nlp.trees.TypedDependency;

/**
 * 抽取特征词
 * 
 * @author srcb04161
 * 
 */
public class ExtractFeatureWord {

	public static ArrayList<ArrayList<String>> extractFeatureWord(
			String[] wordList, StanfordParserStr gsT) {

		ArrayList<ArrayList<String>> featureList = new ArrayList<ArrayList<String>>();

		isOpinion(wordList, featureList);

		generateDependencyRelated(featureList, gsT);
		// 8 - 判断该词是否是特征词
		for (ArrayList<String> wordlist : featureList) {

			//去除停用词
			if (WordDict.stopDict.contains(wordlist.get(0))) {

				wordlist.add("-1");

				continue;

			}

			//是否在特征词库
			if (isInFeatureDict(wordlist.get(0)).equalsIgnoreCase("1")) {

				wordlist.add("1");

			} else {

				//词性是否为名词
				if (wordlist.get(2).equalsIgnoreCase("0")) {

					wordlist.add("0");

				} else {

					//附近是否有情感词
					if (wordlist.get(3).equalsIgnoreCase("0")) {

						wordlist.add("0");

					} else if(wordlist.get(1).equalsIgnoreCase("0") && wordlist.get(6).equalsIgnoreCase("1")){
						
							wordlist.add("1");

					}else{
						
						wordlist.add("0");
						
					}

				}

			}

		}

//		for (int i = 0; i < featureList.size(); i++) {
//
//			System.out.println(featureList.get(i).get(0) + ":"
//					+ featureList.get(i).get(1) + ":"
//					+ featureList.get(i).get(2) + ":"
//					+ featureList.get(i).get(3) + ":"
//					+ featureList.get(i).get(4) + ":"
//					+ featureList.get(i).get(5) + ":"
//					+ featureList.get(i).get(6));
//		}

		return featureList;

	}

	// 1 - 查情感词库，获取该词情感极性
	public static void isOpinion(String[] wordList,
			ArrayList<ArrayList<String>> featureList) {

		for (int i = 0; i < wordList.length; i++) {

			ArrayList<String> featureResult = new ArrayList<String>();

			if (WordDict.posDict.contains(wordList[i])) {

				featureResult.add(wordList[i]);

				featureResult.add("1");

			} else if (WordDict.negDict.contains(wordList[i])) {

				featureResult.add(wordList[i]);

				featureResult.add("-1");

			} else {

				featureResult.add(wordList[i]);

				featureResult.add("0");
			}

			featureList.add(featureResult);
		}

	}

	// 查特征词库，判断该词是否在特征词库中
	public static String isInFeatureDict(String word) {

		if (WordDict.featureDict.contains(word)) {

			return "1";

		}

		return "0";

	}

	// 根据词性标注和句法分析的结果提取词性和判断词附近是否有情感词
	public static void generateDependencyRelated(
			ArrayList<ArrayList<String>> featureList, StanfordParserStr gsT) {

		Collection<TypedDependency> tdl = gsT.gs.allTypedDependencies();// cgs.allTypedDependencies();

		Object[] tdArray = (Object[]) tdl.toArray();

		HashSet<Integer> indexOfOpinion = new HashSet<Integer>();
		// 提取附近情感词
		for (int i = 0; i < featureList.size(); i++) {

			ArrayList<String> wordlist = featureList.get(i);

			String isOpinin = wordlist.get(1);

			if (isOpinin.equalsIgnoreCase("0")) {

				continue;

			}

			indexOfOpinion.add(i);
		}
		// 2 - 提取词性并判断是否为名词
		for (int i = 0; i < featureList.size(); i++) {
			ArrayList<String> wordlist = featureList.get(i);

			TypedDependency td = (TypedDependency) tdArray[i];

			String posTag = td.dep().tag();

			if (posTag.equalsIgnoreCase("NN") || posTag.equalsIgnoreCase("NR")
					|| posTag.equalsIgnoreCase("PN")) {

				wordlist.add("1");

			} else {

				wordlist.add("0");

			}
		}

		// 判断该词附近是否有情感词
		for (int i = 0; i < tdl.size(); i++) {

			ArrayList<String> wordlist = featureList.get(i);

			TypedDependency td = (TypedDependency) tdArray[i];

			int depindex = td.dep().index();

			// System.out.println("depindex:" + depindex);
			// 修改与原SA不同
			// 根据依存句法分析结果，获取该词附近的词，并判断是否是情感词
			int parentIndex = td.gov().index() - 1;

			// int currentIndex = td.gov().index();

			// int childIndex = td.gov().index() + 1;

			// System.out.println("parentIndex:" + parentIndex);
			// 3 - isDependentToOpinion
			if (indexOfOpinion.contains(parentIndex)) {

				wordlist.add("1");

			} else {

				wordlist.add("0");

			}
			// 4 - 附近情感词位置 parentIndex
			wordlist.add(String.valueOf(parentIndex));
			// 5 - dependencyToParent
			wordlist.add(td.reln().toString());

		}

		HashSet<Integer> dependentByOpinion = new HashSet<Integer>();

		for (Integer o : indexOfOpinion) {
			ArrayList<String> line = featureList.get(o);
			dependentByOpinion.add(Integer.parseInt(line.get(4)));
		}

		for (int i = 0; i < featureList.size(); i++) {
			ArrayList<String> line = featureList.get(i);
			// 6 - isDependentByOpinion
			if (dependentByOpinion.contains(i)) {
				line.add("1");
			} else {
				line.add("0");
			}

			// 7 - isDependentToOpinion or isDependentByOpinion or
			// isDependentToRootToOpinion
			if (line.get(3).equalsIgnoreCase("1")) {
				line.add("1");
			} else {
				if (dependentByOpinion.contains(i)) {
					line.add("1");
				} else {
					int parent = Integer.parseInt(line.get(4));
					if (parent == -1) {
						line.add("0");
					} else {
						if (featureList.get(parent).get(5)
								.equalsIgnoreCase("root")
								&& dependentByOpinion.contains(parent)) {
							line.add("1");
						} else {
							line.add("0");
						}
					}
				}
			}
		}

	}

}
