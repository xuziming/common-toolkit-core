package com.simon.credit.toolkit.batch;

import java.util.Collection;

/**
 * 分批处理器
 * @author XUZIMING 2019-11-16
 */
public interface BatchProcessor<C extends Collection<?>> {

	/**
	 * 分批处理
	 * @param batchParams
	 */
	void batchProcess(C batchParams);

}