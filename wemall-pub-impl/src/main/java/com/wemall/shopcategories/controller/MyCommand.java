package com.wemall.shopcategories.controller;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

public class MyCommand extends HystrixCommand<String> {

	protected MyCommand(HystrixCommandGroupKey group) {
		super(group);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String run() throws Exception {
		// TODO Auto-generated method stub
		return "123";
	}

}
