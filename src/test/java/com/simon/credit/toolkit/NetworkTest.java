package com.simon.credit.toolkit;

import com.simon.credit.toolkit.network.NetToolkits;
import com.simon.credit.toolkit.network.URL;

public class NetworkTest {

	public static void main(String[] args) {
		// 获取本地IP
		String localHost = NetToolkits.getLocalHost();
		System.out.println(localHost);

		URL url = URL.valueOf("http://www.baidu.com?aa=bb&&cc=dd");
		System.out.println(url.getParameters());
	}

}
