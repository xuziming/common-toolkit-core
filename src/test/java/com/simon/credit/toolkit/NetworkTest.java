package com.simon.credit.toolkit;

<<<<<<< HEAD
import com.simon.credit.toolkit.network.HttpToolkits;
=======
>>>>>>> 623b2424f05757a25e9c77a6d30d1c04cccde15c
import com.simon.credit.toolkit.network.NetToolkits;
import com.simon.credit.toolkit.network.URL;

public class NetworkTest {

	public static void main(String[] args) {
		testCustomURL();
		testGetLocalhost();
		testJsonPost();
	}

	private static void testCustomURL() {
		URL url = URL.valueOf("http://www.baidu.com?aa=bb&&cc=dd");
		System.out.println(url.getParameters());
	}

	private static void testGetLocalhost() {
		// 获取本地IP
		String localHost = NetToolkits.getLocalHost();
		System.out.println(localHost);
	}

	private static void testJsonPost() {
		String url = "http://ip.taobao.com/service/getIpInfo.php?ip=63.223.108.42";
		String json = "{\"tel\":\"13145770936\"}";
<<<<<<< HEAD
		System.out.println(HttpToolkits.jsonPost(url, json));
=======
		// System.out.println(HttpUtils.jsonPost(url, json));
>>>>>>> 623b2424f05757a25e9c77a6d30d1c04cccde15c
	}

}
