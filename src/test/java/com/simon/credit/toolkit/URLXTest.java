package com.simon.credit.toolkit;

import com.simon.credit.toolkit.network.URLX;

import java.util.Map;

public class URLXTest {

	public static void main(String[] args) {
		URLX url = URLX.valueOf("dubbo://10.131.13.54:20888/com.pingan.hrx.ess.abs.vacation.service.AbsVacationRevokeService?accepts=300&anyhost=true&application=ess-abs-service&bean.name=com.pingan.hrx.ess.abs.vacation.service.AbsVacationRevokeService&cluster=failover&deprecated=false&dispatcher=all&dubbo=2.0.2&dynamic=true&generic=false&interface=com.pingan.hrx.ess.abs.vacation.service.AbsVacationRevokeService&metadata=remote&methods=revokeApply&organization=HRX-ESS&pid=14&release=2.7.4.1&retries=0&revision=1.2.0-SNAPSHOT&serialization=hessian2&service.filter=-exception,dubboTracingContextFilter&side=provider&threadpool=fixed&threads=300&timeout=300000×tamp=1621318059802&version=1.0");

		System.out.println("协议是 " + url.getProtocol());
		System.out.println("URL是 " + url.toString());
		System.out.println("主机是 " + url.getHost());
		System.out.println("端口是 " + url.getPort());

		System.out.println(url.toString());
		System.out.println(url.getParameters());
		System.out.println();

		for (Map.Entry<String, String> entry : url.getParameters().entrySet()) {
			System.out.println("&" + entry.getKey() + "=" + entry.getValue());
		}
	}

}