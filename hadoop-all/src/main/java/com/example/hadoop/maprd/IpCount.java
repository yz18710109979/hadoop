package com.example.hadoop.maprd;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

//2.两个超级大的文件   ip   每行一个    两个文件中的相同的ip
public class IpCount {
	
	static ThreadPoolExecutor executeService = 
			new ThreadPoolExecutor(3, 6, 10, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>());

	public static void main(String[] args) throws Exception {
		//=============如果是小文件=========================
//		Future<Set<String>> set1 = executeService.submit(new Callable<Set<String>>() {
//			public Set<String> call() throws Exception {
//				Set<String> set1 = new HashSet<String>();
//				BufferedReader br1 = new BufferedReader(new FileReader("D:\\doc\\ip\\ip1.txt"));
//				String line1 = null;
//				while ((line1 = br1.readLine())!=null) {
//					set1.add(line1);
//				}
//				br1.close();
//				return set1;
//			}
//		});
//		System.out.println(set1.get());
//		Future<Set<String>> set2 = executeService.submit(new Callable<Set<String>>() {
//			public Set<String> call() throws Exception {
//				Set<String> set2 = new HashSet<String>();
//				BufferedReader br2 = new BufferedReader(new FileReader("D:\\doc\\ip\\ip2.txt"));
//				String line2 = null;
//				while ((line2 = br2.readLine())!=null) {
//					set2.add(line2);
//				}
//				br2.close();
//				return set2;
//			}
//		});
//		System.out.println(set2.get());
//		Set<String> resset = new HashSet<String>();
//		for (String s : set1.get()) {
//			if(set2.get().contains(s)) {
//				resset.add(s);
//			}
//		}
//		System.out.println(resset.toString());
		//================================如果是大文件=========================
	}
}
