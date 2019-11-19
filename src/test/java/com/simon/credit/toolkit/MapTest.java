package com.simon.credit.toolkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.simon.credit.toolkit.core.MyHashMap;
import com.simon.credit.toolkit.hash.MapToolkits;

public class MapTest {

	public static void main(String[] args) {
		List<User> users = new ArrayList<User>(8);
		users.add(new User("张三", 18));
		users.add(new User("李四", 20));

		Map<String, User> userMap = MapToolkits.parseMap(users, "name");
		for (Map.Entry<String, User> entry : userMap.entrySet()) {
			System.out.println(entry.getKey());
			System.out.println(entry.getValue().getAge());
		}

		Map<String, Integer> map = new MyHashMap<String, Integer>(8);
		map.put("123", 123);
		map.put("234", 234);
		map.put("345", 345);
		map.put("456", 456);
		map.put("567", 567);
		for (Entry<String, Integer> entry : map.entrySet()) {
			System.out.println("k:" + entry.getKey() + ",v:" + entry.getValue());
		}
	}

}
