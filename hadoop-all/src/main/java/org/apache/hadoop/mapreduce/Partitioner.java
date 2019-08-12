package org.apache.hadoop.mapreduce;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;

/**
 * 
 * @author Administrator
 * 分区抽象类
 * @param <KEY>
 * @param <VALUE>
 */
@InterfaceAudience.Public
@InterfaceStability.Stable
public abstract class Partitioner<KEY, VALUE> {
  
  /** 
   * Get the partition number for a given key (hence record) given the total 
   * number of partitions i.e. number of reduce-tasks for the job.
   *   
   * <p>Typically a hash function on a all or a subset of the key.</p>
   *
   * @param key the key to be partioned.
   * @param value the entry value.
   * @param numPartitions the total number of partitions.
   * @return the partition number for the <code>key</code>.
   */
  public abstract int getPartition(KEY key, VALUE value, int numPartitions);
  
}
