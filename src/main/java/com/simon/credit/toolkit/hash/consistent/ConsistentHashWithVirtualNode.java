package com.simon.credit.toolkit.hash.consistent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.zip.CRC32;

/**
 * 一致性哈希(带虚拟节点)
 * @author XUZIMING 2018-06-20
 */
public class ConsistentHashWithVirtualNode implements ConsistentHash {
	/** 保存虚拟服务器节点节点 */
	List<VirtualServerNode> virtualServerNodes = new ArrayList<VirtualServerNode>();

	/** 每个物理节点对应的虚拟节点的个数 */
	private final static int VIRTUAL_NUM = 100;

	@Override
	public void addServerNode(String serverName) {
		if (serverName == null) {
			return;
		}
		for (int i = 0; i < VIRTUAL_NUM; i++) {
			// 这里假设，虚拟节点的名字为类似这样的形式：serverName+"&&VN"+i，这样方便从虚拟节点得到物理节点
			String virtualServerNodeName = serverName + "&&VSN" + i;
			long serverHash = getHash(virtualServerNodeName);
			VirtualServerNode vsn = new VirtualServerNode(serverName, serverHash);
			virtualServerNodes.add(vsn);
		}

		// 将virtualServerNodes进行排序
		Collections.sort(virtualServerNodes, new Comparator<VirtualServerNode>() {
			@Override
			public int compare(VirtualServerNode node1, VirtualServerNode node2) {
				return node1.getServerNodeHash() < node2.getServerNodeHash() ? -1 : 1;
			}
		});
	}

	@Override
	public long getHash(String serverNodeName) {
		CRC32 crc32 = new CRC32();
		crc32.update(serverNodeName.getBytes());
		return crc32.getValue();
	}

	@Override
	public void deleteServerNode(String serverName) {
		if (virtualServerNodes.isEmpty()) {
			return;
		}

		if (serverName == null || serverName.trim().isEmpty()) {
			return;
		}

		Iterator<VirtualServerNode> iterator = virtualServerNodes.iterator();
		while (iterator.hasNext()) {
			VirtualServerNode vsn = iterator.next();
			if (serverName.trim().contains(vsn.getServerNodeName())) {
				iterator.remove();
			}
		}
	}

	@Override
	public VirtualServerNode getServerNode(String key) {
		// 得到key的hash值
		long hash = getHash(key);

		// 在VirtualServerNode中找到大于hash且离其最近的的那个VirtualServerNode
		// 由于serverNodes是升序排列的,因此找到的第一个大于hash的就是目标节点
		for (VirtualServerNode vsn : virtualServerNodes) {
			if (vsn.getServerNodeHash() > hash) {
				return vsn;
			}
		}

		// 若没找到,说明key的hash值比所有服务器节点的hash值都大,因此返回最小hash值的Server节点
		return virtualServerNodes.get(0);
	}

	@Override
	public void printServerNodes() {
		System.out.println("所有的服务器节点信息如下：");
		for (VirtualServerNode vsn : virtualServerNodes) {
			System.out.println(vsn.getServerNodeName() + ":" + vsn.getServerNodeHash());
		}
	}

	public static void main(String[] args) {
		ConsistentHashWithVirtualNode ch = new ConsistentHashWithVirtualNode();
		// 添加一系列的服务器节点
		String[] servers = { 
			"192.168.0.0:111", 
			"192.168.0.1:111",
			"192.168.0.2:111", 
			"192.168.0.3:111", 
			"192.168.0.4:111" 
		};
		for (String server : servers) {
			ch.addServerNode(server);
		}

		// 打印输出一下服务器节点
		ch.printServerNodes();

		// 看看下面的客户端节点会被路由到哪个服务器节点
		String[] nodes = { "127.0.0.1:1111", "221.226.0.1:2222", "10.211.0.1:3333" };
		System.out.println("此时，各个客户端的路由情况如下：");
		for (String node : nodes) {
			VirtualServerNode vsn = ch.getServerNode(node);
			System.out.println(node + "," + ch.getHash(node) + "------->"
				+ vsn.getServerNodeName() + "," + vsn.getServerNodeHash());
		}

		// 如果由一个服务器节点宕机，即需要将这个节点从服务器集群中移除
//		String deleteNodeName = "192.168.0.2:111";
//		ch.deleteServerNode(deleteNodeName);
//		System.out.println("删除节点" + deleteNodeName + "后，再看看同样的客户端的路由情况，如下：");

		String addNodeName = "192.168.0.5:111";
		ch.addServerNode(addNodeName);
		System.out.println("增加节点" + addNodeName + "后，再看看同样的客户端的路由情况，如下：");

		for (String node : nodes) {
			VirtualServerNode vsn = ch.getServerNode(node);
			System.out.println(node + "," + ch.getHash(node) + "------->"
				+ vsn.getServerNodeName() + "," + vsn.getServerNodeHash());
		}
	}

}