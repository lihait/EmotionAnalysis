package com.srcb.ea.ep;

import java.util.ArrayList;
import java.util.List;

import com.srcb.ea.dict.WordDict;

/**
 * 特征词极性标注
 * 
 * @author srcb04161
 * 
 */
public class ExtractPolarity {

	public static ArrayList<ArrayList<String>> extractPolarity(
			ArrayList<ArrayList<String>> featureList) {

		if (featureList.size() < 2) {

			return featureList;

		}

		List<String> POList = new ArrayList<String>();

		List<String> FFList = new ArrayList<String>();

		for (ArrayList<String> wordlist : featureList) {

			if (!wordlist.get(1).equalsIgnoreCase("0")) {

				POList.add(wordlist.get(0));

			}

			if (wordlist.get(8).equalsIgnoreCase("1")) {

				FFList.add(wordlist.get(0));

			}

		}
		// 没有抽出特征词或者情感词则直接返回
		if (POList.size() == 0 || FFList.size() == 0) {
			return featureList;
		}
		for (ArrayList<String> wordlist : featureList) {
			
			String iOpinion = null;			
			int iOpinionIdx = 0;
			// 判断是否是特征词及附近是否有情感词
			if (wordlist.get(8).equalsIgnoreCase("1")
					&& wordlist.get(3).equalsIgnoreCase("1")) {
				
				// 获取该词附近的情感词位置
				iOpinionIdx = Integer.parseInt(wordlist.get(4));
				
				// 获取情感词极性
				iOpinion = featureList.get(iOpinionIdx).get(1);

			} else if (wordlist.get(8).equalsIgnoreCase("1")
					&& wordlist.get(3).equalsIgnoreCase("0")) {

				// 如果第3位 不是1， 则判断第6位
				if (wordlist.get(6).equals("1")) {

					for (int i = 0; i < featureList.size(); i++) {
						
						if (i == featureList.indexOf(wordlist)) {
							
							continue;
							
						}
						if (!featureList.get(i).get(1).equals("0")
								&& Integer.parseInt(featureList.get(i).get(4)) == featureList
										.indexOf(wordlist)) {
							
							iOpinionIdx = i;
							
							iOpinion = featureList.get(i).get(1);
							
							break;
						}
					}
				} else if (wordlist.get(7).equals("1")) {
					
					int id = Integer.parseInt(wordlist.get(4));
					
					int num = featureList.size();
					
					for (int i = 0; i < featureList.size(); i++) {
						
						if (i == featureList.indexOf(wordlist)) {
							
							continue;
							
						}
						// 取离当前特征词最近的一个
						if (!featureList.get(i).get(1).equals("0")
								&& Integer.parseInt(featureList.get(i).get(4)) == id
								&& Math.abs(i - featureList.indexOf(wordlist)) <= num) {
							
							iOpinionIdx = i;
							
							iOpinion = featureList.get(i).get(1);
							
							num = Math.abs(i - featureList.indexOf(wordlist));
						}
					}
				}else{
					
					iOpinion = "-2";
					
				}
				
			}else{
				
				iOpinion = "-2";
				
			} 
			// 判断是否存在情感逆转
			if (!iOpinion.equalsIgnoreCase("0") && !iOpinion.equalsIgnoreCase("-2")) {

				int iOpinion2 = Integer.parseInt(iOpinion);

				// 依赖neg
				if (featureList.get(iOpinionIdx).get(5).equalsIgnoreCase("NEG")) {
					iOpinion2 = iOpinion2 * (-1);
				} else {

					boolean bFind = false;

					do {
						// 被neg依赖
						for (int i = 0; i < featureList.size(); i++) {
							if (featureList.get(i).get(8).equalsIgnoreCase("1")
									&& featureList.get(i).get(5)
											.equalsIgnoreCase("NEG")
									&& featureList
											.get(i)
											.get(4)
											.equals(String.valueOf(iOpinionIdx))) {
								bFind = true;
								iOpinion2 = iOpinion2 * (-1);
								break;
							}
						}
						if (bFind) {
							break;
						}

						// 在窗口找否定
						for (int i = iOpinionIdx; i >= 0
								&& i > (iOpinionIdx - 4); i--) {
							if (featureList.get(i).get(2)
									.equalsIgnoreCase("PU")) {
								break;
							}
							if (IsNEG(featureList.get(i).get(0))) {
								bFind = true;
								iOpinion2 = iOpinion2 * (-1);
								break;
							}
						}
						if (bFind) {
							break;
						}

						for (int i = iOpinionIdx; i < featureList.size()
								&& i < (iOpinionIdx + 4); i++) {
							if (featureList.get(i).get(2)
									.equalsIgnoreCase("PU")) {
								break;
							}
							if (IsNEG(featureList.get(i).get(0))) {
								bFind = true;
								iOpinion2 = iOpinion2 * (-1);
								break;
							}
						}

					} while (false);

				}

				iOpinion = String.valueOf(iOpinion2);

			}

			if(iOpinion == null){				
				
				iOpinion = "-2";
				
			}
			
			wordlist.add(iOpinion);

		}

		return featureList;

	}

	private static boolean IsNEG(String string) {
		// TODO Auto-generated method stub
		if (WordDict.negationDict.contains(string)) {

			return true;

		}

		return false;
	}

}
