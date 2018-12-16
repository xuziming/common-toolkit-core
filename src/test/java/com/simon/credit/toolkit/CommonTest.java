package com.simon.credit.toolkit;

import java.util.ArrayList;
import java.util.List;

import com.simon.credit.toolkit.common.CommonToolkits;

public class CommonTest {

	public static void main(String[] args) {
		List<String> list = new ArrayList<String>();
		list.add(null);
		// list.add("abc");
		System.out.println(CommonToolkits.isAnyEmpty(list.toArray()));
	}

}
