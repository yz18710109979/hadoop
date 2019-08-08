package com.example.hadoop.maprd.singleton;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WordCount {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException {
		Map<String, Integer> map01 = readOneFile("D:\\doc\\wc\\1.txt");
		System.out.println(map01);
		Map<String, Integer> map02 = readOneFile("D:\\doc\\wc\\2.txt");
		System.out.println(map02);
		Map<String, Integer> map03 = readOneFile("D:\\doc\\wc\\3.txt");
		System.out.println(map03);
		Map<String, Integer> map04 = readOneFile("D:\\doc\\wc\\4.txt");
		System.out.println(map04);
		Map<String, Integer> map05 = readOneFile("D:\\doc\\wc\\5.txt");
		System.out.println(map05);
		Map<String, Integer> result = merge(map01,map02,map03,map04,map05);
		System.out.println(result);
	}
	@SuppressWarnings("resource")
	public static Map<String,Integer> readOneFile(String path) throws IOException{
		Map<String,Integer> map = new HashMap<String, Integer>();
		BufferedReader br = new BufferedReader(new FileReader(path));
		String line = null;
		while((line = br.readLine()) != null) {
			String[] words = line.split("\t");
			String key = words[0];
			if(!map.containsKey(key)) {
				map.put(key, 1);
			}else {
				Integer newvalue = map.get(key) + 1;
				map.put(key, newvalue);
			}
		}
		return map;
	}
	public static Map<String,Integer> merge(Map<String,Integer>...maps){
		Map<String,Integer> resmap = new HashMap<String, Integer>();
		for (Map<String, Integer> map : maps) {
			Set<String> keys = map.keySet();
			for (String key : keys) {
				if(!resmap.containsKey(key)) {
					resmap.put(key, map.get(key));
				}else {
					resmap.put(key, map.get(key) + resmap.get(key));
				}
			}
		}
		return resmap;
	}
}
