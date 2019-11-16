package com.simon.credit.toolkit.batch;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.simon.credit.toolkit.common.CommonToolkits;

/**
 * 批量执行器
 * @author XUZIMING 2019-11-16
 */
public class BatchExecutor {
	private static final int BATCH_SIZE = 100;

	public static final <T> void execute(List<T> params, BatchProcessor<List<T>> batchProcessor) {
		if (CommonToolkits.isEmpty(params)) {
			return;
		}

		if (params.size() <= BATCH_SIZE) {
			batchProcessor.batchProcess(params);
			return;
		}

		int batchSize = BATCH_SIZE;
		int batchTimes = (params.size() - 1) / batchSize + 1;

		for (int i = 0; i < batchTimes; i++) {
			int from = batchSize * i;
			int to   = batchSize * (i + 1);

			List<T> batchParams = params.subList(from, Math.min(to, params.size()));

			batchProcessor.batchProcess(batchParams);
		}
	}

	public static final <T> void execute(Set<T> params, BatchProcessor<Set<T>> batchProcessor) {
		if (CommonToolkits.isEmpty(params)) {
			return;
		}

		if (params.size() <= BATCH_SIZE) {
			batchProcessor.batchProcess(params);
			return;
		}

		Set<T> batchParams = new HashSet<T>(BATCH_SIZE);

		for (Iterator<T> iterator = params.iterator(); iterator.hasNext();) {
			T element = iterator.next();
			batchParams.add(element);

			if (batchParams.size() == BATCH_SIZE) {
				batchProcessor.batchProcess(batchParams);
				batchParams.clear();
			}
		}

		if (CommonToolkits.isNotEmpty(batchParams)) {
			batchProcessor.batchProcess(batchParams);
		}
	}

}