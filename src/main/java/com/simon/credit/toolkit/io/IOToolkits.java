package com.simon.credit.toolkit.io;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * 读写工具类
 * @author XUZIMING 2016-10-11
 */
public class IOToolkits {

	public static final String UTF8 = "UTF-8";

	public static final String readFile(File file) throws IOException {
		InputStream input = null;
		try {
			input = openInputStream(file);
			return toString(input);
		} finally {
			close(input);
		}
	}

	/**
	 * 将输入流数据转为字符串
	 * @param input 输入流
	 */
	public static final String toString(InputStream input) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			copy(input, out);
			return new String(out.toByteArray(), UTF8);
		} finally {
			close(out);
		}
	}

	/**
	 * 拷贝文件
	 * @param srcFile 源文件
	 * @param destFile 目标文件
	 * @throws IOException
	 */
	public static final void copy(File srcFile, File destFile) throws IOException {
		copy(new FileInputStream(srcFile), destFile);
	}

	/**
	 * 拷贝文件(自定义256K字节缓存区)
	 * @param in 输入流
	 * @param out 输出流
	 * @throws IOException
	 */
	public static final void copy(InputStream in, OutputStream out) throws IOException {
		try {
			byte[] buffer = new byte[256 * 1024 * 1]; // 256k缓冲区
			int sum = 0;// 记录实际一次读取了多少字节
			while ((sum = in.read(buffer)) != -1) {
				out.write(buffer, 0, sum);
			}
			// 强制刷出缓冲区数据
			out.flush();
		} finally {
			// 关闭输入、输出流资源
			close(in, out);
		}
	}

	/**
	 * 拷贝文件(自定义1M缓存区)
	 * @param in 输入流
	 * @param destFile 目标文件
	 * @throws IOException
	 */
	public static final void copy(InputStream in, File destFile) throws IOException {
		copy(in, new FileOutputStream(destFile));
	}

	/**
	 * 关闭资源
	 * @param resources 资源
	 */
	public static final void close(AutoCloseable... resources) {
		if (resources == null || resources.length == 0) {
			return;
		}

		for (AutoCloseable resource : resources) {
			if (resource != null) {
				try {
					resource.close();
				} catch (Exception e) {
					// ignore
				}
			}
		}
	}

	/**
	 * 打印流数据
	 * @param input
	 */
	public static final void print(InputStream input) {
		print(input, false);// 打印完不关闭流
	}

	/**
	 * 打印流数据
	 * @param input 输入流
	 * @param autoClose 自动关闭
	 */
	public static final void print(InputStream input, boolean autoClose) {
		if (input == null) {
			return;
		}

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(input, UTF8));
			String line = null;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}
		} catch (UnsupportedEncodingException e) {
			// ignore
		} catch (IOException e) {
			// ignore
		} finally {
			if (autoClose) {
				close(input);
			}
		}
	}

	/**
	 * 从输入流读取数据
	 * @param input 输入流
	 * @return 字节数组形式的数据
	 * @throws IOException
	 */
	public static byte[] toByteArray(InputStream input) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		copy(input, output);
		return output.toByteArray();
	}

	/**
	 * 行迭代
	 * @param file 数据文件
	 * @return 行迭代游标
	 * @throws IOException
	 */
	public static LineIterator lineIterator(File file) throws IOException {
		return lineIterator(file, UTF8);
	}

	/**
	 * 行迭代
	 * @param file 数据文件
	 * @param encoding 文件编码
	 * @return 行迭代游标
	 * @throws IOException
	 */
	public static LineIterator lineIterator(File file, String encoding) throws IOException {
		InputStream input = null;
		try {
			input = openInputStream(file);
			return lineIterator(input, encoding);
		} catch (IOException ioe) {
			close(input);
			throw ioe;
		} catch (RuntimeException re) {
			close(input);
			throw re;
		}
	}

	/**
	 * 获取输入流
	 * @param file 数据文件
	 * @return 文件输入流
	 * @throws IOException 文件不存在| file为目录| 文件不可读
	 */
	public static FileInputStream openInputStream(File file) throws IOException {
		if (file == null || !file.exists()) {
			throw new FileNotFoundException("File '" + file + "' does not exist");
		}

		if (file.isDirectory()) {
			throw new IOException("File '" + file + "' exists but is a directory");
		}

		if (!file.canRead()) {
			throw new IOException("File '" + file + "' cannot be read");
		}

		return new FileInputStream(file);
	}

	/**
	 * 行迭代(适用于大文件,防止一次性读取发生OOM)
	 * @param input 输入流
	 * @return 行迭代器
	 * @throws IOException
	 */
	public static LineIterator lineIterator(InputStream input) throws IOException {
		return lineIterator(input, UTF8);
	}

	/**
	 * 行迭代(适用于大文件,防止一次性读取发生OOM)
	 * @param input 输入流
	 * @param encoding 字符编码
	 * @return
	 * @throws IOException
	 */
	public static LineIterator lineIterator(InputStream input, String encoding) throws IOException {
		if (encoding == null) {
			return new LineIterator(new InputStreamReader(input));
		}

		return new LineIterator(new InputStreamReader(input, encoding));
	}

	public static <T> List<T> readLines(String filePath) {
		return readLines(new File(filePath), UTF8);
	}

	public static <T> List<T> readLines(File file) {
		return readLines(file, UTF8);
	}

	public static <T> List<T> readLines(String filePath, String encoding) {
		return readLines(new File(filePath), encoding);
	}

	public static <T> List<T> readLines(File file, String encoding) {
		List<T> list = new ArrayList<T>();
		InputStream input = null;
		LineIterator lineIterator = null;

		try {
			input = openInputStream(file);
			lineIterator = lineIterator(file, encoding);
			while (lineIterator.hasNext()) {
				@SuppressWarnings("unchecked")
				T line = (T) lineIterator.next();
				list.add(line);
			}
		} catch (IOException ioe) {
			// ignore
		} finally {
			LineIterator.closeQuietly(lineIterator);
			close(input);
		}
		return list;
    }

}
