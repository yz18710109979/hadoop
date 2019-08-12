package com.example.hadoop.maprd.reducetask;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;

public class MyPartition extends Partitioner<IntWritable, Text>{

	@Override
	public int getPartition(IntWritable key, Text value, int numPartitions) {
		return 0;
	}

}
