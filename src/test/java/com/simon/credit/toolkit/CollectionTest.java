package com.simon.credit.toolkit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.simon.credit.toolkit.collection.CollectionToolkits;
import com.simon.credit.toolkit.hash.consistent.ServerNode;
import com.simon.credit.toolkit.lang.StringFormatter;
import com.simon.credit.toolkit.reflect.DataFetcher;
import com.simon.credit.toolkit.reflect.NotNullDataFetcher;
import com.simon.credit.toolkit.reflect.TypeRef;

public class CollectionTest {

	public static void main(String[] args) {
		List<ServerNode> list = new ArrayList<ServerNode>();
		list.add(new ServerNode(null, 12345));
		list.add(new ServerNode("bbb", 12346));
		list.add(new ServerNode("ccc", 12347));
		list.add(new ServerNode("ddd", 12348));
		list.add(new ServerNode("ddd", 12348));
		list.add(new ServerNode("ddd", 12350));

		Set<String> coll = CollectionToolkits.collect(list, new NotNullDataFetcher<ServerNode, String>() {
			public String fetch(ServerNode source) {
				return source.getServerNodeName();
			}
		}, new TypeRef<Set<String>>() {});

		System.out.println(StringFormatter.format("=== className: {}", coll.getClass().getName()));

		Map<String, Collection<ServerNode>> map = 
			CollectionToolkits.groupBy(list, new DataFetcher<ServerNode, String>() {
				public String fetch(ServerNode source) {
					return source.getServerNodeName();
				}
			});

		for (Map.Entry<String, Collection<ServerNode>> entry : map.entrySet()) {
			System.out.println(entry.getKey());
		}

		List<Integer> list2 = new LinkedList<Integer>();
		list2.add(12350);
		list2.add(12346);
		list2.add(12344);
		list2.add(12342);
		list2.add(12337);
		list2.add(12333);

		boolean userRequested = java.security.AccessController.doPrivileged(
			new sun.security.action.GetBooleanAction("java.util.Arrays.useLegacyMergeSort")).booleanValue();
		System.out.println(userRequested);

		Collections.sort(list2);
		for (Integer i : list2) {
			System.out.println(i);
		}
		System.out.println("----------------------------------");

		CollectionToolkits.sort(list2);
		for (Integer i : list2) {
			System.out.println(i);
		}
		System.out.println("----------------------------------");

		Collections.sort(list2, new Comparator<Integer>() {
			@Override
			public int compare(Integer num1, Integer num2) {
				return num1 - num2;// 升序排列
			}
		});
		for (Integer i : list2) {
			System.out.println(i);
		}
		System.out.println("----------------------------------");

		CollectionToolkits.sort(list2, new Comparator<Integer>() {
			@Override
			public int compare(Integer num1, Integer num2) {
				return num2 - num1;// 降序排列
			}
		});

		for (Integer i : list2) {
			System.out.println(i);
		}
	}

}
