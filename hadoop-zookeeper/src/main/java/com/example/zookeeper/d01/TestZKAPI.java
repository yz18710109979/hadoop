package com.example.zookeeper.d01;

import java.io.IOException;
import java.util.List;

import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class TestZKAPI {

	public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
		ZooKeeper zk = 
				new ZooKeeper("hdp01:2181,hdp02:2181,hdp03:2181", 6000, null);
		//进行操作
		//1.创建节点 create -e -s path data
		//参数1：节点路径 参数2：存储数据 参数3：权限 参数4：节点类型
//		String create = zk.create(
//				"/test_api", 
//				"hello world".getBytes(), 
//				Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
//		System.out.println(create);
		//创建临时顺序节点
//		String e_create = zk.create(
//				"/test_e", 
//				"".getBytes(), 
//				Ids.OPEN_ACL_UNSAFE, 
//				CreateMode.EPHEMERAL_SEQUENTIAL);
//		System.out.println(e_create);
		
		//2.查看节点
		//查看所有子节点，ls 参数1：路径 参数2：监听 null
//		List<String> childrens = zk.getChildren("/", null);
//		for (String s : childrens) {
//			System.out.println(s);
//		}
		//查看节点的内容 get 参数1：路径 参数2：监听 参数3：状态对象 null
		byte[] data = zk.getData("/test03", null, null);
		String ss = new String(data);
		System.out.println(ss);
		
		//3.判断节点是否存在
		//如果存在则返回节点的状态信息  如果不存在返回null 
//		if(zk.exists("/test02", null) != null) {
//			System.out.println("存在");
//		}else {
//			System.out.println("不存在");
//		}
		
		//4.删除节点 参数1：节点路径 参数2：版本 -1 不能级联删除
//		zk.delete("/test02", -1);
		
		//5.修改节点内容  参数1：路径 参数2：内容 参数3：版本
//		Stat ss = zk.setData("/test03", "tom and jerry".getBytes(), -1);
//		System.out.println(ss.toString());
	}
}
