package com.simon.credit.toolkit.hash.consistent;

/**
 * 服务器节点
 * @author XUZIMING 2018-06-20
 */
public class ServerNode {

	private String serverNodeName;
	private long serverNodeHash;

	public ServerNode(String serverNodeName, long serverNodeHash) {
		this.serverNodeName = serverNodeName;
		this.serverNodeHash = serverNodeHash;
	}

	public String getServerNodeName() {
		return serverNodeName;
	}

	public void setServerNodeName(String serverNodeName) {
		this.serverNodeName = serverNodeName;
	}

	public long getServerNodeHash() {
		return serverNodeHash;
	}

	public void setServerNodeHash(long serverNodeHash) {
		this.serverNodeHash = serverNodeHash;
	}

}
