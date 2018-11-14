package com.wemall.shopcategories.controller;

import java.util.List;

import com.github.pagehelper.PageHelper;
import com.wemall.shopcategories.dao.CategoriesDao;
import com.wemall.shopcategories.entity.Categories;

public class GetList implements Runnable {

	private List<Categories> list;
	private int pageSize;
	private int pageNow;
	private CategoriesDao categoriesDao;

	public GetList(List<Categories> list, int pageSize, int pageNow, CategoriesDao categoriesDao) {
		this.list = list;
		this.pageNow = pageNow;
		this.pageSize = pageSize;
		this.categoriesDao = categoriesDao;
	}

	public void run() {
		// TODO Auto-generated method stub
		PageHelper.startPage(pageNow, pageSize);
		List<Categories> categoriesList = categoriesDao.selectAllCategories();
		list.addAll(categoriesList);
	}

}
