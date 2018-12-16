package com.simon.credit.toolkit.io;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 控制台工具类
 * @author XUZIMING 2018-06-11
 */
public class ConsoleToolkits {

	/** 控制台读取器 */
	private static final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

	/**
	 * 使用系统的输入流，从控制台中读取数据<br/>
	 * 用于所用的JDK版本
	 * @param prompt 提示信息
	 * @return 输入的字符串
	 */
	public static String readString(String prompt) {
		try {
			System.out.print(prompt);
			return reader.readLine();
		} catch (IOException e) {
			// ignore
			return null;
		}
	}

	/**
	 * JDK1.6(利用java.io.Console进行读取)
	 * JDK6中提供了java.io.Console类专用来访问基于字符的控制台设备.
	 * 若程序要与Windows下的cmd或者Linux下的Terminal交互,就可以用Console类代劳.(类似System.in和System.out)
	 * 但程序不总是能得到可用的Console, 一个JVM是否有可用的Console依赖于底层平台和JVM如何被调用.
	 * 若JVM是在交互式命令行(比如Windows的cmd)中启动的,并且输入输出没有重定向到另外的地方,那么就可以得到一个可用的Console实例.
	 * 在使用IDE的情况下,将无法获取到Console实例,原因是在IDE环境下重新定向了标准输入和输出流,也就是将系统控制台上的输入输出重定向到了IDE的控制台中
	 * @param prompt 提示信息
	 * @return
	 */
	public static String readLine(String prompt) {
		Console console = System.console();
		if (console == null) {
			throw new IllegalStateException("Console is not available!");
		}
		return console.readLine(prompt);
	}

	public static void main(String[] args) {
		while(true){
			String input = readString("please input data: ");
			System.out.println(input);
		}
	}

}
