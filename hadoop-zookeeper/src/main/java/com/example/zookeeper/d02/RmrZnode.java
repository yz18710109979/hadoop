package com.example.zookeeper.d02;

import java.io.IOException;
import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

public class RmrZnode {

	public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
		ZooKeeper zk = new ZooKeeper(
				"hdp01:2181,hdp02:2181,hdp03:2181", 
				6000, 
				null);
		rmr(zk,"/aa");
	}

	private static void rmr(ZooKeeper zk, String path) throws KeeperException, InterruptedException {
		//获取给定路径的所有子节点
		List<String> children = zk.getChildren(path, null);
		if(children.size() == 0) {
			zk.delete(path, -1);
		}else {
			for (String s : children) {
				rmr(zk, path + "/" +s);
			}
			rmr(zk, path);
		}
	}
}
