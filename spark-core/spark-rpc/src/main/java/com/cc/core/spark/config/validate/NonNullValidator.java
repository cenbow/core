package com.cc.core.spark.config.validate;


import com.cc.core.spark.error.ConfigException;

/**
 * User: chenchong
 * Date: 2018/11/16
 * description:
 */
public class NonNullValidator implements Validator {
	@Override
	public void ensureValid(String name, Object value) {
		if (value == null) {
			// Pass in the string null to avoid the spotbugs warning
			throw new ConfigException(name, "null", "entry must be non null");
		}
	}

	public String toString() {
		return "non-null string";
	}
}