package com.simon.credit.toolkit;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import com.simon.credit.toolkit.common.CommonToolkits;
import com.simon.credit.toolkit.io.IOToolkits;
import com.simon.credit.toolkit.io.LineIterator;

/**
 * 逐行读(行迭代)测试
 * @author XUZIMING 2018-06-25
 */
public class LineIteratorTest {

	public static void main(String[] args) throws Exception {
		File file = new File("d:/credit_riskmanage.sql");
		InputStream input = null;
		LineIterator iterator = null;
		try {
			input = new FileInputStream(file);
			iterator = IOToolkits.lineIterator(input, CommonToolkits.UTF8);
			while (iterator.hasNext()) {
				String line = iterator.nextLine();
				System.out.println(line);
			}
			// 关闭资源
			iterator.close();
		} finally {
			if (iterator != null) {
				iterator.close();
			}
			IOToolkits.close(input);
		}
	}

}
