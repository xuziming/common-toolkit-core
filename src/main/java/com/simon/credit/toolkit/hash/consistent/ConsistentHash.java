package com.simon.credit.toolkit.hash.consistent;

/**
 * 一致性哈希(无虚拟节点)
 * @author XUZIMING 2018-06-20
 */
public interface ConsistentHash {

	/** 添加服务器节点 */
	void addServerNode(String serverName);

	/** 获取HASH值 */
	long getHash(String serverNodeName);

	/** 删除服务器节点,即要删除其物理服务器节点对应的所有虚拟节点 */
	void deleteServerNode(String serverName);

	/** 得到应当路由到的结点 */
	ServerNode getServerNode(String key);

	/** 打印服务器节点 */
	void printServerNodes();

}
