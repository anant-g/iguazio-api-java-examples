package com.network.singtel.algo;

import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Row;

public class MonitorFunction implements MapFunction<Row, String> {

	@Override
	public String call(Row arg0) throws Exception {
		return arg0.mkString();
	}
}
