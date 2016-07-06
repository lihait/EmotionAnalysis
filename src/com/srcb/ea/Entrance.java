package com.srcb.ea;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import com.srcb.ea.ep.ExtractPolarity;
import com.srcb.ea.feature.ExtractFeatureWord;
import com.srcb.ea.parser.StanfordParser;
import com.srcb.ea.parser.StanfordParserStr;
import com.srcb.ea.word.seg.SegmentWord;

/**
 * 主函数
 * 
 * @author srcb04161
 * 
 */
public class Entrance {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		if (args.length < 2) {

			System.out.println("参数输入错误！");

			System.exit(0);

		}

		String filepath = args[0];
		// 获取句子内容
		ArrayList<String[]> SentenceSet = readTxtFile(filepath);

		// 存储最终结果
		ArrayList<String> result = new ArrayList<String>();

		if (!(SentenceSet.isEmpty())) {

			for (String[] sentence : SentenceSet) {

				String SenID = sentence[0];

				String sentenceContent = sentence[1];

				// 处理无效句子（长度大于100个字的句子等同于无效）
				if (sentenceContent.isEmpty()
						|| sentenceContent.length() >= 100) {

					result.add(SenID + "\t" + "invalid" + "\t" + "invalid");

				} else {

					// 使用jieba分词工具对句子分词
					String[] wordList = SegmentWord.segment(sentenceContent);

					// 使用stanford-parser对分好的词做词性标注和依存句法分析
					StanfordParserStr gsT = StanfordParser
							.parseAndLexi(wordList);
					// 抽取特征词
					ArrayList<ArrayList<String>> featureList = ExtractFeatureWord
							.extractFeatureWord(wordList, gsT);
					// 极性标注
					ArrayList<ArrayList<String>> featureResult = ExtractPolarity
							.extractPolarity(featureList);
					// 保存结果
					for (ArrayList<String> resultList : featureResult) {

						if (resultList.get(8).equalsIgnoreCase("1") && !resultList.get(9).equalsIgnoreCase("-2")) {

							String resultStr = SenID + "\t" + resultList.get(0)
									+ "\t" + resultList.get(9);

							System.out.println(resultStr);

							result.add(resultStr);

						}

					}

				}

			}

		}
		
		try {

			File file = new File(args[0]);

			if (!(file.isFile() && file.exists())) {
				
				System.out.println("文件不存在！");
				
				System.exit(0);
				
			}

			String fileName = file.getName();
			
			String userPath = file.getAbsolutePath() + File.separatorChar;
			
			userPath = userPath.substring(0,
					userPath.length() - fileName.length() - 1);

			//将结果保存到本地文件
			BufferedWriter writer = new BufferedWriter(new FileWriter(userPath
					+ args[1] + ".txt"));

			for (int i = 0; i < result.size(); i++) {

				writer.write(result.get(i));
				
				writer.newLine();

			}

			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println(result.size());

	}

	/*
	 * 读取文件内容
	 */
	public static ArrayList<String[]> readTxtFile(String filePath) {

		ArrayList<String[]> SentenceSet = new ArrayList<String[]>();

		try {

			String encoding = "utf8";

			File file = new File(filePath);

			if (file.isFile() && file.exists()) { // 判断文件是否存在

				InputStreamReader read = new InputStreamReader(
						new FileInputStream(file), encoding);// 设定文件格式

				BufferedReader bufferedReader = new BufferedReader(read);

				String lineTxt = null;

				while ((lineTxt = bufferedReader.readLine()) != null) {

					String[] result = lineTxt.split("\t");

					SentenceSet.add(result);

				}

				read.close();

			} else {

				System.out.println("文件不存在！");

			}
		} catch (Exception e) {

			System.out.println("读取文件内容出错！");

			e.printStackTrace();

		}
		return SentenceSet;

	}

}
