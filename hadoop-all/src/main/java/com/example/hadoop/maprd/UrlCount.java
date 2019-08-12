package com.example.hadoop.maprd;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.stat.inference.TestUtils;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

//1.一个超级大的文件   里面存储url  一行一个    求出现次数最多的url
public class UrlCount {
	
	static class MyMapper extends Mapper<LongWritable, Text, Text, LongWritable>{
		Text mk = new Text();
		@Override
		protected void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			
		}
	}

	public static void main(String[] args) throws IOException {
		Map<String,Integer> map = new HashMap<String,Integer>();
		BufferedReader br = new BufferedReader(new FileReader("D:\\doc\\url\\url.txt"));
		String line = null;
		while((line = br.readLine()) != null) {
			if(!map.containsKey(line)) {
				map.put(line, 1);
			}else {
				Integer value = map.get(line) + 1;
				map.put(line, value);
			}
		}
		System.out.println(map);
		br.close();
		//======================如果是大文件，基于maprd实现=============================
	}
}
