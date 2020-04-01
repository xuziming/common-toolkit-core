package com.simon.credit.toolkit.batch;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.simon.credit.toolkit.common.CommonToolkits;

/**
 * 批量任务分割器
 * @author XUZIMING 2019-11-16
 */
public class BatchExecuter {

	private static final int DEFAULT_BATCH_SIZE = 100;

	private int initBatchSize = 100;

	public BatchExecuter() {
		this(DEFAULT_BATCH_SIZE);
	}

	public BatchExecuter(int initBatchSiz) {
		if (initBatchSiz <= 0) {
			initBatchSiz = DEFAULT_BATCH_SIZE;
		}
		this.initBatchSize = initBatchSiz;
	}

	/**
	 * 任务分割
	 * @param <T>
	 * @param params
	 * @param callback
	 */
	public <T> void execute(List<T> params, BatchCallback<List<T>> callback) {
		if (CommonToolkits.isEmpty(params)) {
			return;
		}

		if (params.size() <= initBatchSize) {
			callback.process(params);
			return;
		}

		int batchSize = initBatchSize;
		int batchTimes = (params.size() - 1) / batchSize + 1;

		for (int i = 0; i < batchTimes; i++) {
			int from = batchSize * i;
			int to   = batchSize * (i + 1);

			List<T> batchParams = params.subList(from, Math.min(to, params.size()));

			callback.process(batchParams);
		}
	}

	/**
	 * 任务分割
	 * @param <T>
	 * @param params
	 * @param callback
	 */
	public <T> void execute(Set<T> params, BatchCallback<Set<T>> callback) {
		if (CommonToolkits.isEmpty(params)) {
			return;
		}

		if (params.size() <= initBatchSize) {
			callback.process(params);
			return;
		}

		Set<T> batchParams = new HashSet<T>(initBatchSize);

		for (Iterator<T> iterator = params.iterator(); iterator.hasNext();) {
			T element = iterator.next();
			batchParams.add(element);

			if (batchParams.size() == initBatchSize) {
				callback.process(batchParams);
				batchParams.clear();
			}
		}

		if (CommonToolkits.isNotEmpty(batchParams)) {
			callback.process(batchParams);
		}
	}

}