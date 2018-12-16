package com.simon.credit.toolkit;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

/**
 * 一致性哈希
 * @author XUZIMING 2018-08-12
 */
public class ConsistentHashing {

	/** 每个机器节点关联的虚拟节点个数 */
	private static final int VIRTUAL_NODE_NUM = 100;

	/** 虚拟节点到真实节点的映射 */
	private TreeMap<Long, Node> virtualToActualMap;

	/** key到真实节点的映射 */
	private TreeMap<Long, Node> keyToActualMap;

	/** 真实机器节点 */
	private List<Node> actualNodes = new ArrayList<Node>();

	boolean flag = false;

	public ConsistentHashing(List<Node> actualNodes) {
		this.actualNodes = actualNodes;
		init();
	}

	public void printKeyTree() {
		for (Map.Entry<Long, Node> entry : keyToActualMap.entrySet()) {
			long hash = entry.getKey();
			System.out.println("hash(" + hash + ")连接到主机->" + entry.getValue());
		}
	}

	private void init() { // 初始化一致性hash环
		virtualToActualMap = new TreeMap<Long, Node>();
		keyToActualMap = new TreeMap<Long, Node>();

		// 每个真实机器节点都需要关联虚拟节点
		for (Node actualNode : actualNodes) {
			for (int i = 0; i < VIRTUAL_NODE_NUM; i++) {
				long virtualNodeHash = hash("actual-" + actualNode.name + "-virtual-" + i);
				// 一个真实机器节点关联NODE_NUM个虚拟节点
				virtualToActualMap.put(virtualNodeHash, actualNode);
			}
		}
	}

	/**
	 * 增加一个真实节点(服务器)
	 * @param actualNode
	 */
	private void addActualNode(Node actualNode) {
		actualNodes.add(actualNode);
		System.out.println("add actual node: " + actualNode + " change: ");

		for (int i = 0; i < VIRTUAL_NODE_NUM; i++) {
			long virtualNodeHash = hash("actual-" + actualNode.name + "-virtual-" + i);
			addVirtualNode(virtualNodeHash, actualNode);
		}
	}

	/**
	 * 添加一个虚拟节点进环形结构
	 * @param virtualNodeHash 虚拟节点hash值
	 * @param actualNode
	 */
	public void addVirtualNode(Long virtualNodeHash, Node actualNode) {
		SortedMap<Long, Node> tail = virtualToActualMap.tailMap(virtualNodeHash);
		SortedMap<Long, Node> head = virtualToActualMap.headMap(virtualNodeHash);

		Long begin = 0L;
		@SuppressWarnings("unused")
		Long end = 0L;

		SortedMap<Long, Node> between;
		if (head.size() == 0) {
			between = keyToActualMap.tailMap(virtualToActualMap.lastKey());
			flag = true;
		} else {
			begin = head.lastKey();
			between = keyToActualMap.subMap(begin, virtualNodeHash);
			flag = false;
		}

		virtualToActualMap.put(virtualNodeHash, actualNode);

		for (Iterator<Long> it = between.keySet().iterator(); it.hasNext();) {
			Long hash = it.next();
			if (flag) {
				keyToActualMap.put(hash, virtualToActualMap.get(virtualNodeHash));
				System.out.println("hash(" + hash + ")change to->" + tail.get(tail.firstKey()));
			} else {
				keyToActualMap.put(hash, virtualToActualMap.get(virtualNodeHash));
				System.out.println("hash(" + hash + ")change to->" + tail.get(tail.firstKey()));
			}
		}
	}

	/**
	 * 删除真实节点
	 * @param actualNode
	 */
	public void deleteActualNode(Node actualNode) {
		if (actualNode == null) {
			return;
		}

		System.out.println("删除主机" + actualNode + "的变化：");
		for (int i = 0; i < VIRTUAL_NODE_NUM; i++) {
			// 定位真实节点的第i个虚拟节点的位置
			long virtualNodeHash = hash("actual-" + actualNode.name + "-virtual-" + i);
			SortedMap<Long, Node> tail = virtualToActualMap.tailMap(virtualNodeHash);
			SortedMap<Long, Node> head = virtualToActualMap.headMap(virtualNodeHash);
			Long begin = 0L;
			Long end = 0L;

			SortedMap<Long, Node> between;
			if (head.size() == 0) {
				between = keyToActualMap.tailMap(virtualToActualMap.lastKey());
				end = tail.firstKey();
				tail.remove(tail.firstKey());
				// 从virtualToActualMap中删除真实节点的第i个虚拟节点
				virtualToActualMap.remove(tail.firstKey());
				flag = true;
			} else {
				begin = head.lastKey();
				end = tail.firstKey();
				tail.remove(tail.firstKey());
				// 在真实节点的第i个虚拟节点的所有key
				between = keyToActualMap.subMap(begin, end);
				flag = false;
			}
			for (Iterator<Long> it = between.keySet().iterator(); it.hasNext();) {
				Long lo = it.next();
				if (flag) {
					keyToActualMap.put(lo, tail.get(tail.firstKey()));
					System.out.println("hash(" + lo + ")change to->" + tail.get(tail.firstKey()));
				} else {
					keyToActualMap.put(lo, tail.get(tail.firstKey()));
					System.out.println("hash(" + lo + ")change to->" + tail.get(tail.firstKey()));
				}
			}
		}
	}

	/**
	 * 映射key到真实节点
	 * @param key
	 */
	public void keyToActualNode(String key) {
		long hash = hash(key);
		// 沿环的顺时针找到一个虚拟节点
		SortedMap<Long, Node> tail = virtualToActualMap.tailMap(hash);
		if (tail.size() == 0) {
			return;
		}

		keyToActualMap.put(hash, tail.get(tail.firstKey()));
		System.out.println(key + "(hash:" + hash + ")connect to host->" + tail.get(tail.firstKey()));
	}

	/**
	 * MurMurHash算法，是非加密HASH算法，性能很高，
	 * 比传统的CRC32,MD5，SHA-1（这两个算法都是加密HASH算法，复杂度本身就很高，带来的性能上的损害也不可避免）
	 * 等HASH算法要快很多，而且据说这个算法的碰撞率很低. http://murmurhash.googlepages.com/
	 */
	private static Long hash(String key) {
		ByteBuffer buf = ByteBuffer.wrap(key.getBytes());
		int seed = 0x1234ABCD;

		ByteOrder byteOrder = buf.order();
		buf.order(ByteOrder.LITTLE_ENDIAN);

		long m = 0xc6a4a7935bd1e995L;
		int r = 47;

		long h = seed ^ (buf.remaining() * m);

		long k;
		while (buf.remaining() >= 8) {
			k = buf.getLong();

			k *= m;
			k ^= k >>> r;
			k *= m;

			h ^= k;
			h *= m;
		}

		if (buf.remaining() > 0) {
			ByteBuffer finish = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
			// for big-endian version, do this first:
			// finish.position(8-buf.remaining());
			finish.put(buf).rewind();
			h ^= finish.getLong();
			h *= m;
		}

		h ^= h >>> r;
		h *= m;
		h ^= h >>> r;

		buf.order(byteOrder);
		return h;
	}

	static class Node {
		String name;
		String ip;

		public Node(String name, String ip) {
			this.name = name;
			this.ip = ip;
		}

		@Override
		public String toString() {
			return this.name + "-" + this.ip;
		}
	}

	public static void main(String[] args) {
		List<Node> actualNodes = new ArrayList<Node>();
		actualNodes.add(new Node("s1", "192.168.1.1"));
		actualNodes.add(new Node("s2", "192.168.1.2"));
		actualNodes.add(new Node("s3", "192.168.1.3"));
		actualNodes.add(new Node("s4", "192.168.1.4"));

		ConsistentHashing consistentHashing = new ConsistentHashing(actualNodes);

		System.out.println("添加客户端，一开始有4个主机，分别为s1,s2,s3,s4,每个主机有100个虚拟主机：");
		consistentHashing.keyToActualNode("101客户端");
		consistentHashing.keyToActualNode("102客户端");
		consistentHashing.keyToActualNode("103客户端");
		consistentHashing.keyToActualNode("104客户端");
		consistentHashing.keyToActualNode("105客户端");
		consistentHashing.keyToActualNode("106客户端");
		consistentHashing.keyToActualNode("107客户端");
		consistentHashing.keyToActualNode("108客户端");
		consistentHashing.keyToActualNode("109客户端");

		consistentHashing.keyToActualNode(UUID.randomUUID().toString());
		consistentHashing.keyToActualNode(UUID.randomUUID().toString());
		consistentHashing.keyToActualNode(UUID.randomUUID().toString());
		consistentHashing.keyToActualNode(UUID.randomUUID().toString());
		consistentHashing.keyToActualNode(UUID.randomUUID().toString());
		consistentHashing.keyToActualNode(UUID.randomUUID().toString());
		consistentHashing.keyToActualNode(UUID.randomUUID().toString());
		consistentHashing.keyToActualNode(UUID.randomUUID().toString());
		consistentHashing.keyToActualNode(UUID.randomUUID().toString());

		consistentHashing.deleteActualNode(actualNodes.get(1));

		consistentHashing.addActualNode(new Node("s5", "192.168.1.5"));

		System.out.println("最后的客户端到主机的映射为：");
		consistentHashing.printKeyTree();
	}

}
