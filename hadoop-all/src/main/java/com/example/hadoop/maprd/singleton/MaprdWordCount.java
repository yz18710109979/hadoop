package com.example.hadoop.maprd.singleton;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class MaprdWordCount {

	static class MyMapper extends Mapper<LongWritable, Text, Text, IntWritable>{
		@Override
		protected void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			String[] words = value.toString().split("\t");
			for (String w : words) {
				Text mk = new Text(w);
				IntWritable mv = new IntWritable(1);
				context.write(mk, mv);
			}
		}
	}
	
	static class MyReducer extends Reducer<Text, IntWritable, Text, IntWritable>{
		@Override
		protected void reduce(Text key, Iterable<IntWritable> values,
				Context context) throws IOException, InterruptedException {
			//循环遍历  values 每一个值   累加   每一组  单词出现次数
			int sum=0;
			for(IntWritable v:values){
				//hadoop    数值  --- java 数值  get（）
				sum+=v.get();
			}
			IntWritable rv=new IntWritable(sum);
			context.write(key, rv);
		}
	}
	
	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf);
		job.setJarByClass(MaprdWordCount.class);
		job.setMapperClass(MyMapper.class);
		job.setReducerClass(MyReducer.class);
		
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(IntWritable.class);
		
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		
		FileInputFormat.addInputPath(job, new Path("D:\\doc\\wc"));
		
		FileSystem fs = FileSystem.get(conf);
		Path out = new Path("D:\\doc\\wc_out0001");
		if(fs.exists(out)) fs.mkdirs(out);
		FileOutputFormat.setOutputPath(job, out);
		
		job.waitForCompletion(false);
	}
}
