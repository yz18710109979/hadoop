hdfs

maprd
maptask的并行度  面试
========================================
maptask：运行mapper端逻辑的任务
并行度：有多少个maptask 需要一起运行
maptask 并行度  ：mapper端那段逻辑  在进行运行的时候  需要拆分多少个任务（task）
这里的一个task是job运行的最小单位   不可拆分  一个task只能在一个节点运行

相关因素：
	每一个任务 对应一部分原始数据
	这个原始数据  到底应该多大：
		存储：hdfs的存储默认128M 一个块
	如果计算的时候：
		一个任务对应的数据  1G 
		获取数据的时候  hdfs 8个块  可能在存储 会被分散到不同节点
		必然面临  数据的跨节点传输  降低计算的效率
		
		一个任务对应的数据  100M 
		task1---blk1  0-100M
		task2---blk1  100-128M   blk2 129--200M 
		仍然会造成跨节点数据传输问题 降低计算的效率
		
		一个任务对应的数据 最合理的应该就是和hdfs数据存储的块的大小一致  128M 

注意：这里的每一个任务只会被分配到每一个节点的一小部分资源
一个节点上可以执行多个任务（maptask|reducetask）的  一个任务只能在一个节点执行

事实上  一个maptask任务对应的数据量 一个切片（SPLIT）的大小
推断： 一个切片大小  理论上  一个block块的大小
切片：
	逻辑上的概念 代表的仅仅是一个范围的划分  针对maptask的计算
	每一个maptask 对应  一个逻辑切片的数据
	
源码实现：
	FileInputFormat
		/*
	   * 128   1   long_max
	   */
	  protected long computeSplitSize(long blockSize, long minSize,
									  long maxSize) {
		return Math.max(minSize, Math.min(maxSize, blockSize));
	  }
	  
	最终 splitSize 大小  blockSize
	是否可以修改：
		1)splitSize > blockSize   128M 
		修改  minSize
		2) splitSize <blockSize  128M 
		修改  maxSize
		
		实践：
		1）修改配置文件  mapred-site.xml
		不建议使用
		minSize  mapreduce.input.fileinputformat.split.minsize
		<property>
		  <name>mapreduce.input.fileinputformat.split.minsize</name>
		  <value>140</value>
		  <description>The minimum size chunk that map input should be split
		  into.  Note that some file formats may have minimum split sizes that
		  take priority over this setting.</description>
		</property>
		
		maxSize  mapreduce.input.fileinputformat.split.maxsize
		<property>
		  <name>mapreduce.input.fileinputformat.split.maxsize</name>
		  <value>100</value>
		  <description>The minimum size chunk that map input should be split
		  into.  Note that some file formats may have minimum split sizes that
		  take priority over this setting.</description>
		</property>
		
		2）代码中修改
		提倡
		FileInputFormat.setMaxInputSplitSize(job, 100*1024*1024);
		FileInputFormat.setMinInputSplitSize(job, 200*1024*1024);

补充：
	1）如果文件大小不够一个blockSize大小  也是独自成一个切片
	2）130M 一个文件  对应几个切片呢？ 1
		blk0  0-128M 
		blk1  129-130M
		最后一个逻辑切片大小   最大  128*1.1   140.8M 
		((double) bytesRemaining)/splitSize > SPLIT_SLOP
=========================================
mapreduce编程套路：
	数据加载读取：
		FileInputFormat
			TextInputFormat
				RecordReader
					LineRecordReader
						LongWritable getCurrentKey
						Text getCurrentValue
	
	Mapper<>
		map(LongWritable key,Text value,Context context){
			获取
			拆分
			封装
			发送
			context.write(k,v)
		}
	shuffle:
		分区   决定reducetask的并行度
		排序   默认按照mapkey  排序
		分组   map key 
			key 
			values
		
		combiner  默认没有   局部聚合组件  对每一个maptask的输出结果做聚合的
	Reducer
		reduce(key,values,context){
			context.write(k,v)
		}
	输出：
		FileOutputFormat
			TextOutputFormat
				RecordWriter
					LineRecordWriter
自定义类   不能放在map key 其他位置都可以
=============================================
hadoop中有自己的一套序列化  反序列化的  工具
常用类型  hadoop已经实现了
	IntWritable
	LongWritable
	Text 
	DoubleWwritable
	.....
内置的类型  无法满足需求   自定义类型
自定义类型   网络传输   持久化磁盘
自定义类型
	具备序列化和反序列化的能力
	
内置类型  如何实现：
	public class IntWritable implements WritableComparable<IntWritable>
	public class LongWritable implements WritableComparable<LongWritable>
	public class Text extends BinaryComparable
    implements WritableComparable<BinaryComparable>
	
	
	public interface WritableComparable<T> extends Writable, Comparable<T> {
			}
	WritableComparable<T>   
		Writable    序列化  反序列化的接口
		Comparable  比较

序列化  反序列化  接口  Writable 
	void write(DataOutput out) throws IOException;
	void readFields(DataInput in) throws IOException;
	
hadoop中的自定义类：
	1）实现  Writable接口
	2）重写  
		write    对象----二进制  序列化
		readFields   二进制---》对象   反序列化
		
		
	注意：
	1）一定给无参构造
	2）一定要给  toString 
排序   
=========================================
排序  shuffle过程
mapreduce中 默认情况下是否有排序：
	有排序
	默认按照map输出的key 进行排序
	如果map key
		Text  默认按照字典顺序排序的  升序  compareTo
		数值  默认按照值  从小到大排序的  compareTo方法
		
案例：	
	对wc的结果  按照单词的词频进行  降序排序
	hadoop  24
	hello   10
	hive    17
	lily    7
	spark   18
	word    6
	ww      4
	
	如果按照哪一个进行排序  哪一个必须在map key 
	map端：
		key：-词频  intwritable
		value：单词  text
	shuffle：
		按照-词频排序  升序
	reduce：
		排好序的数据
		顺序输出结果--词频

mapreduce中  在shuffle中 需要按照map输出的key进行排序的
	要求map key 具备排序能力
	
hadoop中的内置类型：
	public class IntWritable implements WritableComparable<IntWritable>
	public class LongWritable implements WritableComparable<LongWritable>
	public class Text extends BinaryComparable
    implements WritableComparable<BinaryComparable>
	
	WritableComparable  Writable   Comparable
	hadoop中默认内置类型  具备排序能力的 
	所以 mapreduce的时候   使用内置类型  在shuffle中进行正常排序的
	
如果使用自定义的类  放在map输出的key：
	这个自定义类   
		具备排序能力Comparable   
		具备序列化反序列能力  Writable
		
	自定义类  放在map key 位置  必须实现  WritableComparable的接口
		1）Comparable
			compareTo()  指定排序规则
		2）Writable
			write
			readFields
默认类型：  Text  IntWritable  LongWritable  DoubleWwritable...
	只能进行单一元素 全排序
	map key  Text 按照字符串的最后两位排序  Text不满足排序需求的
	原因：
		默认类型的排序 compareTo方法定义好的
		
	默认类型的排序规则  无法满足需求   自定义类型
		
			
案例:
1）
对流量的结果  按照总流量  降序排序
	map：
		key：
			-总流量
		value：其他
	reduce：
		顺序  - 输出
		
2）对流量的结果 先按照总流量降序  总流量相同 按照上行流量升序排 
	map：
		key: 总流量 降 +  上行 升
			自定义类  （总流量  上行流量  -- 手机号 下行流量）
			1)实现   WritableComparable
			2）重写  compareTo   write   readFields
		value:NullWritable
	reduce:
		排好序  分好组的数据
		循环遍历输出
		
	二次排序--- 自定义排序


reducetask并行度----分区
============================================================
reducetask并行度：运行reducer逻辑的  任务的并行运行的个数
如果map输出的数据  数据量很大   到reduce端进行执行的时候  reduce端同样需要启动多个任务  并行运行

reducetask的并行度和什么有关？
	1）默认情况下
	<property>
	  <name>mapreduce.job.reduces</name>
	  <value>1</value>
	  <description>The default number of reduce tasks per job. Typically set to 99%
	  of the cluster's reduce capacity, so that if a node fails the reduces can 
	  still be executed in a single wave.
	  Ignored when mapreduce.jobtracker.address is "local".
	  </description>
	</property>
	默认情况下  只启动一个reducetask任务的
	2）数据量大的时候  启动多个reducetask 
	代码中指定
	job.setNumReduceTasks(3);
	参数指定的是几  最终就会执行几个reducetask 
	每一个reducetask -- yarnchild
	reducetask reduce 端运行任务的最小单位 
	一个reducetask只能运行在一个节点  一个节点上可以运行多个reducetask的
	
	如果启动多个reducetask   会不会造成同一个组的数据分散到不同的reducetask中？
		不会造成
	对map输出的结果 在shuffle阶段  进行数据分发的时候  有一个策略 这个策略中既可以按照reducetask的个数将数据分成对应的份数  又不会将map输出的相同的key 进行分发到不同的reducetask中
	
	这个分发策略  在mapreduce中  分区算法
	对map输出结果  进行分发过程  就是分区的过程   
	这里的分区  是为了给reducetask做数据准备的  所以有几个reducetask 在shuffle中就会分几个分区
	
	推断分区算法：
		(mapkey .hash & Integer_max)  % 分区个数（reducetask的个数）
	源码：
		默认分区类：Partitioner
			 public abstract int getPartition(KEY key, VALUE value, int numPartitions);
			实现：HashPartitioner
				//核心方法  分区算法
				/*参数：K key  map输出的key, V value  map输出的value   
					numReduceTasks 分区个数  job.setNumReduceTasks()
					
				返回值：分区编号  0---》 numReduceTasks-1 最终相同分区编号的数据在同一个分区
				*
				/
			  public int getPartition(K key, V value,
                          int numReduceTasks) {
				return (key.hashCode() & Integer.MAX_VALUE) % numReduceTasks;
			  }
	
			经过分区之后  不同的分区的数据 内部进行排序  分组  最终这个数据给不同的reducetask进行处理的
			
			最终运行结果：
			有几个reducetask 就有几个结果文件 每一个reducetask都会输出一个文件
			_SUCCESS   成功标志
			reducetask0 ---->part(partition)-r(reducetask)-00000(编号)
			reducetask1---->part-r-00001
			reducetask2---->part-r-00002
			
			reducetask的编号是根据  分区编号来的
	
默认分区算法中   没有办法进行制定对应的数据  到对应的分区中的

案例：

要求将手机号的总流量结果  按照不同的手机号的归属地  将最终结果输出到不同文件

	bj --- part-r-00000
	sz --- part-r-00001
	默认的分区算法  无法实现  

	自定义分区
	1）定义一个类  继承  Partitioner
	2）重写 getPartition
	3）在job中  制定自定义分区类
		job.setPartitionerClass(cls); 
	4）制定reducetask的个数  不指定默认值运行一个
		job.setNumReduceTasks(4);
	人为规定：
		134---136   bj  part-r-00000
		137---138   sz  part-r-00001
		139---159   wh  part-r-00002
		其他    未名	part-r-00003
		4reducetasks -- 4个分区

	要求将手机号的总流量结果  按照不同的手机号的归属地  将最终结果输出到不同文件
		按照手机号分区  
		map：
			key：手机号
			value：其他  Text 
		shuffle：
			自定义分区
			按照手机号归属地  分区
			排序  分组
		reduce端：
			每一个分区的数据  排序  分组 
			直接输出
			
			
	注意：
	1）自定义分区中 分区和reducetask的一一对应
	分区编号  一定 要和reducetask的编号一一对应
	reducetask的编号  默认从0开始  顺序递增的
		job.setNumReduceTasks(3)
		reducetask0
		reducetask1
		reducetask2
	分区编号
	0  ---- reducetask0
	1  ---- reducetask1
	2  ---- reducetask2
	没有分区数据对应	reducetask3   空跑  浪费资源
	4  ---- reducetask4
	
	虽然自定义分区中  分区编号 是可以自己定义返回值的  不一定要顺序递增
	***但是出于性能考虑  分区编号最好是顺序递增的  reducetask设置和分区个数相同  否则必然有reducetask在执行空跑
	
	
分区：决定是每一个reducetask中数据分配问题
===========================================
分组  *********
----------------------------
map----shuffle(分区---》排序----》分组)----reduce
默认情况下 将map输出的key相同的为一组
当mapkey我们使用mapreduce中的默认类型的时候
	默认将mapkey 相同的分到一组
	======================hadoop
	@@@@@hadoop-1
	@@@@@hadoop-1
	@@@@@hadoop-1
	@@@@@hadoop-1
	@@@@@hadoop-1
	@@@@@hadoop-1
	@@@@@hadoop-1
	@@@@@hadoop-1
	@@@@@hadoop-1
	@@@@@hadoop-1
	@@@@@hadoop-1
	@@@@@hadoop-1
	@@@@@hadoop-1
	@@@@@hadoop-1
	@@@@@hadoop-1
	@@@@@hadoop-1
	@@@@@hadoop-1
	@@@@@hadoop-1
	@@@@@hadoop-1
	@@@@@hadoop-1
	@@@@@hadoop-1
	@@@@@hadoop-1
	@@@@@hadoop-1
	@@@@@hadoop-1
	
当mapkey 使用的是自定义类型的时候  WritableComparable
	分组：默认按照排序的字段进行分组的
	按照总流量   按照上行流量
	==========================key->13726230503	34386660	342078660	376465320
	@@@key--13726230503	34386660	342078660	376465320-(null)
	@@@key--13726238888	34386660	342078660	376465320-(null)
	
	==========================13719199419	3326400	0	3326400
	@@@13719199419	3326400	0	3326400-(null)
	@@@13926251106	3326400	0	3326400-(null)
	
	
为什么？
	分组默认调用的类 WritableComparator 
	分组在本质上  也是一个比较的过程
	默认的用于比较的方法  compare()
	分组的核心方法  分组按照map key (WritableComparable)
	public int compare(WritableComparable a, WritableComparable b) {
		//底层  调用 mapkey的 comparaTo
		/*排序   大小	compareTo  大于0  小于0	
		
		*分组  是否相等  compareTo  ==0
		/
		return a（mapkey1）.compareTo(b(mapkey2));
	}
	
	默认类型中：
		mapkey  comparaTo  按照整个字符串  整个值进行比较大小
		将整个串  或  值  完全相同的分到一组
		
	自定义类：
		map key comparaTo==0
		将map key的所有的比较属性都相同的 分到一组
		
		
默认情况下  分组字段  和  排序的字段完全一致

当排序  和  分组   规则不一致   不能使用默认分组了  必须自定义分组了

自定义分组：
	1）写一个类   继承  WritableComparator
	2）重写  compare 分组核心方法
	3）job指定
	
案例：
每一个科目中 平均分最高的前3个学生  topN 
排序   平均分   降序
分组   按照科目  自定义分组

map端：
	key：平均分  科目
		自定义类
		course avgscore  name
	value：null
shuffle：
	排序：
		自定义类
		comparaTo
			平均分   降序
	分组：
		1）写一个类   继承  WritableComparator
		2）重写  compare 分组核心方法
			course
		3）job指定
		
reduce：
	按照科目分组  按照平均分 【排序的  
	每一组前3个
	
	
按照上面的思路
	分组不对
	数据：
	排序：
	@@@@@computer	huangjiaju	83.2
	@@@@@computer	liutao	83.0
	@@@@@math	huangxiaoming	83.0
	@@@@@english	huanglei	83.0
	@@@@@math	huangjiaju	82.28571428571429
	@@@@@algorithm	huangjiaju	82.28571428571429
	@@@@@algorithm	liutao	82.0
	@@@@@computer	huanglei	74.42857142857143
	@@@@@english	liuyifei	74.42857142857143
	@@@@@algorithm	huanglei	74.42857142857143
	
	分组：
	组1：
	@@@@@computer	huangjiaju	83.2
	@@@@@computer	liutao	83.0
	组2：
	@@@@@math	huangxiaoming	83.0
	
	@@@@@english	huanglei	83.0
	@@@@@math	huangjiaju	82.28571428571429
	@@@@@algorithm	huangjiaju	82.28571428571429
	@@@@@algorithm	liutao	82.0
	@@@@@computer	huanglei	74.42857142857143
	@@@@@english	liuyifei	74.42857142857143
	@@@@@algorithm	huanglei	74.42857142857143
	
	分组发生在排序之后的   compare比较的时候  只会比较相邻的
	
	想要得到需要的分组  必须在排序阶段将需要分组的数据排到一起
	调整排序：
		将相同的科目数据放在一起
		在排序中先按照科目排序  在科目相同的时候  按照分数排序
		
	是否可以不需要自定义分组：
		algorithm	huangjiaju	82.28571428571429
		
		
		algorithm	liutao	82.0
		
		
		algorithm	huanglei	74.42857142857143
		
		computer	huangjiaju	83.2
		computer	liutao	83.0
		computer	huanglei	74.42857142857143
		english	huanglei	83.0
		english	liuyifei	74.42857142857143
		english	huangxiaoming	72.42857142857143
		math	huangxiaoming	83.0
		math	huangjiaju	82.28571428571429
		math	huanglei	74.42857142857143
		默认分组  按照mapkey 指定的属性  科目  平均分 
			将科目  和  平均分都相同的  分在一组
		
	
既有分组  又有排序的时候   分组和排序字段不一致
	排序中  一定要先按照分组字段进行排序  在按照排序的字段排序
	分组中 正常些分组
	
	排序   A
	分组  B  C 
	最终  排序： B  C   A 分组：B  C
	
	


各个区域排名前5商品
排序  销量倒叙
分组  按照地区分
	

		

	
	




reduce中的两个坑
----------------------------------------
参数 参数1：一组key   参数2：一组中的所有value的迭代器
1）reduce中的参数2 这个迭代器只能循环遍历一次
2）reduce 中的两个参数都存在  对象重用的问题
	每一组   key 一个对象   values底层只有一个对象
	key=hello  values=《1,1,1,1,1,1,1》
	reduce(key,values,context){
	}
	循环遍历的过程中 每一个values的值  都会对应一个与之对应的key的值   （同一个指针）
	每次循环遍历的时候  相当于对  对象重新赋值


yarn
产生背景：
--------------------------------------------
	hadoop1
		hdfs   分布式存储
		mapreduce   分布式计算  编程套路+计算流程
			运行计算任务的时候
			jobtracker   计算老大  主节点  单点故障
				1）既要负责整个集群的资源调度
				一个集群中 执行多个mr任务
				2）还要负责任务启动 以及进度跟踪
				启动mapreduce任务   跟踪maptask reducetask的进度
				如果集群中有多个  同时负责跟踪多个
				
				jobtracker压力很大
			tasktracker   计算的从节点
				提供资源   负责计算  
				将整个节点的资源  分为2部分  资源极大的浪费
					mapslot  --- maptask 
					reduceslot  --- reducetask 
					
			缺陷：
				1）单点故障
				2)扩展性差
				3）资源利用率低
				4）资源调度 只能为mr任务服务  计算框架的资源调度使用受限
					
	hadoop2
		将资源调度 和 进度跟踪分开
		hdfs
		mapreduce   编程套路  逻辑
		yarn 负责资源调度  mr   spark 
		任务跟踪  每一个job启动之后  负责自己的任务跟踪 
		
yarn的架构
---------------------------------------
resourcemanager : 主节点
	负责接收客户端的请求  提供资源调度  负责整个集群的资源调度
	ASM:
		applicationsmanager   所有任务的管理
		每一个任务的启动销毁   每一个任务的进度跟踪  胜败
		管理每一个应用程序的mrappmaster 
		1）每一个应用 mrappmaster 启动  销毁
		2）跟踪mrappmaster运行状态   失败重启
	Scheduler：
		调度器   决定的任务的执行顺序
		FIFO:
			first in first out
			内部维护的是单一的队列   哪一个任务先提交  哪一个任务先进行资源分配   哪一个任务运行
			缺点：
				如果前面有一个大任务  后面的任务阻塞
		FAIR：
			公平调度器
			所有任务平分资源
			1task   100% 
			2task   50%
			3task   33%
			4task    25%
			缺点：
				没有根据任务大小进行资源分配
		
		CAPACITY：
			计算能力  容量
			可以根据任务   组真是需要  手动配置资源占比
			group1  测试  20%  资源栈  单一队列  FIFO 
			group2  生产  80%  资源栈   单一队列  fifo
			内部维护了多个队列  每一个队列  都是FIFO 
			
			
	

nodemanager：从节点
	负责真正的资源提供者  为计算任务提供资源
	需要的时候提供  运行完成  回收资源  动态
	提供资源的时候  maptask | reducetask 单位提供
	1maptask|1reducetask ---- 1container 资源
	container 抽象资源容器 逻辑资源容器   nodemanager提供资源的基本单位|最小单位   内部封装了一定的资源（cpu 内存 磁盘  网络  io ）
	一个container运行的是一个maptask |reducetask
	
补充概念：
	MRAppMaster:
		mapreduce application master 
		mapreduce中  每一个应用程序（job）运行的时候 先启动mrappmaster  负责管理整个应用程序
		1）帮助当前应用程序申请资源
		2）启动maptask reducetask 
		3）跟踪maptask任务  reducetask任务的运行状态和进度
		4）进行maptask reducetask 资源回收
	

资源调度过程
----------------------------------------
1)客户端提交hadoop jar...  客户端先去请求rm 申请资源
2）rm会返回一个资源节点   用于启动当前应用程序的mrappmaster 
3）rm会到对应的节点上 启动mrappmaster 
4）mrappmaster 向resourcemanager进行申请资源  maptask|reducetask 
5)resourcemanager进行向mrappmaster 返回对应的资源节点
6）mrappmaster 就会到对应的资源节点上启动(container)maptask任务
7)maptask任务就会向 mrappmaster 进行汇报自己的运行状态和进度
8）当mrappmaster 获取到 有一个maptask执行完成 就开始启动reducetask(container)
9)reducetask 启动之后 也会向 mrappmaster进行汇报自己的状态和进度
10）每一个maptask 或 reducetask 运行完成  mrappmaster就会到对应的节点上进行资源回收
11）整个任务运行完成   mrappmaster 会向rm汇报并注销自己  并把整个运行结果返回给客户端

job提交
----------------------------------
见图