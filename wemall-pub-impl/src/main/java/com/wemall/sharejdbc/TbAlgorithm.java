package com.wemall.sharejdbc;
import com.dangdang.ddframe.rdb.sharding.api.ShardingValue;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.SingleKeyTableShardingAlgorithm;
 
import java.util.Collection;
 
public class TbAlgorithm implements SingleKeyTableShardingAlgorithm<Integer> {
 
 
    
    public String doEqualSharding(Collection<String> availableTargetNames, ShardingValue<Integer> shardingValue) {
        int id = shardingValue.getValue();
 
        //中间变量 
        //中间变量=id%(库数*每个库的表述)
        int temp = id % (2 * 2);
        int index = id % 2;
 
        for (String each : availableTargetNames) {
            if (each.endsWith(index + "")) {
                return each;
            }
        }
        throw new UnsupportedOperationException();
    }
 
    
    public Collection<String> doInSharding(Collection<String> availableTargetNames, ShardingValue<Integer> shardingValue) {
        return null;
    }
 
    
    public Collection<String> doBetweenSharding(Collection<String> availableTargetNames, ShardingValue<Integer> shardingValue) {
        return null;
    }
}
