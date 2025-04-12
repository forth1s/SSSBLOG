package com.example.controller;

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
    final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping(value = "/all")
    public List<Category> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @DeleteMapping(value = "/{ids}")
    public Result<?> deleteById(@PathVariable String ids) {
        boolean result = categoryService.deleteCategoryByIds(ids);
        if (result) {
            return new Result<>(204,"success", "删除成功!");
        }
        return new Result<>(500,"error", "删除失败!");
    }

    @PostMapping(value = "/")
    public Result<?> addNewCate(Category category) {

        if ("".equals(category.getCateName()) || category.getCateName() == null) {
            return new Result<>(400,"error", "请输入栏目名称!");
        }

        int result = categoryService.addCategory(category);

        if (result == 1) {
            return new Result<>(201,"success", "添加成功!");
        }
        return new Result<>(500,"error", "添加失败!");
    }

    @PutMapping(value = "/")
    public Result<?> updateCate(Category category) {
        int i = categoryService.updateCategoryById(category);
        if (i == 1) {
            return new Result<>(200,"success", "修改成功!");
        }
        return new Result<>(500,"error", "修改失败!");
    }
}
