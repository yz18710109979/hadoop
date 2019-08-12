package com.example.zookeeper.d02;

import java.io.IOException;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

public class ConfigClient {

	public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
		//获取连接
		ZooKeeper zk=new ZooKeeper(
				"hdp01:2181,hdp02:2181,hdp03:2181", 
				6000,
				null);
		//nodecraeted   创建一个新的配置文件
//		zk.create(
//				"/config/mapred05", 
//				"rr".getBytes(), 
//				Ids.OPEN_ACL_UNSAFE, 
//				CreateMode.PERSISTENT);
		
		//nodedatachenge 
//		zk.setData("/config/yarn", "tt01".getBytes(), -1);
		
		
		//nodedeleted 
		zk.delete("/config/mapred01", -1);
		
		
	}
}
