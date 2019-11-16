package com.simon.credit.toolkit;

import java.util.ArrayList;
import java.util.List;

import com.simon.credit.toolkit.batch.BatchProcessor;
import com.simon.credit.toolkit.batch.BatchSpliter;

public class BatchSpliterTest {

	public static void main(String[] args) {
		List<Integer> nums = new ArrayList<Integer>(1600);
		for (int i = 0; i < 1000; i++) {
			nums.add(i);
		}

		BatchSpliter.split(nums, new BatchProcessor<List<Integer>>() {
			@Override
			public void batchProcess(List<Integer> batchParams) {
				System.out.println(batchParams);
			}
		});
	}

}
