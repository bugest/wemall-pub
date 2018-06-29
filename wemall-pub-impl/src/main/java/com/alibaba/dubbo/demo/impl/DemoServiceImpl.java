package com.alibaba.dubbo.demo.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.dubbo.demo.DemoService;


/** 
* 
*
* @author linan
* @date 2018年6月7日
*/
public class DemoServiceImpl implements DemoService ,Serializable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public List<String> getPermissions(Long id) {
        List<String> arrayList = new ArrayList<String>();
        arrayList.add("1222");
        arrayList.add("33222");
		return arrayList;
    }
}
