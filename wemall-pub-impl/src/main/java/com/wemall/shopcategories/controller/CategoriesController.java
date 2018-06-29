package com.wemall.shopcategories.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wemall.shopcategories.entity.Categories;
import com.wemall.shopcategories.model.CategoryModel;
import com.wemall.shopcategories.service.CategoriesService;

@Controller
@RequestMapping("/categories")
public class CategoriesController {

	@Autowired
	private CategoriesService categoriesService;
	
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

}
