package com.example.controller;

import com.example.common.exceptions.BadRequestException;
import com.example.entity.Category;
import com.example.entity.Result;
import com.example.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 超级管理员专属Controller
 */
@RestController
@RequestMapping("/admin/category")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping(value = "/all")
    public List<Category> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @DeleteMapping(value = "/{ids}")
    public Result<?> deleteById(@PathVariable String ids) {
        categoryService.deleteCategoryByIds(ids);
        return Result.success("删除成功", null);
    }

    @PostMapping(value = "/")
    public Result<?> addNewCate(Category category) {
        if ("".equals(category.getCateName()) || category.getCateName() == null) {
            throw new BadRequestException(400,"请输入栏目名称");
        }
        categoryService.addCategory(category);
        return Result.success("添加成功", null);
    }

    @PutMapping(value = "/")
    public Result<?> updateCate(Category category) {
        categoryService.updateCategoryById(category);
        return Result.success("修改成功", null);
    }
}
