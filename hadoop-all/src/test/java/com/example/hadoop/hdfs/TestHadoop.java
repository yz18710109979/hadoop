package com.example.hadoop.hdfs;

import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestHadoop {

	FileSystem fs = null;
	@Before
	public void before() throws Exception {
		Configuration conf = new Configuration();
		fs = FileSystem.get(
				new URI("hdfs://hdp01:9000"), conf, 
				"hadoop");
	}
	@Test
	public void test1() {
		String result = FileUtils.up(
				fs, 
				"D:\\BaiduNetdiskDownload\\Subversive-connectors-4.1.3.I20150214-1700.zip",
				"/");
		System.out.println(result);
	}
	
	@Test
	public void test2() {
		String result = FileUtils.download(
				fs,
				"/Subversive-connectors-4.1.3.I20150214-1700.zip",
				"D:\\SVN");
		System.out.println(result);
	}
	
	@After
	public void after() throws Exception {
		fs.close();
	}
}
