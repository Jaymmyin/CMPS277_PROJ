package bftsmart.demo.currency_control;
import bftsmart.tom.ServiceProxy;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class OccLayerDef {
    public ServiceProxy KVProxy;
    public ReplicaDef replica;
    public  Map<Integer, TransDBWrapper> wrapper_trans_map = new HashMap<>(); // key: tnc
    public  Map<Integer, OccExecutor> executor_trans_map = new HashMap<>(); // key: id
    public  int committed_trans_num = 0;

    public OccLayerDef(int id, ReplicaDef replica){
        KVProxy = new ServiceProxy(id, "config");
        this.replica = replica;
    }

    public String create_or_update_executor_by_read_op(OperationDef op){
        if (!this.executor_trans_map.containsKey(op.trans_id)){ // already exists
            TransDBWrapper empty_wrapper = new TransDBWrapper(replica, op.trans_id);
            OccExecutor executor = new OccExecutor(empty_wrapper, this);
            executor_trans_map.put(op.trans_id, executor);
        }
        OccExecutor exist_executor = executor_trans_map.get(op.trans_id);
        return exist_executor.cache.read(op.key);
    }

    public void create_or_update_executor_by_write_op(OperationDef op){
        if (!this.executor_trans_map.containsKey(op.trans_id)){ // already exists
            TransDBWrapper empty_wrapper = new TransDBWrapper(replica, op.trans_id);
            OccExecutor executor = new OccExecutor(empty_wrapper, this);
            executor_trans_map.put(op.trans_id, executor);
        }
        OccExecutor exist_executor = executor_trans_map.get(op.trans_id);
        exist_executor.cache.wtite(op.key, op.val);
    }

    public boolean create_or_update_executor_by_commit_op(OperationDef op){
        if (!this.executor_trans_map.containsKey(op.trans_id)){ // already exists
            TransDBWrapper empty_wrapper = new TransDBWrapper(replica, op.trans_id);
            OccExecutor executor = new OccExecutor(empty_wrapper, this);
            executor_trans_map.put(op.trans_id, executor);
        }
        OccExecutor exist_executor = executor_trans_map.get(op.trans_id);
        try {
            return exist_executor.validate();
        }catch (IOException e){
            System.out.println(e);
            return false;
        }
    }

    public TransDBWrapper get_cache_by_tn(int trans_id){
        assert(wrapper_trans_map.containsKey(trans_id)); // todo: try
        return wrapper_trans_map.get(trans_id);
    }

    public boolean commit_after_reach_consensus(TransDBWrapper cache){
        assert(!wrapper_trans_map.containsKey(committed_trans_num)); // todo: try
        wrapper_trans_map.put(committed_trans_num, cache);
        return cache.commit();
    }
}
