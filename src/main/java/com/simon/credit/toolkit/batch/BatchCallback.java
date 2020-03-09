package com.simon.credit.toolkit.batch;

import java.util.Collection;

/**
 * 批量回调处理
 * @author XUZIMING 2019-11-16
 */
public interface BatchCallback<C extends Collection<?>> {

	/**
	 * 回调处理
	 * @param batchParams
	 */
	void process(C batchParams);

}