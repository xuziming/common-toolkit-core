package com.simon.credit.toolkit;

import com.simon.credit.toolkit.network.URLX;

public class URLXTest {

	public static void main(String[] args) throws Exception {
		URLX url = URLX.valueOf("http://www.baidu.com?bbb=kkk");

		System.out.println("URL是 " + url.toString());
		System.out.println("主机是 " + url.getHost());

		System.out.println(url.toString());
		System.out.println(url.getParameters());

		System.out.println(url.update("http://www.yahoo.com?bb=kk&hh=ww"));
		System.out.println(url.addParameter("rr", "yy").addParameter("cc", "dd"));
	}

}
