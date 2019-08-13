hive的特点
================================
1）Hive 由 Facebook 实现并开源
2）是基于 Hadoop 的一个数据仓库工具
    基于hadoop： 底层数据hdfs 计算转换为mapreduce 
    数据仓库：data warehouse 
    数据仓库和数据库：
        概念：
        数据仓库针对于海量数据的  存储
        数据库 小批量的数据的
        
        应用场景:
        olap On-Line Analysis Processing  在线分析处理   数据仓库 查询  不擅长insert delete update   对于hive来说不支持delete update 支持insert 不建议使用 效率低  hive的数据加载load 
        oltp  On-Line Transaction Processing在线事务处理  数据库 增删改  insert delete update 
        
        事务支持：
        数据仓库  hive不支持事务
        数据库   支持事务  
3）可以将结构化的数据映射为一张数据库表
    字段---列
    95002,刘晨,女,19,IS   --- 一行
    95017,王风娟,女,18,IS
    hive只能做结构化数据  海量结构化
    hive是否可以完全替代mr？
        mr不仅仅可以针对结构化数据  半结构化数据xml 
4）并提供 HQL(Hive SQL)查询功能，
5）底层数据是存储在 HDFS 上。
6）Hive的本质是将 SQL 语句转换为 MapReduce 任务运行
    sql中的关键字  转换
    join 
    group by
    order by 
7)使不熟悉 MapReduce 的用户很方便地利用 HQL 处理和计算 HDFS 上的结构化的数据，适用于离线的批量数据计算。

hive优缺点：
========================
优点：
    延展性好   hive中提供的内置函数271个，hive自定义函数  java 
缺点：
    1)不支持 delete update 
    数据存储hdfs 
    2）不支持事务
hive基本架构
=======================================
4层架构
1）用户接口层
    cli   命令行
    jdbc  
    web ui 可视化界面操作  不用  
2）跨语言服务平台  thift server|hiveserver2
    保证其他java python 操作hive 
3）核心驱动
将hql语句转化为mr任务 并进行提交
    解释器：hql----抽象语法树
    编译器：抽象语法树--- 逻辑执行计划  mr 
    优化器：优化逻辑执行计划
    执行器：执行最终优化结果
4）元数据层
hive数据存储
    表数据|原始数据
        /user/hive/warehouse
        表中真正存储的数据  hdfs
        键表   插入数据
        00000_0   1       zs
        00000_1   2       zs
        将表中的数据 存为文件的形式
    元数据:
        表结构
        描述表数据的数据  MYSQL --> bd1904
        1）描述数据库相关信息的数据  DBS
        数据库数据存储的hdfs的位置         数据库名
        hdfs://bd1904/user/hive/warehouse   default 
        hive中创建一个数据库 这个表中插入1条数据  描述 
        2）描述表的信息   TBLS
        表名  dbid    表类型
        stu     7       MANAGED_TABLE  管理表
        hive中有一个表  这个表就会有一条信息
        3）表的字段信息  columns_v2
        表id    字段名  字段类型    字段顺序
        1       id      int         0
        hive中显示的表结构来自元数据的

hive的数据组织形式
================================
库：
    database 
    便于数据精细化管理  将不同模块的数据  存储在不同的数据库中
    order   shopping   log 
表:table 
    按照权限分：表数据管理权限
    内部表|管理表 managed_table
        默认创建的表  都是内部表
        表数据hdfs的管理权限  hive自己所有的
        存储表数据的hdfs目录 hive具备绝对的权限的（数据的删除）
        **内部表在进行删除的时候元数据和表数据一并被删除的
    外部表  external_table
        创建 external 关键字
        create external table stu(id int,name string);
        表数据的管理权限hdfs的  hive不具备表数据权限的（数据删除）
        表数据hdfs目录的管理权限  hdfs自己  不是hive 
        hive对这个数据  只有使用权限  没有删除权限
        **外部表在进行删除的时候元数据会被删除  但是表数据不会被删除
        外部表要想彻底删除  手动删除hdfs的数据
    内部表和外部表区别：
        1）建表语句  external 
        2）删除时候  本质
            	内部表  元数据  和 原始数据|表数据  一并删除
            	外部表 只删除元数据 
        3）应用场景：
            	外部表  公共数据 好多部门同用的数据  清洗原始日志数据   
          		  内部表  自己部门的数据    
    按照功能分：
        分区表：这里的分区完全不同于mr中分区
            hive中每一个表中存储的数据海量的数据
            我们在进行查询时候 select * from stu where age=19;执行的全表扫描   数据量大  全表扫描  严重影响查询效率
            为了提高查询效率  将原来的表进行划分成不同的区域  查询的时候降低扫描范围
            这里的每一个区域  就叫做一个分区
            如何进行划分区域：按照过滤的字段
            select * from stu where age=19;
            按照年龄进行划分   
            age=19
            age=18
            age=20 
            分区本质上相当于将原来的表划分成一个个的小表  分区依据按照过滤的字段  分区字段
            
            分区表的表现形式：
            一个分区表对应一个目录
            /user/hive/warehouse/test01.db/stu/00000  没有分区
            有分区：
            /user/hive/warehouse/test01.db/stu/age=19/0000
            /user/hive/warehouse/test01.db/stu/age=18/0000
            /user/hive/warehouse/test01.db/stu/age=20/0000
        分桶表:
            类似于mr中的分区
            作用
            1）提升抽样性能
                取某一个或几个桶中的数据  
            2）提升join性能
                select * from a join b on a.id=b.id;
                a  1T   b  1T 
                a   id%5     b==id%5 
                a  桶1    b 桶1
                两个表桶个数  相同  倍数
            
            将原始数据 按照一定的规则  分成不同的文件
            /user/hive/warehouse/test01.db/stu/00000
            分桶：不同分桶  不同的文件的
            age % 3
            /user/hive/warehouse/test01.db/stu/00000   0
            /user/hive/warehouse/test01.db/stu/00001   1
            /user/hive/warehouse/test01.db/stu/00002   2
            每一个分桶如何切分的：
            分桶字段 .hash & Integer_max % 分桶个数
视图：
    类型 VIRTUAL_VIEW
    类似于mysql中
    hive中只存在逻辑视图 不存在物化视图
    hive中的视图  不会真正的执行 仅仅将视图代表的sql语句存储
    create view view_name as select...
    视图类似于sql查询语句的快捷方式
    视图的作用 提sql高代码的可读性
    select 
        select 
            select 
                select 
                    .....

数据存储 
    原始数据  表数据 
    元数据


hive的操作
========================================
DDL  data define language
------------------------------------
数据库的操作
----------------------------
1）建库
create database if not exists dbname;

2)切换库
use dbname;

3)查看正在使用的库
select current_database();

4)查看库列表
show databases;
show databases like "test*";

5)查看库的详细描述信息
desc database dbname;  描述
test             hdfs://bd1904/user/hive/warehouse/test.db       hadoop  USER   元数据


desc database extended dbname;  详细描述  了解
desc database extended test;

6）删除数据库
drop database if exists dbname;
drop database if exists test01;
    不能删除非空数据库的  只能删除空的
drop database if exists dbname cascade;   级联删除
drop database if exists test01 cascade;
drop database if exists test01 restrict;    默认的删除

7）修改数据库
    不支持

 
表操作
------------------------- 
1）建表
CREATE [EXTERNAL] TABLE [IF NOT EXISTS] table_name
 [(字段名 字段类型 [COMMENT 字段描述], ...)]
 [COMMENT 表描述]
 [PARTITIONED BY (字段名 字段类型 [COMMENT 字段描述], ...)]
 [CLUSTERED BY (col_name, col_name, ...)
 [SORTED BY (col_name [ASC|DESC], ...)] INTO num_buckets BUCKETS]
 [ROW FORMAT row_format]
 [STORED AS file_format]
 [LOCATION hdfs_path]
 
    说明：
    1)EXTERNAL  外部表关键字  不加 默认内部表
    2）IF NOT EXISTS  建表防止报错
    3）字段类型 
    int  (tinyint smallint  bigint)
    String 
    4）COMMENT 描述信息的
    5）PARTITIONED BY  指定分区  分区表标识
    注意： 分区字段一定不能再建表字段中  分区字段是一个全新的字段
    6）CLUSTERED BY  指定分桶的
    分桶字段   分桶个数 
    每一个桶  分桶字段.hash % 分桶个数
    INTO 桶个数 BUCKETS
    
    SORTED BY (col_name [ASC|DESC], ...)  指定每一个桶的是否排序的
    注意：分桶字段  一定是建表字段中的
    7)ROW FORMAT  指定格式化  通常情况下用于指定列之间的分隔符
    hive数据加载  
    load  将一个本地数据或hdfs数据（文件） 加载到hive的表中的
    hive 列----文件字段
    
    ROW FORMAT  指定的分隔符
    fields terminated by ","  **
    lines terminated by "\n"  
    collection keys terminated by ""
    
    8)STORED AS  指定hive的表数据  在hdfs的存储格式的
        textfile   默认  文本
        SEQUENCEFILE | 
        RCFILE
    9）LOCATION  指定hive的表数据在hdfs的存储路径
    这里指定了  覆盖默认路径  /user/hive/warehouse
    
案例：
    student.txt    95002,刘晨,女,19,IS
    1）创建一个内部表
    create table if not exists stu_managed(id int,name string,sex string,age int,dept string)row format delimited fields terminated by "," stored as textfile;


    2）创建一个外部表
    create external table if not exists stu_external(id int,name 
    string,sex string,age int,dept string)row format delimited fields 
    terminated by "," stored as textfile;


    3）创建一个分区表
    分区字段   查询业务   经常过滤的字段  生产上 日期
    分区字段   dept 
    create table if not exists stu_ptn(id int,name string,sex string,age int) 
    partitioned by (dept string) row format delimited fields terminated by ",";


    4）创建一个分桶表
    分桶字段  age  关联键     分桶个数   3

    create table if not exists stu_buk(id int,name string,sex string,age int,dept string) 
    clustered by (age) sorted by (age desc) into 3 buckets 
    row format delimited fields terminated by ",";

    5）表复制
    like 
    create table tbname like stu_managed;
    create external table stu01 like stu_managed;
           表复制   只复制表结构(字段)  不会复制表属性


    6）ctas语句建表
    create table tbname as select.....
2)查看表列表
show tables;
show tables in dbname;  查看某一个数据库下的
show tables like "*t*";


3)查看表的详细描述信息
desc tbname;  查看字段信息
desc extended tbname; 查看表的详细信息  了解
desc formatted tbname;查看表的详细信息  格式化显示


4)修改表
修改表的列信息
    1）修改表列名  类型
    alter table tbname change col col1 type;
    alter table stu01 change id sid int;
    alter table stu01 change sid sid string;  int--> string 
    alter table stu01 change sid sid int;
    修改类型   小---》 大
    2）添加列
    alter table tbname add columns (col type);
    alter table stu01 add columns(address string);
    3)替换列*** 
    将整个表的所有列  替换为指定的列
    alter table tbname replace columns(col type);
    alter table stu01 replace columns(idd int,names string);
    4)删除列  
    不支持
修改表的分区信息
    1）添加分区
    ......./stu_ptn/dept="is"
    相当于给分区字段指定值
    alter table tbname add partition(分区字段=分区值);
    alter table stu_ptn add partition(dept="IS");
    一次性添加多个分区
    alter table tbname add partition(分区字段=分区值) partition(分区字段=值);
    alter table stu_ptn add partition(dept="CS") partition(dept="MA");
    
    2)修改分区
    修改分区的存储路径
    添加分区的时候直接指定路径
    alter table tbname add partition(分区字段=分区值) location "hdfs path ";
    alter table stu_ptn add partition(dept="test") location "/data/hive/test";
    
    修改已经存在的分区的存储路径
    alter table tbname partition(分区字段=分区制) set location "hdfs path" 
    alter table stu_ptn partition(dept="test") set location "/user/hive/warehouse/bd1904.db/stu_ptn";  添加数据 创建对应目录
    
    3）查询分区
    show partitions tbname;
    show partitions stu_ptn;
    分区字段只指定一个  叫一级分区
    select * from stu where age=18 and address="bj";
    分区字段多个的时候   多级分区   多个层级目录结构
    show partitions stu_ptn partition (高级分区);  查看某个高级分区下的所有子分区
    4）删除分区
    表权限  
    alter table tbname drop partition(分区字段=分区制)；
    alter table stu_ptn drop partition(dept="IS");
5)查看表的详细建表语句
show create table tbname;
show create table stu_buk;

CREATE TABLE `stu_buk`(
  `id` int, 
  `name` string, 
  `sex` string, 
  `age` int, 
  `dept` string)
CLUSTERED BY ( 
  age) 
SORTED BY ( 
  age DESC) 
INTO 3 BUCKETS
ROW FORMAT SERDE 
  'org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe' 
WITH SERDEPROPERTIES ( 
  'field.delim'=',', 
  'serialization.format'=',') 
STORED AS INPUTFORMAT 
  'org.apache.hadoop.mapred.TextInputFormat' 
OUTPUTFORMAT 
  'org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat'
LOCATION
  'hdfs://bd1904/user/hive/warehouse/bd1904.db/stu_buk'
TBLPROPERTIES (
  'transient_lastDdlTime'='1565190095')
  
6)清空表
truncate table tbname;
清空表数据  
这个操作 只能针对内部表
内部表  清空表  将表对应的hdfs的目录下的文件删除了

7）删除表
drop table if exists tbname;
    


DML  data manage language 
=======================================
1)向表中添加数据 
    load 
    将一个已经存在的文件（本地|hdfs）  加载到hive表中  按照hive表中指定的分割方式进行解析这个数据 
    语法：
    load data [local] inpath "path" into table tbname;
    说明：
        local 代表数据来源   数据来自于本地  不加数据来自hdfs 
    案例：
        1）将数据从本地加载到hive的表中
        load data local inpath "/home/hadoop/tmp_data" into table stu_managed;
        推断：数据加载过程  将数据从指定的路径下  挪（复制）到了hive表所在的路径下
        测试： 手动将数据上传hive表hdfs的对应的路径下
        2）将数据从hdfs 加载到hive表中
        load data inpath "/mydata" into table stu_managed;
        将hdfs的指定路径数据  移动到hive表所在的hdfs的路径的
        
        load的操作本质 将数据 挪到 hive表所在的目录下
        只要数据在hive表所在的目录下  hive表  自动解析  ****
        
    insert 
        1）单条数据插入  一次插入一条数据
        insert into table tbname values();
        insert into table stu_managed values(1,"zs","f",18,"CS");
        最终还是将数据 存储文件的形式  数据的字段分隔符  建表指定的分隔符
        效率低
        2）单重数据插入
        一次性  插入一个sql的查询结果
        将一个sql的查询结果（多条）  插入到表中
        insert into table tbname select ....
        insert into table stu_external select * from stu_managed where age=18;
        
        
        
        3）多重数据插入
        一次扫描表  但是最终将多个查询结果  插入到多张表中  或者一个表的多个分区中
        from tbname 
        insert into table tb1 select ... 
        insert into table tb2 select .....
        
        插入多个表中
        stu_managed   age=18   tb1   age>19 tb2
        from stu_managed 
        insert into table tb1 select * where age=18 
        insert into table tb2 select * where age>19;
        
        插入多个分区中
        分区表  数据分成2块存储的
            1）分区字段   /user/hive/warehouse/bd1904.db/stu_ptn/dept=CS
            2）普通字段
            表数据  文件 
            注意：分区表数据操作  一定要指定分区名  partition（分区名）
            from stu_managed  
            insert into stu_ptn partition(dept="IS") select id,name,sex,age where dept="IS" 
            insert into stu_ptn partition(dept="MA") SELECT id,name,sex,age where dept="MA";
            
            
验证：分区表数据插入方式
    分桶表数据插入方式
            
        

    

    
    
本地模式：
set hive.exec.mode.local.auto=true;