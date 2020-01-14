package com.simon.credit.toolkit.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.Pipe;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipToolkits {

	private static final String ZIP_FILE = "";
	private static final long FILE_SIZE = 1024 * 1024;
	private static final String SUFFIX_FILE = "";
	private static final String JPG_FILE_PATH = "";

	/**
	 * 使用Pip
	 */
	public static void zipFilePip() {
		long beginTime = System.currentTimeMillis();
		OutputStream os = null;
		WritableByteChannel out = null;
		try {
			os = new FileOutputStream(ZIP_FILE);
			out = Channels.newChannel(os);
			Pipe pipe = Pipe.open();
			// 异步任务
			CompletableFuture.runAsync(new Runnable() {
				@Override
				public void run() {
					runTask(pipe);
				}
			});
			// 获取读通道
			ReadableByteChannel readableByteChannel = pipe.source();
			ByteBuffer buffer = ByteBuffer.allocate(((int) FILE_SIZE) * 10);
			while (readableByteChannel.read(buffer) >= 0) {
				buffer.flip();
				out.write(buffer);
				buffer.clear();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			IOToolkits.close(os, out);
		}

		printInfo(beginTime);
	}

	private static void printInfo(long beginTime) {
		long endTime = System.currentTimeMillis();
		System.out.println("consum time: " + (endTime - beginTime));
	}

	/**
	 * 异步任务
	 * @param pipe
	 */
	public static void runTask(Pipe pipe) {
		OutputStream os = null;
		ZipOutputStream zos = null;
		WritableByteChannel out = null;
		try {
			os = Channels.newOutputStream(pipe.sink());
			zos = new ZipOutputStream(os);
			out = Channels.newChannel(zos);
			System.out.println("begin...");
			for (int i = 0; i < 10; i++) {
				zos.putNextEntry(new ZipEntry(i + SUFFIX_FILE));

				FileInputStream fis = new FileInputStream(new File(JPG_FILE_PATH));
				FileChannel jpgChannel = fis.getChannel();

				jpgChannel.transferTo(0, FILE_SIZE, out);

				jpgChannel.close();
				fis.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			IOToolkits.close(os, zos, out);
		}
	}

}