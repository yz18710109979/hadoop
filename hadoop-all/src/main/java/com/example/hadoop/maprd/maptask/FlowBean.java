package com.example.hadoop.maprd.maptask;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

/*
 * 	1）实现  Writable接口
	2）重写  
		write    对象----二进制  序列化
		readFields   二进制---》对象   反序列化
 */
public class FlowBean implements Writable {
	private int upflow;
	private int downflow;
	private int sumflow;
	
	public int getUpflow() {
		return upflow;
	}

	public void setUpflow(int upflow) {
		this.upflow = upflow;
	}

	public int getDownflow() {
		return downflow;
	}

	public void setDownflow(int downflow) {
		this.downflow = downflow;
	}

	public int getSumflow() {
		return sumflow;
	}

	public void setSumflow(int sumflow) {
		this.sumflow = sumflow;
	}
	//1.无参构造必须要
	public FlowBean() {
	}

	public FlowBean(int upflow, int downflow) {
		this.upflow = upflow;
		this.downflow = downflow;
		this.sumflow = this.upflow + this.downflow;
	}
	
	//toString 必须要  写出hdfs的数据格式
	@Override
	public String toString() {
		return upflow + "\t" + downflow + "\t" + sumflow;
	}
	//序列化方法  属性
	//参数  
	public void write(DataOutput out) throws IOException {
		out.writeInt(upflow);
		out.writeInt(downflow);
		out.writeInt(sumflow);
	}
	//反序列化方法
	/*
	 *反序列顺序 一定  和序列化顺序一致 
	 */
	public void readFields(DataInput in) throws IOException {
		this.upflow = in.readInt();
		this.downflow = in.readInt();
		this.sumflow = in.readInt();
	}
}
