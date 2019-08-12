package com.example.zookeeper.d01;

import java.io.IOException;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

public class TestZKWithWatcher {

	public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
		//获取连接
		//需要监听  参数3 监听器对象
		//创建对象  如果传入了监听器对象   会进行测试  测试监听process是否被调用
		ZooKeeper zk = new ZooKeeper(
				"hdp01:2181,hdp02:2181,hdp03:2181",
				60000, 
				new Watcher() {
					//回调方法，事件触发之后调用的方法
					//参数：WatchedEvent监听事件类型，封装事件类型|路径|状态等信息
					public void process(WatchedEvent event) {
						//获取事件类型
						EventType type = event.getType();
						//事件触发的节点
						String path = event.getPath();
						System.out.println(type + ":" + path);
					}
				});
		//添加监听 ls-getChildren get-getData exists
		/*
		 * 监听相关参数
		 * 1）boolean   是否添加监听  是 true 否  false
		 * 	true 监听触发的时候 调用的是zk对象的监听器  process 
		 * 	true zk对象创建的时候  必须传入监听器对象实例
		 * 2）Watcher
		 */
		zk.exists("/dd", true);
		//2.触发监听
		//nodecreated
//		zk.create("/dd", "".getBytes(), Ids.OPEN_ACL_UNSAFE, 
//				CreateMode.PERSISTENT);
		//NodeDataChanged
//		zk.setData("/dd", "aa".getBytes(), -1);
		//nodedeleted 
		zk.delete("/dd", -1);
	}
}
