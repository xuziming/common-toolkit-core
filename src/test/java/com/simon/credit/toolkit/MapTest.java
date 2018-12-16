package com.simon.credit.toolkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
	}

}
