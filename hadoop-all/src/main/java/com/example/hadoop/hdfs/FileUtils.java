package com.example.hadoop.hdfs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;

public class FileUtils {

	public static String up(FileSystem fs,String src,String dst) {
		try {
			fs.copyFromLocalFile(new Path(src), new Path(dst));
			return "success";
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String download(FileSystem fs,String src,String dst) {
		try {
			FSDataInputStream in = fs.open(new Path(src));
			FileOutputStream out = new FileOutputStream(new File(dst));
			IOUtils.copyBytes(in, out, 4096);
			return "success";
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
