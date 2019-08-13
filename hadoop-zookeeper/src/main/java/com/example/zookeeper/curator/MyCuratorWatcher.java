package com.example.zookeeper.curator;

import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.WatchedEvent;

public class MyCuratorWatcher implements CuratorWatcher  {
	// Watcher事件通知方法
	public void process(WatchedEvent watchedEvent) throws Exception {
		System.out.println("触发watcher，节点路径为：" + watchedEvent.getPath());
	}

}
