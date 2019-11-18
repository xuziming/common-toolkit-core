package com.simon.credit.toolkit.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;

/**
 * 随机读写文件工具类
 * @author XUZIMING 2019-11-18
 */
public class RandomAccessFileToolkits {

	/**
	 * 随机读
	 * @param filePath 文件路径
	 * @param position 指针位置
	 **/
	public static void randomRead(String filePath, int position) {
		// model各个参数详解 r 代表以只读方式打开指定文件 rw 以读写方式打开指定文件 rws 读写方式打开，
		// 并对内容或元数据都同步写入底层存储设备 rwd 读写方式打开，对文件内容的更新同步更新至底层存储设备
		try (RandomAccessFile raf = new RandomAccessFile(filePath, "r")) {
			// RandomAccessFile raf=new RandomAccessFile(new File("D:\\3\\test.txt"), "r");
			// 获取RandomAccessFile对象文件指针的位置，初始位置是0
			System.out.println("RandomAccessFile文件指针的初始位置:" + raf.getFilePointer());
			raf.seek(position);// 移动文件指针位置
			byte[] buff = new byte[512];
			// 用于保存实际读取的字节数
			int hasRead = 0;
			// 循环读取
			while ((hasRead = raf.read(buff)) > 0) {
				// 打印读取的内容,并将字节转为字符串输入
				System.out.println(new String(buff, 0, hasRead));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 随机写(追加方式)
	 * @param filePath 文件路径
	 * @param appendContent 追加内容
	 ***/
	public static void randomWrite(String filePath, String appendContent) {
		if (appendContent == null || appendContent.trim().isEmpty()) {
			return;
		}
		// 以读写的方式建立一个RandomAccessFile对象
		try (RandomAccessFile raf = new RandomAccessFile(filePath, "rw")) {
			// 将记录指针移动到文件最后
			raf.seek(raf.length());
			raf.write(appendContent.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 实现向指定位置插入数据
	 * @param filePath      文件路径
	 * @param position      指针位置
	 * @param insertContent 插入内容
	 **/
	public static void insert(String filePath, long position, String insertContent) {
		RandomAccessFile raf = null;
		FileOutputStream fos = null;
		FileInputStream  fis = null;

		try {
			File tmp = File.createTempFile("tmp", null);
			tmp.deleteOnExit();// 在JVM退出时删除

			raf = new RandomAccessFile(filePath, "rw");
			// 创建一个临时文件夹来保存插入点后的数据
			fos = new FileOutputStream(tmp);
			fis = new FileInputStream(tmp);
			raf.seek(position);

			/** 将插入点后的内容读入临时文件夹 **/
			byte[] buffer = new byte[1024];
			// 用于保存临时读取的字节数
			int hasRead = 0;
			// 循环读取插入点后的内容
			while ((hasRead = raf.read(buffer)) > 0) {
				// 将读取的数据写入临时文件中
				fos.write(buffer, 0, hasRead);
			}

			// 插入需要指定添加的数据
			raf.seek(position);// 返回原来的插入处
			// 追加需要追加的内容
			raf.write(insertContent.getBytes());
			// 最后追加临时文件中的内容
			while ((hasRead = fis.read(buffer)) > 0) {
				raf.write(buffer, 0, hasRead);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			IOToolkits.close(fis, fos, raf);
		}
	}

	public static void main(String[] args) {
		// 测试随机读
		String path = "D:\\test.txt";
		int seekPointer = 20;
		randomRead(path, seekPointer);// 读取的方法
		// randomWrite(path);//追加写的方法
		// insert(path, 33, "\nlucene是一个优秀的全文检索库");

		// 测试随机写
		// String path="D:\\3\\test.txt";
		// int seekPointer=20;
		// randomRed(path,seekPointer);//读取的方法
		randomWrite(path, "我是追加的\r\n");// 追加写的方法
		// insert(path, 33, "\nlucene是一个优秀的全文检索库");

		// 测试任意位置插入数据
		// String path="D:\\3\\test.txt";
		// int seekPointer=20;
		// randomRed(path,seekPointer);//读取的方法
		// randomWrite(path);//追加写的方法
		insert(path, 33, "\nlucene是一个优秀的全文检索库");
	}

}
