package com.example.hadoop.maprd;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

//3.一个超级大的文件   url  一行一个   用户给定一个url   如何能够快速判断这个url是在这个文件中 
public class FindUrl {

	public static void main(String[] args) throws Exception {
		//=================如果是小文件=================================
		BufferedReader br = new BufferedReader(new FileReader("D:\\doc\\find_url\\find_url.txt"));
		String line = null;
		Set<String> set = new HashSet<String>(); 
		while((line = br.readLine()) != null) {
			set.add(line);
		}
		Scanner sc = new Scanner(System.in);
		System.out.println("请输入：");
		String string = sc.next();
		System.out.println("输入的是：" + string);
		if(set.contains(string)) {
			System.out.println(string + "存在文件");
		}else {
			System.out.println("不存在");
		}
	}
}
