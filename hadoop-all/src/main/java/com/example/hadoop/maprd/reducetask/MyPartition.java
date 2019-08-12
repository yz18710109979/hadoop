package com.example.hadoop.maprd.reducetask;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;

public class MyPartition extends Partitioner<Text, Text>{

	@Override
	public int getPartition(Text key, Text value, int numPartitions) {
		return 0;
	}

}
