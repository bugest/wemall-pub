package com.wemall.sharejdbc;
import com.dangdang.ddframe.rdb.sharding.api.ShardingValue;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.SingleKeyDatabaseShardingAlgorithm;
 
import java.util.Collection;
 
public class DbAlgorithmNo implements SingleKeyDatabaseShardingAlgorithm<Integer> {
 
    
    public String doEqualSharding(Collection<String> collection, ShardingValue<Integer> shardingValue) {
        int id = shardingValue.getValue();
        
        //中间变量 
        //中间变量=id%(库数*每个库的表述)
        int temp = id%(2*2);
        
        int index = temp/2;
 
        for (String each : collection) {
            if (each.endsWith(index + "")) {
                return each;
            }
        }
        throw new UnsupportedOperationException();
    }
 
    
    public Collection<String> doInSharding(Collection<String> collection, ShardingValue<Integer> shardingValue) {
        return null;
    }
 
    
    public Collection<String> doBetweenSharding(Collection<String> collection, ShardingValue<Integer> shardingValue) {
        return null;
    }
}
