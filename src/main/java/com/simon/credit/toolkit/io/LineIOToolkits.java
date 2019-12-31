package com.simon.credit.toolkit.io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class LineIOToolkits {

	public static void main(String[] args) {
		FileWriter fw = null;
		try {
			fw = new FileWriter("d://test.txt", true);
			String line = "abs" + "\r\n";
			fw.write(line);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			IOToolkits.close(fw);
		}
	}

	public static final void writeLine(FileWriter fw, String line) throws IOException {
		fw.write(line);
	}

	/**
	 * 逐行读取文本
	 * @param filePath 文件路径
	 * @throws IOException
	 */
	public static final void readLineByLine(String filePath) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
		String line = null;
		for (line = br.readLine(); line != null; line = br.readLine()) {
			System.out.println(line);

		}
		br.close();
	}

}