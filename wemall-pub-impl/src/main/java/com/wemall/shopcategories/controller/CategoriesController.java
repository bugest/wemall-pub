package com.wemall.shopcategories.controller;

import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.disconf.demo.config.JedisConfigTest;
import com.github.pagehelper.PageInfo;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.wemall.shopcategories.entity.Categories;
import com.wemall.shopcategories.model.CategoryModel;
import com.wemall.shopcategories.service.CategoriesService;

import rx.Observable;
import rx.Observer;


@Controller
@RequestMapping("/categories")
public class CategoriesController {

	public CategoriesController() {
		System.out.println("okle");
	}
	@Autowired
	private CategoriesService categoriesService;
	
	@Autowired
	private JedisConfigTest jedisConfigTest;
	
	@ResponseBody
	@RequestMapping("/selectAllCategories")
	public List<CategoryModel> selectAllCategories() {
		List<CategoryModel> CategoryModelList = categoriesService.getCategoryModelList();
		return CategoryModelList;
	}
	
	@RequestMapping("/test")
	public String getTest() {
		return "redirect:/testb"; 
	}
	@ResponseBody
	@RequestMapping("/testb")
	public String getTest1() {
		return "hahah"; 
	}
	
	@ResponseBody
	@RequestMapping(value="/selectByPrimaryKey/{id}") 
	public Categories selectByPrimaryKey(@PathVariable Integer id) {
		return categoriesService.selectByPrimaryKey(id);
	}
	
	@ResponseBody
	@RequestMapping("/updateAllCategories")
	public List<CategoryModel> updateAllCategories() {
		List<CategoryModel> CategoryModelList = categoriesService.updateCategoryModelList();
		return CategoryModelList;
	}
	
	@ResponseBody
	@RequestMapping("/removeAllCategories")
	public void removeAllCategories() {
		categoriesService.removeCategoryModelList();
	}

	/** 
	* @Title: selectAllCates 
	* @Description: TODO(这里用一句话描述这个方法的作用) 
	* @param @return    设定文件 
	* @return List<Categories>    返回类型 
	* @throws 
	*/
	@ResponseBody
	@RequestMapping("selectAllCates") 
	public List<Categories> selectAllCates(int pageNow, int pageSize) {
		jedisConfigTest.getHost();
		return categoriesService.selectAllCates(pageNow, pageSize);
	}
	
	@ResponseBody
	@RequestMapping("selectCategoriesByCondition") 
	public PageInfo<Categories> selectCategoriesByCondition(Integer id, String name, int pageNow, int pageSize) {
		return categoriesService.selectCategoriesByCondition(id, name, pageNow, pageSize);
	}
	
	@ResponseBody
	@RequestMapping("insert") 
	public int insert(Categories categories) {
		return categoriesService.insert(categories);
	}
	@PostConstruct
	public void test() {
		System.out.println("11111111111111111");
	}
	
	@ResponseBody
	@RequestMapping("testcommand") 
	public String insert1() throws InterruptedException, ExecutionException {
		MyCommand myCommand = new MyCommand(HystrixCommandGroupKey.Factory.asKey("111"));
		Observable<String> observe = myCommand.observe();
		observe.subscribe(new Observer<String>() {

			public void onCompleted() {
				// TODO Auto-generated method stub
				System.out.println("compeleted");
			}

			public void onError(Throwable arg0) {
				// TODO Auto-generated method stub
				System.out.println("error");
			}

			public void onNext(String arg0) {
				// TODO Auto-generated method stub
				System.out.println(arg0);
			}
	
		});
		return "123";
	}
}
