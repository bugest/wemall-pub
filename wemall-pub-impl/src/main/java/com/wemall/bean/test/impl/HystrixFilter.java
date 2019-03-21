package com.wemall.bean.test.impl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;

import rx.Observable;

 
@Activate(group = "consumer")
public class HystrixFilter implements Filter {

    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
 
        DubboHystrixCommand command = new DubboHystrixCommand(invoker, invocation, "aaa");
        //Future<Result> queue = command.queue();
        /*Observable<Result> observe = command.observe();
        System.out.println("12");
        try {
			return observe.;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return null;*/
        Result res = command.execute();
        return res;
    }
}