package com.wemall.bean.test.impl;

import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.filter.tps.DefaultTPSLimiter;
import com.alibaba.dubbo.rpc.filter.tps.TPSLimiter;

public class TpsLimitFilter implements Filter {

    private final TPSLimiter tpsLimiter = new DefaultTPSLimiter();

	public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        if (!tpsLimiter.isAllowable(invoker.getUrl(), invocation)) {
            throw new RpcException(
                    new StringBuilder(64)
                            .append("Failed to invoke service ")
                            .append(invoker.getInterface().getName())
                            .append(".")
                            .append(invocation.getMethodName())
                            .append(" because exceed max service tps.")
                            .toString());
        }
        return invoker.invoke(invocation);
	}


}