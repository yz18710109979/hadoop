package com.example.zookeeper.d01;

import java.io.IOException;

import org.apache.zookeeper.ZooKeeper;

public class TestZKConnect {

	public static void main(String[] args) throws Exception {
		//创建连接
		/*
		 * 参数1：zk连接url 
		 * 		主机：2181，主机：2181
		 * 参数2：连接超时时间  ms
		 * 参数3：监听器  watcher  监听器对象 反馈监听结果的  不需要  null
		 */
		ZooKeeper zk=new ZooKeeper("hdp:2181,hdp02:2181,hdp03:2181", 
				6000, null);
		System.out.println(zk);
		zk.close();
	}
}
