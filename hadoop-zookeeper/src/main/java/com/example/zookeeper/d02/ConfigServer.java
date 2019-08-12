package com.example.zookeeper.d02;

import java.io.IOException;
import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooKeeper;

public class ConfigServer {
	static String p = "/config";
	static ZooKeeper zk = null;
	static List<String> children = null;
	public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
		zk = new ZooKeeper(
				"hdp01:2181,hdp02:2181,hdp03:2181",
				6000,
				new Watcher() {
					public void process(WatchedEvent event) {
						//触发监听
						EventType type = event.getType();
						String path = event.getPath();
						System.out.println(type + "@@@" + path);
						if(EventType.NodeChildrenChanged.equals(type)) {
							//判断是否nodecreated
							try {
								List<String> new_children = zk.getChildren(path, true);
								//判断两个集合的节点个数
								if(new_children.size() > children.size()) {
									String new_node = getDiff(children,new_children);
									String content = new String(zk.getData(p+"/"+new_node, true, null));
									//nodecreated
									System.out.println("添加了一个配置文件，配置文件名"+new_node+
											",这个配置文件的内容是"+content);
									children = new_children;
								}
							} catch (KeeperException e) {
								e.printStackTrace();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}else if(EventType.NodeDeleted.equals(type)) {
							System.out.println("删除了一个配置文件，配置文件名"+path);
							try {
								children = zk.getChildren(p, true);
							} catch (KeeperException e) {
								e.printStackTrace();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}else if(EventType.NodeDataChanged.equals(type)) {
							String con = "";
							try {
								 con= new String(zk.getData(p, true, null));
							} catch (KeeperException e) {
								e.printStackTrace();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							System.out.println("修改了配置文件"+path+"的内容，修改之后的内容为"+con);
						}
					}
				});
		//添加监听
		//获去  所给节点的所有子节点   nodechildrenchanged--- nodecreated
		children = zk.getChildren(p, true);
		
		//循环遍历每一个子节点，添加监听 nodedeleted nodedatachanged
		for (String c : children) {
			zk.getData(p+"/" + c, true, null);
		}
		Thread.sleep(1000000);
	}
	private static String getDiff(List<String> children, List<String> new_children) {
		String res = "";
		for (String s : new_children) {
			if(!children.contains(s)) {
				res = s;
				break;
			}
		}
		return res;
	}
}
