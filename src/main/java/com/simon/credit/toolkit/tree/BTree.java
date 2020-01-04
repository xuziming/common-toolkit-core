package com.simon.credit.toolkit.tree;

/**
 * B树
 * @param <K>
 * @param <V>
 */
public class BTree<K extends Comparable<K>, V> {
	// max children per B-tree node = M-1 (must be even and greater than 2)
	private static final int MAX_CHILDREN = 4;

	private Node root; 	// root of the B-tree
	private int  height;// height of the B-tree
	private int  num; 	// number of key-value pairs in the B-tree

	private static final class Node {
		/** number of children */
		private int childrenNum;

		/** the array of children */
		@SuppressWarnings("rawtypes")
		private Entry[] children = new Entry[MAX_CHILDREN];

		private Node(int childrenNum) {
			this.childrenNum = childrenNum;
		}
	}

	/** 初始化一个空的B树 */
	public BTree() {
		root = new Node(0);
	}

	/** B树是否为空 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/** 返回B树结点数 */
	public int size() {
		return num;
	}

	/** 返回B树的高度 */
	public int height() {
		return height;
	}

	/** 根据key查询value */
	public V get(K key) {
		if (key == null) {
			throw new NullPointerException("key must not be null");
		}
		return search(root, key, height);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private V search(Node node, K key, int treeHeight) {
		Entry[] children = node.children;

		if (treeHeight == 0) {// external node到最底层叶子结点，遍历
			for (int i = 0; i < node.childrenNum; i++) {
				if (equals(key, children[i].key)) {
					return (V) children[i].value;
				}
			}
		} else {// internal node递归查找next地址
			for (int i = 0; i < node.childrenNum; i++) {
				if (i + 1 == node.childrenNum || less(key, children[i + 1].key)) {
					return search(children[i].next, key, treeHeight - 1);
				}
			}
		}

		return null;
	}

	/**
	 * 插入key与value键值对
	 * @param key 
	 * @param value 
	 * @throws NullPointerException key为空
	 */
	@SuppressWarnings("unchecked")
	public void put(K key, V value) {
		if (key == null) {
			throw new NullPointerException("key must not be null");
		}
		Node rightNode = insert(root, key, value, height); // 分裂后生成的右结点
		num++;
		if (rightNode == null) {
			return;
		}

		// need to split root重组root
		Node node = new Node(2);
		node.children[0] = new Entry<K, V>(root.children[0].key, null, root);
		node.children[1] = new Entry<K, V>(rightNode.children[0].key, null, rightNode);
		root = node;
		height++;
	}

	@SuppressWarnings("unchecked")
	private Node insert(Node half, K key, V value, int treeHeight) {
		int i;
		Entry<K, V> temp = new Entry<K, V>(key, value, null);

		if (treeHeight == 0) {// external node外部结点，也是叶子结点，在树的最底层，存的是内容value
			for (i = 0; i < half.childrenNum; i++) {
				if (less(key, half.children[i].key)) {
					break;
				}
			}
		} else {// internal node内部结点，存的是next地址
			for (i = 0; i < half.childrenNum; i++) {
				if ((i + 1 == half.childrenNum) || less(key, half.children[i + 1].key)) {
					Node node = insert(half.children[i++].next, key, value, treeHeight - 1);
					if (node == null) {
						return null;
					}
					temp.key = node.children[0].key;
					temp.next = node;
					break;
				}
			}
		}

		for (int j = half.childrenNum; j > i; j--) {
			half.children[j] = half.children[j - 1];
		}
		half.children[i] = temp;
		half.childrenNum++;

		if (half.childrenNum < MAX_CHILDREN) {
			return null;
		} else { // 分裂结点
			return split(half);
		}
	}

	/** 从中间分裂树 */
	private Node split(Node half) {
		Node node = new Node(MAX_CHILDREN / 2);
		half.childrenNum = MAX_CHILDREN / 2;
		for (int i = 0; i < MAX_CHILDREN / 2; i++) {
			node.children[i] = half.children[MAX_CHILDREN / 2 + i];
		}
		return node;
	}

	/** 返回B树的字符串表示形式 */
	public String toString() {
		return toString(root, height, "") + "\n";
	}

	/**
	 * 返回B树的字符串表示形式
	 * @param node 树结点
	 * @param treeHeight 树的高度
	 * @param indent 缩进字符
	 * @return
	 */
	private String toString(Node node, int treeHeight, String indent) {
		StringBuilder builder = new StringBuilder();
		@SuppressWarnings("rawtypes")
		Entry[] children = node.children;

		if (treeHeight == 0) {
			for (int i = 0; i < node.childrenNum; i++) {
				builder.append(indent + children[i].key + " " + children[i].value + "\n");
			}
		} else {
			for (int i = 0; i < node.childrenNum; i++) {
				if (i > 0) {
					builder.append(indent + "(" + children[i].key + ")\n");
				}
				builder.append(toString(children[i].next, treeHeight - 1, indent + "   "));
			}
		}

		return builder.toString();
	}

	/** 比较key值,避免覆盖 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private boolean less(Comparable key1, Comparable key2) {
		return key1.compareTo(key2) < 0;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private boolean equals(Comparable key1, Comparable key2) {
		return key1.compareTo(key2) == 0;
	}

	/** key,value键值对 */
	private static class Entry<K, V> {
		private Comparable<K> key;
		private Object value;

		/** 下一个结点 */
		private Node next;

		public Entry(Comparable<K> key, Object value, Node next) {
			this.key = key;
			this.value = value;
			this.next = next;
		}
	}

	public static void main(String[] args) {
		BTree<String, String> btree = new BTree<String, String>();

		btree.put("www.cs.princeton.edu" , "128.112.136.12"	);
		btree.put("www.cs.princeton.edu" , "128.112.136.11"	);
		btree.put("www.princeton.edu"	 , "128.112.128.15"	);
		btree.put("www.yale.edu"		 , "130.132.143.21"	);
		btree.put("www.simpsons.com"	 , "209.052.165.60"	);
		btree.put("www.apple.com"		 , "17.112.152.32" 	);
		btree.put("www.amazon.com"		 , "207.171.182.16"	);
		btree.put("www.ebay.com"		 , "66.135.192.87" 	);
		btree.put("www.cnn.com"		 	 , "64.236.16.20"  	);
		btree.put("www.google.com"		 , "216.239.41.99"	);
		btree.put("www.nytimes.com"	 	 , "199.239.136.200");
		btree.put("www.microsoft.com"	 , "207.126.99.140"	);
		btree.put("www.dell.com"		 , "143.166.224.230");
		btree.put("www.slashdot.org"	 , "66.35.250.151"	);
		btree.put("www.espn.com"		 , "199.181.135.201");
		btree.put("www.weather.com"	 	 , "63.111.66.11"	);
		btree.put("www.yahoo.com"		 , "216.109.118.65"	);

		System.out.println("cs.princeton.edu: "  + btree.get("www.cs.princeton.edu"));
		System.out.println("hardvardsucks.com: " + btree.get("www.harvardsucks.com"));
		System.out.println("simpsons.com: " 	 + btree.get("www.simpsons.com"));
		System.out.println("apple.com: " 		 + btree.get("www.apple.com"));
		System.out.println("ebay.com: " 		 + btree.get("www.ebay.com"));
		System.out.println("dell.com: " 		 + btree.get("www.dell.com"));
		System.out.println();

		System.out.println("size: " + btree.size());
		System.out.println("height: " + btree.height());
		System.out.println(btree);
		System.out.println();
	}

}