package com.example.service;

import com.example.common.exceptions.BusinessException;
import com.example.entity.Category;
import com.example.mapper.CategoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

@Service
@Transactional
public class CategoryService {

    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    public List<Category> getAllCategories() {
        try {
            return categoryMapper.getAllCategories();
        } catch (Exception e) {
            throw new BusinessException(500, "获取所有分类时出现异常");
        }
    }

    public void deleteCategoryByIds(String ids) {
        String[] split = ids.split(",");
        int result = categoryMapper.deleteCategoryByIds(split);
        if (result != split.length) {
            throw new BusinessException(500, "删除分类时部分删除失败");
        }
    }

    public void updateCategoryById(Category category) {
        int result = categoryMapper.updateCategoryById(category);
        if (result != 1) {
            throw new BusinessException(500, "更新分类信息失败");
        }
    }

    public void addCategory(Category category) {
        category.setDate(new Timestamp(System.currentTimeMillis()));
        int result = categoryMapper.addCategory(category);
        if (result != 1) {
            throw new BusinessException(500, "添加分类失败");
        }
    }
}
