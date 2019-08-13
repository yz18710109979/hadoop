package com.example.zookeeper.curator;
/**
 * 常用的zookeeper java客户端：
		zookeeper原生Java API
		zkclient
		Apache curator
 *ZooKeeper原生Java API的不足之处：

		在连接zk超时的时候，不支持自动重连，需要手动操作
		Watch注册一次就会失效，需要反复注册
		不支持递归创建节点
 *Apache curator：
		Apache 的开源项目
		解决Watch注册一次就会失效的问题
		提供的 API 更加简单易用
		提供更多解决方案并且实现简单，例如：分布式锁
		提供常用的ZooKeeper工具类
		编程风格更舒服，
 */

import java.util.List;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;

public class CuratorConnect {

	// Curator客户端
	public CuratorFramework client = null;

	public static final String zkServerIps = "hpd01:2181,hdp02:2181,hdp03:2181";

	public CuratorConnect() {
		/**
		 * 推荐） 同步创建zk示例，原生api是异步的 这一步是设置重连策略
		 *
		 * ExponentialBackoffRetry构造器参数： curator链接zookeeper的策略:ExponentialBackoffRetry
		 * baseSleepTimeMs：初始sleep的时间 maxRetries：最大重试次数 maxSleepMs：最大重试时间
		 */
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 5);
		/**
		 * 第二种，可设定重连n次：（推荐） curator链接zookeeper的策略:RetryNTimes
		 * 
		 * 构造器参数： n：重试的次数 sleepMsBetweenRetries：每次重试间隔的时间
		 */
//		RetryPolicy retryPolicy = new RetryNTimes(3, 5000);
		/**
		 * 第三种，只会重连一次（不推荐） curator链接zookeeper的策略:RetryOneTime
		 * 
		 * 构造器参数： sleepMsBetweenRetry:每次重试间隔的时间 这个策略只会重试一次
		 */
//		RetryPolicy retryPolicy2 = new RetryOneTime(3000);
		/**
		 * 第四种，永远重连：永远重试，不推荐使用
		 */
//		RetryPolicy retryPolicy3 = new RetryForever(retryIntervalMs)
		/**
		 * 第五种，可设定最大重试时间： curator链接zookeeper的策略:RetryUntilElapsed
		 * 
		 * 构造器参数： maxElapsedTimeMs:最大重试时间 sleepMsBetweenRetries:每次重试间隔
		 * 重试时间超过maxElapsedTimeMs后，就不再重试
		 */
//		RetryPolicy retryPolicy4 = new RetryUntilElapsed(2000, 3000);

		// 实例化Curator客户端，Curator的编程风格可以让我们使用方法链的形式完成客户端的实例化
		client = CuratorFrameworkFactory.builder()// 使用工厂类来建造客户端的实例对象
				.connectString(zkServerIps)// 放入zookeeper服务器ip
				.sessionTimeoutMs(1000).retryPolicy(retryPolicy) // 设定会话时间以及重连策略
				.namespace("workspace")// 设置命名空间
				.build();// 建立连接通道

		client.start();// 启动Curator客户端
	}

	// 关闭zk客户端连接
	public void closeZKClient() {
		if (null != client) {
			client.close();
		}
	}

	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws Exception {
		// 实例化
		CuratorConnect connect = new CuratorConnect();
		// 获取当前客户端的状态
		boolean started = connect.client.isStarted();
		System.out.println("当前客户端的状态：" + (started ? "连接中..." : "已关闭..."));

		// 创建节点
		String nodePath = "/super/testNode"; // 节点路径
		byte[] data = "this is a test data".getBytes(); // 节点数据
		
		String result = connect.client.create().creatingParentsIfNeeded() // 创建父节点，也就是会递归创建
				.withMode(CreateMode.PERSISTENT) // 节点类型
				.withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE) // 节点的acl权限
				.forPath(nodePath, data);

		System.out.println(result + "节点，创建成功...");
		
		// 添加 watcher 事件，当使用usingWatcher的时候，监听只会触发一次，监听完毕后就销毁
        connect.client.getData().usingWatcher(new MyCuratorWatcher()).forPath(nodePath);
        // curatorConnect.client.getData().usingWatcher(new MyWatcher()).forPath(nodePath);
        
        
     // NodeCache: 缓存节点，并且可以监听数据节点的变更，会触发事件
        final NodeCache nodeCache = new NodeCache(connect.client, nodePath);

        // 参数 buildInitial : 初始化的时候获取node的值并且缓存
        nodeCache.start(true);

        // 获取缓存里的节点初始化数据
        if (nodeCache.getCurrentData() != null) {
            System.out.println("节点初始化数据为：" + new String(nodeCache.getCurrentData().getData()));
        } else {
            System.out.println("节点初始化数据为空...");
        }

        // 为缓存的节点添加watcher，或者说添加监听器
        nodeCache.getListenable().addListener(new NodeCacheListener() {
            // 节点数据change事件的通知方法
            public void nodeChanged() throws Exception {
                // 防止节点被删除时发生错误
                if (nodeCache.getCurrentData() == null) {
                    System.out.println("获取节点数据异常，无法获取当前缓存的节点数据，可能该节点已被删除");
                    return;
                }
                // 获取节点最新的数据
                String data = new String(nodeCache.getCurrentData().getData());
                System.out.println(nodeCache.getCurrentData().getPath() + " 节点的数据发生变化，最新的数据为：" + data);
            }
        });
        
     // 为子节点添加watcher
        // PathChildrenCache: 监听数据节点的增删改，可以设置触发的事件
        final PathChildrenCache childrenCache = new PathChildrenCache(connect.client, nodePath, true);

        /**
         * StartMode: 初始化方式
         * POST_INITIALIZED_EVENT：异步初始化，初始化之后会触发事件
         * NORMAL：异步初始化
         * BUILD_INITIAL_CACHE：同步初始化
         */
        childrenCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);

        // 列出子节点数据列表，需要使用BUILD_INITIAL_CACHE同步初始化模式才能获得，异步是获取不到的
        List<ChildData> childDataList = childrenCache.getCurrentData();
        System.out.println("当前节点的子节点详细数据列表：");
        for (ChildData childData : childDataList) {
            System.out.println("\t* 子节点路径：" + new String(childData.getPath()) + "，该节点的数据为：" + new String(childData.getData()));
        }

        // 添加事件监听器
        childrenCache.getListenable().addListener(new PathChildrenCacheListener() {
            public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent event) throws Exception {
                // 通过判断event type的方式来实现不同事件的触发
                if (event.getType().equals(PathChildrenCacheEvent.Type.INITIALIZED)) {  // 子节点初始化时触发
                    System.out.println("\n--------------\n");
                    System.out.println("子节点初始化成功");
                } else if (event.getType().equals(PathChildrenCacheEvent.Type.CHILD_ADDED)) {  // 添加子节点时触发
                    System.out.println("\n--------------\n");
                    System.out.print("子节点：" + event.getData().getPath() + " 添加成功，");
                    System.out.println("该子节点的数据为：" + new String(event.getData().getData()));
                } else if (event.getType().equals(PathChildrenCacheEvent.Type.CHILD_REMOVED)) {  // 删除子节点时触发
                    System.out.println("\n--------------\n");
                    System.out.println("子节点：" + event.getData().getPath() + " 删除成功");
                } else if (event.getType().equals(PathChildrenCacheEvent.Type.CHILD_UPDATED)) {  // 修改子节点数据时触发
                    System.out.println("\n--------------\n");
                    System.out.print("子节点：" + event.getData().getPath() + " 数据更新成功，");
                    System.out.println("子节点：" + event.getData().getPath() + " 新的数据为：" + new String(event.getData().getData()));
                }
            }
        });
        
        
		// 更新节点数据
		byte[] newData = "this is a new data".getBytes();
		Stat resultStat = connect.client.setData().withVersion(0) // 指定数据版本
				.forPath(nodePath, newData); // 需要修改的节点路径以及新数据

		System.out.println("更新节点数据成功，新的数据版本为：" + resultStat.getVersion());

		// 删除节点
		connect.client.delete().guaranteed() // 如果删除失败，那么在后端还是会继续删除，直到成功
				.deletingChildrenIfNeeded() // 子节点也一并删除，也就是会递归删除
				.withVersion(resultStat.getVersion()).forPath(nodePath);
		
		
		// 读取节点数据
        Stat stat = new Stat();
        byte[] nodeData = connect.client.getData().storingStatIn(stat).forPath(nodePath);
        System.out.println("节点 " + nodePath + " 的数据为：" + new String(nodeData));
        System.out.println("该节点的数据版本号为：" + stat.getVersion());
        
        
       // 获取子节点列表
        List<String> childNodes = connect.client.getChildren().forPath(nodePath);
        System.out.println(nodePath + " 节点下的子节点列表：");
        for (String childNode : childNodes) {
            System.out.println(childNode);
        }

     // 查询某个节点是否存在，存在就会返回该节点的状态信息，如果不存在的话则返回空
        Stat statExist = connect.client.checkExists().forPath(nodePath);
        if (statExist == null) {
            System.out.println(nodePath + " 节点不存在");
        } else {
            System.out.println(nodePath + " 节点存在");
        }
		Thread.sleep(1000);

		// 关闭客户端
		connect.closeZKClient();

		// 获取当前客户端的状态
		started = connect.client.isStarted();
		System.out.println("当前客户端的状态：" + (started ? "连接中..." : "已关闭..."));
	}
}
