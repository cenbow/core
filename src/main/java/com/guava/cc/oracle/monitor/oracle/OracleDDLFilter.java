package com.guava.cc.oracle.monitor.oracle;

/*
 * User: chenchong
 * Date: 2019/3/14
 * description:
 */

import com.guava.cc.oracle.monitor.Filter;

public class OracleDDLFilter implements Filter<String> {
	@Override
	public boolean match(String s) {
		return false;
	}
}
