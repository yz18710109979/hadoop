写数据过程：
1）客户端写入zk数据时候 请求最终被leader进行处理
2）leader进行数据写入  
3）leader写入数据成功 不代表数据真的写入成功的  follower | observer 进行数据同步 同上面
4）当过半的follower更新数据成功   leader将这条数据标记为真正的可读数据  剩下的机器自己慢慢更新


namenode的高可用设计
	hadoop2.0 一个集群中  可以设计2个namenode的  这两个namenode 
	同一时间只有一个namenode对外提供服务的   将这个namenode 称为 active namenode   
	另一个namenode 处于热备份的状态  standby namenode   一旦active宕机的时候  standby 就会立即无缝切换 active namenode   
	对于客户端觉得集群24h处于对外提供服务状态