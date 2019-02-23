package com.wemall.shopcategories.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.rpc.RpcContext;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wemall.shopcategories.controller.GetList;
import com.wemall.shopcategories.dao.CategoriesDao;
import com.wemall.shopcategories.entity.Categories;
import com.wemall.shopcategories.model.CategoryModel;
import com.wemall.shopcategories.service.CategoriesService;

@Service
@CacheConfig(cacheNames = "categories")
public class CategoriesServiceImpl implements CategoriesService {
	
	public CategoriesServiceImpl() {
		System.out.println("122222222222");
	}
	
	@Autowired
	private CategoriesDao categoriesDao;

	// private RedisTemplateUtil redisTemplateUtil;

	// 可以自定义构造函授
	/*
	 * @Autowired public CategoriesServiceImpl(CategoriesDao categoriesDao,
	 * RedisTemplateUtil redisTemplateUtil) { this.categoriesDao = categoriesDao;
	 * //this.redisTemplateUtil = redisTemplateUtil; List<Categories> allCategories
	 * = categoriesDao.selectAllCategories(); // 把目录写入缓存
	 * //redisTemplateUtil.set("allCategories", allCategories); }
	 */
	//@Cacheable(value ="cache1",key="allCategories")
	public List<Categories> selectAllCategories() {
		return categoriesDao.selectAllCategories();
	}

	//@Cacheable(value ="cache1",key = "'CategoryModelList'")
	public List<CategoryModel> getCategoryModelList() {
		Object object = RpcContext.getContext().get("token");
		int a = 1;
		int b = 0;
		int i = a/b;
		List<Categories> categoriesList = categoriesDao.selectAllCategories();
		// 首先增加全部商家
		CategoryModel categoryModel = new CategoryModel();
		categoryModel.setName("全部商家");
		categoryModel.setLevel(1);
		List<Integer> ids = new ArrayList<Integer>();
		List<CategoryModel> categoryModelList = new ArrayList<CategoryModel>();
		for (Categories categories : categoriesList) {
			if (categories.getLevel() != null && categories.getLevel() == 1) {
				ids.add(categories.getId());
				// 同时创建了子
				CategoryModel cm = new CategoryModel();
				cm.setName(categories.getName());
				cm.setId(categories.getId());
				cm.setLevel(1);
				List<CategoryModel> categoryModelSubList = new ArrayList<CategoryModel>();
				// 每个节点增加全部
				CategoryModel suball = new CategoryModel();
				suball.setId(cm.getId());
				suball.setName("全部");
				suball.setLevel(1);
				categoryModelSubList.add(suball);
				List<Integer> idss = new ArrayList<Integer>();
				idss.add(cm.getId());
				for (Categories cgl : categoriesList) {
					if (cgl.getParentId() != null && cgl.getParentId().equals(cm.getId())) {
						CategoryModel cmsub = new CategoryModel();
						cmsub.setId(cgl.getId());
						cmsub.setName(cgl.getName());
						cmsub.setLevel(1);
						idss.add(cgl.getId());
						categoryModelSubList.add(cmsub);
					}
				}
				cm.setSubCategories(categoryModelSubList);
				cm.setIds(idss);
				categoryModelList.add(cm);
			}
		}
		categoryModel.setIds(ids);
		// 将全部商家的id加入
		categoryModelList.add(0, categoryModel);
		return categoryModelList;
		//return null;
	}
	@Transactional
	public Categories selectByPrimaryKey(Integer id) {
		// TODO Auto-generated method stub
		Categories categories = categoriesDao.selectByPrimaryKey(id);
		//return categories;
		return null;
	}

	@CacheEvict(key = "'CategoryModelList'")
	public void removeCategoryModelList() {

	}

	/**
	 * @return
	 */
	/* (非 Javadoc) 
	* <p>Title: updateCategoryModelList</p> 
	* <p>Description: </p> 
	* @return 
	* @see com.wemall.shopcategories.service.CategoriesService#updateCategoryModelList() 
	*/
	@CachePut(key = "'CategoryModelList'")
	public List<CategoryModel> updateCategoryModelList() {
		List<Categories> categoriesList = categoriesDao.selectAllCategories();
		// 首先增加全部商家
		CategoryModel categoryModel = new CategoryModel();
		categoryModel.setName("全部商家");
		categoryModel.setLevel(1);
		List<Integer> ids = new ArrayList<Integer>();
		List<CategoryModel> categoryModelList = new ArrayList<CategoryModel>();
		for (Categories categories : categoriesList) {
			if (categories.getLevel() != null && categories.getLevel() == 1) {
				ids.add(categories.getId());
				// 同时创建了子
				CategoryModel cm = new CategoryModel();
				cm.setName(categories.getName());
				cm.setId(categories.getId());
				cm.setLevel(1);
				List<CategoryModel> categoryModelSubList = new ArrayList<CategoryModel>();
				// 每个节点增加全部
				CategoryModel suball = new CategoryModel();
				suball.setId(cm.getId());
				suball.setName("全部");
				suball.setLevel(1);
				categoryModelSubList.add(suball);
				List<Integer> idss = new ArrayList<Integer>();
				idss.add(cm.getId());
				for (Categories cgl : categoriesList) {
					if (cgl.getParentId() != null && cgl.getParentId().equals(cm.getId())) {
						CategoryModel cmsub = new CategoryModel();
						cmsub.setId(cgl.getId());
						cmsub.setName(cgl.getName());
						cmsub.setLevel(1);
						idss.add(cgl.getId());
						categoryModelSubList.add(cmsub);
					}
				}
				cm.setSubCategories(categoryModelSubList);
				cm.setIds(idss);
				categoryModelList.add(cm);
			}
		}
		categoryModel.setIds(ids);
		// 将全部商家的id加入
		categoryModelList.add(0, categoryModel);
		return categoryModelList;
	}
	
	
	public List<Categories> selectAllCates(int pageNow, int pageSize) {
		/*PageHelper.startPage(pageNow, pageSize);
		List<Categories> categoriesList = categoriesDao.selectAllCategories();
		PageInfo<Categories> pageInfo = new PageInfo<Categories>(categoriesList);
		return pageInfo;*/
		List<Categories> list = new ArrayList<Categories>();
		
		ExecutorService a = Executors.newFixedThreadPool(3);
		Future f1 = a.submit(new GetList(list, 10, 1, categoriesDao));
		Future f2 = a.submit(new GetList(list, 10, 2, categoriesDao));
		Future f3 = a.submit(new GetList(list, 10, 3, categoriesDao));
		while (!f1.isDone() || !f2.isDone() || !f3.isDone()) {
			
		}
		return list;
	}
	
	public PageInfo<Categories> selectCategoriesByCondition(Integer id, String name,int pageNow, int pageSize) {
		PageHelper.startPage(pageNow, pageSize);
		Categories categories = new Categories();
		categories.setName(name);
		categories.setId(id);
		List<Categories> categoriesList = categoriesDao.selectCategoriesByCondition(categories);
		PageInfo<Categories> pageInfo = new PageInfo<Categories>(categoriesList);
		return pageInfo;
	}

	public int insert(Categories categories) {
		// TODO Auto-generated method stub
		return categoriesDao.insert(categories);
	}

}
