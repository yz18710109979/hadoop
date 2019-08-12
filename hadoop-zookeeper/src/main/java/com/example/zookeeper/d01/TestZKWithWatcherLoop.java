package com.example.zookeeper.d01;

import java.io.IOException;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

public class TestZKWithWatcherLoop {
	static ZooKeeper zk = null;
	public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
		//获取连接
		//需要监听  参数3 监听器对象
		//创建对象  如果传入了监听器对象   会进行测试  测试监听process是否被调用
		//顺序1
		zk=new ZooKeeper(
				"hdp01:2181,hdp02:2181,hdp03:2181", 
				6000, 
				new Watcher() {
					public void process(WatchedEvent event) {
						//获取事件类型
						EventType type = event.getType();
						String path = event.getPath();
						System.out.println(type + "@@@" + path);
						//添加监听
						if(EventType.NodeDeleted.equals(type)) {
						}
						try {
							zk.getChildren("/aa", null);
						} catch (KeeperException e) {
							e.printStackTrace();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
			});
		//1.添加监听  ls--getChildren   get---getData   exists
		/*
		 * 监听相关参数
		 * 1）boolean   是否添加监听  是 true 否  false
		 * 	true 监听触发的时候 调用的是zk对象的监听器  process 
		 * 	true zk对象创建的时候  必须传入监听器对象实例
		 * 2）Watcher
		 * 	如果zk对象  也传入监听器对象  这个时候调用的是谁的？
		 * 	这里的 方法上的监听器对象
		 * 	zk对象的监听器就可以取消了  null 
		 */
		//顺序2 
		zk.getChildren("/aa", true);
		//2.触发监听
		//nodecreated
		/*zk.create("/aa", "".getBytes(), Ids.OPEN_ACL_UNSAFE, 
				CreateMode.PERSISTENT);*/
		//NodeDataChanged
		//zk.setData("/dd", "aa".getBytes(), -1);
		//nodedeleted 
		//zk.delete("/dd", -1);
		//NodeDeleted
		//zk.delete("/aa", -1);
		//顺序3
		zk.create(
				"/aa/bb1", 
				"bb1".getBytes(),
				Ids.OPEN_ACL_UNSAFE, 
				CreateMode.PERSISTENT);
		//顺序5   连续触发  之前把监听添加上
		zk.create("/aa/cc1", "".getBytes(), Ids.OPEN_ACL_UNSAFE, 
				CreateMode.PERSISTENT);
		zk.create("/aa/dd1", "".getBytes(), Ids.OPEN_ACL_UNSAFE, 
				CreateMode.PERSISTENT);
	}
}
