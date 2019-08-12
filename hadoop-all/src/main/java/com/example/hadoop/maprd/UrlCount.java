package com.example.hadoop.maprd;

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

//1.一个超级大的文件   里面存储url  一行一个    求出现次数最多的url
public class UrlCount {
	
	static class MyMapper extends Mapper<LongWritable, Text, Text, IntWritable>{
		@Override
		protected void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			System.out.println(key +":::" + value);
			context.write(new Text(value), new IntWritable(1));
		}
	}
	static class MyReducer extends Reducer<Text, IntWritable, Text, IntWritable>{
		@Override
		protected void reduce(Text key, Iterable<IntWritable> values,
				Context context) throws IOException, InterruptedException {
			System.out.println(key);
			int sum = 0;
			for (IntWritable intWritable : values) {
				System.out.println(intWritable);
				sum += intWritable.get();
			}
			context.write(key, new IntWritable(sum));
		}
	}

	public static void main(String[] args) throws Exception {
//		Map<String,Integer> map = new HashMap<String,Integer>();
//		BufferedReader br = new BufferedReader(new FileReader("D:\\doc\\url\\url.txt"));
//		String line = null;
//		while((line = br.readLine()) != null) {
//			if(!map.containsKey(line)) {
//				map.put(line, 1);
//			}else {
//				Integer value = map.get(line) + 1;
//				map.put(line, value);
//			}
//		}
//		System.out.println(map);
//		br.close();
		//======================如果是大文件，基于maprd实现=============================
		//获取集群的配置文件
		Configuration conf = new Configuration();
		//启动job   构建一个job对象
		Job job = Job.getInstance(conf);
		//进行job的封装
		//指定jar包运行的 主类
		/*
		 * 获取class
		 * 1)类名。class
		 * 2）对象。getClass
		 * 3)Class.forName()
		 */
		job.setJarByClass(UrlCount.class);
		
		//指定map 和 reduce对应的类
		job.setMapperClass(MyMapper.class);
		job.setReducerClass(MyReducer.class);
		
		//指定map输出的  k v的类型
		/*
		 * 框架读取文件 -----》 mapper ----》reducer
		 * 泛型的作用周期：
		 * 	编译时生效  运行时自动擦除
		 */
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(IntWritable.class);
		
		//指定reduce输出的类型  指定最终输出的
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		
		//指定输入路径
		FileInputFormat.addInputPath(job, new Path("D:\\doc\\url"));
		
		//指定输出路径
		Path path = new Path("D:\\doc\\url\\url_out");
		FileSystem fs = FileSystem.get(conf);
		if(fs.exists(path)) fs.mkdirs(path);
		FileOutputFormat.setOutputPath(job, new Path("D:\\doc\\url\\url_out"));
		
		//不会打印运行日志
		//job.submit();
		//参数 代表 是否打印日志
		job.waitForCompletion(true);
	}
}
