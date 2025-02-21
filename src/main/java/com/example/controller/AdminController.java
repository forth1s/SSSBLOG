package com.example.controller;

import com.example.entity.Article;
import com.example.entity.Result;
import com.example.service.ArticleService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 超级管理员专属Controller
 */
@RestController
@RequestMapping("/admin")
public class AdminController {
    final ArticleService articleService;

    public AdminController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping(value = "/article/all")
    public Map<String, Object> getArticleByStateByAdmin(@RequestParam(value = "page", defaultValue = "1") Integer page, @RequestParam(value = "count", defaultValue = "6") Integer count, String keywords) {
        List<Article> articles = articleService.getArticleByState(-2, page, count, keywords);
        Map<String, Object> map = new HashMap<>();
        map.put("articles", articles);
        map.put("totalCount", articleService.getArticleCountByState(1, null, keywords));
        return map;
    }

    @PutMapping(value = "/article/dustbin")
    public Result updateArticleState(Long[] aids, Integer state) {
        if (articleService.updateArticleState(aids, state) == aids.length) {
            return new Result("success", "删除成功!");
        }
        return new Result("error", "删除失败!");
    }
}