package com.wemall.bean.test.impl;

import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;

 
@Activate(group = "consumer")
public class HystrixFilter implements Filter {

    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
 
        DubboHystrixCommand command = new DubboHystrixCommand(invoker, invocation, "aaa");
        Result res = command.execute();
        return res;
    }
}