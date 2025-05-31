package com.example.controller;

import com.example.common.exceptions.BusinessException;
import com.example.entity.Article;
import com.example.entity.Result;
import com.example.service.ArticleService;
import com.example.common.utils.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/article")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @PostMapping("/")
    public Result<?> addNewArticle(Article article) {
        articleService.addNewArticle(article);
        return Result.success("文章添加成功", article.getId());
    }

    @PostMapping("/uploadimg")
    public Result<?> uploadImg() {
        // 这里可以添加一些基本的参数验证
        // 实际逻辑需要调整
        return Result.success("图片上传成功", "图片地址");
    }

    @GetMapping("/all")
    public Map<String, Object> getArticleByState(@RequestParam(value = "state", defaultValue = "-1") Integer state,
                                                 @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                 @RequestParam(value = "count", defaultValue = "6") Integer count,
                                                 @RequestParam(value = "keywords", required = false) String keywords) {
        try {
            List<Article> articles = articleService.getArticleByState(state, page, count, keywords);
            int totalCount = articleService.getArticleCountByState(state, Util.getCurrentUser().getId(), keywords);
            Map<String, Object> map = new HashMap<>();
            map.put("totalCount", totalCount);
            map.put("articles", articles);
            return map;
        } catch (Exception e) {
            log.error("获取文章列表时发生错误", e);
            throw new BusinessException(500, "给文章添加标签时出现异常");
//            return Collections.emptyMap();
        }
    }

    @GetMapping("/{aid}")
    public Article getArticleById(@PathVariable Long aid) {
        return articleService.getArticleById(aid);
    }

    @PutMapping("/dustbin")
    public Result<?> updateArticleState(@RequestParam("aids") Long[] aids, @RequestParam("state") Integer state) {
        articleService.updateArticleState(aids, state);
        return Result.success("文章状态更新成功", null);
    }

    @PutMapping("/restore")
    public Result<?> restoreArticle(@RequestParam("articleId") Integer articleId) {
        articleService.restoreArticle(articleId);
        return Result.success("文章还原成功", null);
    }

    @RequestMapping("/dataStatistics")
    public Map<String, Object> dataStatistics() {
        try {
            Map<String, Object> map = new HashMap<>();
            List<String> categories = articleService.getCategories();
            List<Integer> dataStatistics = articleService.getDataStatistics();
            map.put("categories", categories);
            map.put("ds", dataStatistics);
            return map;
        } catch (Exception e) {
            log.error("获取数据统计信息时发生错误", e);
            throw new BusinessException(500, "给文章添加标签时出现异常");

//            return Collections.emptyMap();
        }
    }
}
