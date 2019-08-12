package com.example.hadoop.maprd.maptask;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
	map端：
		key：手机号   Text
		value:上行流量 int+下行流量  int+总流量 int  自定义类  FlowBean
		自定义类：
			upflow
			downflow
			sumflow
	reduce端：
		相同手机号所有流量
		key  手机号   values：所有流量  自定义类
		循环遍历   累加
 * @author aura-bd
 *
 */
public class FlowCount {

	static class MyMapper extends Mapper<LongWritable, Text, Text, FlowBean>{
		Text mk = new Text();
		protected void map(LongWritable key, Text value, Context context) throws IOException ,InterruptedException {
			String line = value.toString();
			System.out.println("每行的数据：" + line);
			String[] flow_data = line.split("\t");
			if(flow_data.length == 11) {
				mk.set(flow_data[1]);
				FlowBean fb = new FlowBean(
						Integer.parseInt(flow_data[flow_data.length-3]),
						Integer.parseInt(flow_data[flow_data.length-2]));
				context.write(mk, fb);
			}
		};
	}
	
	static class MyReducer extends Reducer<Text, FlowBean, Text, FlowBean>{}
	public static void main(String[] args) throws Exception {
		//获取集群的配置文件
		Configuration conf = new Configuration();
		//启动job，构建job对象
		Job job = Job.getInstance(conf);
		//进行job的封装，指定jar包运行的主类
		job.setJarByClass(FlowCount.class);
		//指定mapper和reduce对应的类
		job.setMapperClass(MyMapper.class);
		job.setReducerClass(MyReducer.class);
		//指定map输出的  k v的类型
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(FlowBean.class);
		//指定reducer输出的类型，最终输出类型
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(FlowBean.class);
		//指定输入文件路径
		FileInputFormat.addInputPath(job, new Path(""));
		
		//指定输出路径
		Path path = new Path("");
		FileSystem fs = FileSystem.get(conf);
		if(fs.exists(path)) fs.mkdirs(path);
		FileOutputFormat.setOutputPath(job, path);
		
		//打印日志
		job.waitForCompletion(true);
	}
}
