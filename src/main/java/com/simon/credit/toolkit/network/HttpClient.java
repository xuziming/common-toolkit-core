package com.simon.credit.toolkit.network;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.simon.credit.toolkit.common.CommonToolkits;
import com.simon.credit.toolkit.io.IOToolkits;

/**
 * HTTP请求工具类
 * @author XUZIMING 2018-12-20
 */
public class HttpClient {

	private static final int RESPONSE_CODE_SUCCESS = 200;

	/**
	 * JSON请求
	 */
	public static String jsonPost(String baseUrl, String jsonParams) {
		StringBuilder result = new StringBuilder("");
		BufferedReader reader = null;
		try {
			URL url = new URL(baseUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setUseCaches(false);
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("Charset", "UTF-8");
			// 设置数据类型
			connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			// 设置接收类型否则返回415错误
			// conn.setRequestProperty("accept","*/*")此处为暴力方法设置接受所有类型，以此来防范返回415;
			connection.setRequestProperty("accept", "application/json");

			// 往服务器里面发送数据
			if (CommonToolkits.isNotEmpty(jsonParams)) {
				byte[] data = jsonParams.getBytes();
				// 设置文件长度
				connection.setRequestProperty("Content-Length", String.valueOf(data.length));
				OutputStream output = connection.getOutputStream();
				output.write(jsonParams.getBytes());
				output.flush();
				output.close();
			}

			if (connection.getResponseCode() == RESPONSE_CODE_SUCCESS) {
				reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
				String line = null;
				while ((line = reader.readLine()) != null) {
					result.append(line);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			IOToolkits.close(reader);
		}

		return result.toString();
	}

}
