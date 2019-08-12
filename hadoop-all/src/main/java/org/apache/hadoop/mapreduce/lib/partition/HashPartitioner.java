package org.apache.hadoop.mapreduce.lib.partition;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.mapreduce.Partitioner;

/** Partition keys by their {@link Object#hashCode()}. */
@InterfaceAudience.Public
@InterfaceStability.Stable
public class HashPartitioner<K, V> extends Partitioner<K, V> {

  /** Use {@link Object#hashCode()} to partition. */
  public int getPartition(K key, V value,
                          int numReduceTasks) {
	  System.out.println(key.hashCode());
	  String hashCode = Integer.toBinaryString(key.hashCode());
	  System.out.println("二进制的hashCode：" + hashCode);
	  System.out.println(Integer.MAX_VALUE);
	  int maxValue = Integer.MAX_VALUE;
	  String binaryString = Integer.toBinaryString(maxValue);
	  System.out.println("二进制的int最大值：" + binaryString);
	  return (key.hashCode() & Integer.MAX_VALUE) % numReduceTasks;
  }
  
  public static void main(String[] args) {
	  HashPartitioner<String, String> hashPartitioner = new HashPartitioner<String, String>();
	  int i = hashPartitioner.getPartition("123", "123", 11);
	  System.out.println(i);
  }

}
