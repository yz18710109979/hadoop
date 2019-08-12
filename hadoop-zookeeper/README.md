zookeeper原理
===========================================
zookeeper中的角色：
	leader :
		发起提议
		接受客户端的读写请求  主要处理写请求
		具有选举权 和 被选举权
		时刻具备最新数据
	follower
		接受客户端的读写请求  读请求可以自己处理  如果接受到客户端的写请求  将写请求转发给leader 
		具备选举权  和 被选举权
	observer 
		配置  server.4=hadoop04:2888:3888：observer
		不具备选举权 和被选举权   follower 
		作用：分担集群的读数据的压力  接受到写请求  转发leader 
		95  5
		
整个写入数据过程中  leader最先写入  follower|observer 从leader进行数据同步
follower|observer 数据更新过程  leader宕机了  重新选主

写数据过程：
1）客户端写入zk数据时候 请求最终被leader进行处理
2）leader进行数据写入  
3）leader写入数据成功 不代表数据真的写入成功的  follower | observer 进行数据同步 同上面
4）当过半的follower更新数据成功   leader将这条数据标记为真正的可读数据  剩下的机器自己慢慢更新


详解stat信息：
cZxid = 0x200000075
ctime = Mon Aug 05 23:52:15 CST 2019  节点创建时间
mZxid = 0x200000075
mtime = Mon Aug 05 23:52:15 CST 2019  节点修改时间
pZxid = 0x20000007e
cversion = 6  创建版本
dataVersion = 0  数据版本
aclVersion = 0  权限版本
ephemeralOwner = 0x0  节点的生命周期标识
	永久节点   0x0 
	ephemeralOwner = 0x16c5d8f0a950009  临时节点 
	sessionid = 0x16c5d8f0a950009   
dataLength = 0   数据长度
numChildren = 6  子节点个数

数据版本id 数据版本越大   数据越新  全局的
cZxid  创建节点事件id 
	创建节点  +1 
mZxid  修改节点事件id 
	修改节点内容 发生变化
pZxid	子节点变化的事件id 
	创建一个节点的子节点的时候   发生变化
	
cZxid   mZxid  pZxid  整体无论修改了哪一个  都会全局顺序递增
上面的3个id 共同标识  整个集群中的这个机器  数据更新程度
只要zxid 最大  这个机器中的数据最新的


非全新集群选主：
	集群运行一段时间 之后    leader 宕机    集群的重新选主
	依据：
	1）myid 
	2）zxid 
	3）逻辑时钟  投票的轮数
	
	1）先根据逻辑时钟  逻辑时钟不统一  先统一逻辑时钟
	2）统一完成逻辑时钟  按照zxid  zxid大的胜出
	3）在zxid大的里面选myid 大的胜出
	最终选的leader   数据版本最新的
	
数据同步过程：
	follower|observer 的数据和leader的数据保持一致
	1、leader 等待 follower|observer 连接；
	2、follower|observer 连接 leader，将最大的 zxid 发送给 leader；
	3、leader 根据 follower 的 zxid 确定是否需要更新数据，如果follwer   zxid < leader zxid需要数据同步，确定数据更新同步点；
	4、follower 进行数据更新，follower 将自己的状态 update 状态，不在接受客户端的读数据请求；
	5、follower更新完成数据，将自己的状态改为updated ，又可以重新接受 client 的请求进行服务了。
	
	
	follwer   zxid == leader zxid  不需要更新数据
	follwer   zxid < leader zxid   需要更新数据
				fzixd+1  --- lzxid 

namenode的高可用设计
	hadoop2.0 一个集群中  可以设计2个namenode的  这两个namenode 
	同一时间只有一个namenode对外提供服务的   将这个namenode 称为 active namenode   
	另一个namenode 处于热备份的状态  standby namenode   一旦active宕机的时候  standby 就会立即无缝切换 active namenode   
	对于客户端觉得集群24h处于对外提供服务状态
	
文件系统
----------------------------
1）zk的文件系统同linux  以  /开始的
2）zk只有绝对路径访问方式  没有相对路径的访问方式  所有访问路径必须从 /开始
3）zk中没有文件的概念  也没有目录的概念  里面只有节点znode 
znode既有文件的功能  又有目录的功能
4）znode 分类
create [-s] [-e] path data
按照生命周期分：
	临时节点：
		创建：create -e 节点路径 节点存储内容
			e--->Ephemerals
			create -e /test01 "hello"
		临时节点只对当前客户端生效   当前客户端退出  临时节点被zk删除
		临时节点不可以有子节点
	永久节点：
		创建：create 节点路径 节点存储内容
		create /test02 "world"
		永久节点对所有客户端生效   不会自动删除   要想删除必须手动删除
		永久节点可以有子节点
		
		有子节点的节点一定是永久节点
		
按照有无编号：
	有编号节点
		创建：create -s path data
			creare -s /test ""
		这个节点创建的时候 自动添加一个编号
		编号是由父节点维护的  0开始顺序递增的（无论有无编号  编号都会顺序递增）
		同一个节点  可以重复创建  每次都是不同编号
		有编号节点  访问（查看 删除）都需要加上编号
	无编号节点
		创建：create path data 
			create /bb ""
		无编号节点只能创建一次
节点分类：
	永久无编号
		create path data 
	永久有编号
		create -s path data
	临时无编号
		create -e path data
	临时有编号
		create -e -s path data
		
5）zk来说有几个节点  数据就会被存储几份
zk中每一个节点存储的数据都是一致的
只要在一个节点进行操作  其他节点自动进行数据同步

6）zookeeper中znode存储数据的时候  每一个znode存储的数据  不要超过1M  最好不要超过1kb

7）zk中监听对象必须是znode 

监听机制
------------------------------
客户端对某一个znode感兴趣  这个时候 可以对这个znode添加监听
1）监听事件  -- 感兴趣的事件
	子节点变化  nodechildrenchanged 
	内容变化	nodedatachanged
	节点被删除	nodedeleted 
	节点创建	nodecreated 
	
2）添加监听   关注事件
	shell watch 
	ls path watch   ---> 子节点变化nodechildrenchanged 
	get path watch ----> 节点内容变化   nodedatachanged
	exists    --->   nodecreated  nodedeleted
3）触发监听
	create 
	delete rmr
	set path data
	
zk的核心就是  用监听机制 监听文件系统   分布式数据一致性