package com.example.controller;

import com.example.entity.Article;
import com.example.entity.Result;
import com.example.service.ArticleService;
import com.example.common.utils.Util;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/article")
public class ArticleController {

    private static final Logger logger = LoggerFactory.getLogger(ArticleController.class);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

    private final ArticleService articleService;

    // 构造函数注入
    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @PostMapping("/")
    public Result<?> addNewArticle(Article article) {
        try {
            int result = articleService.addNewArticle(article);
            if (result == 1) {
                return new Result<>(201,"success", article.getId() + "");
            } else {
                return new Result<>(500,"error", article.getState() == 0 ? "文章保存失败!" : "文章发表失败!");
            }
        } catch (Exception e) {
            logger.error("添加文章时发生错误", e);
            return new Result<>(500,"error", "添加文章时发生错误");
        }
    }

    /**
     * 上传图片
     *
     * @return 返回值为图片的地址
     */
    @PostMapping("/uploadimg")
    public Result<?> uploadImg(HttpServletRequest req, MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return new Result<>(400,"error", "上传的图片不能为空");
        }
        try {
            String filePath = "/blogimg/" + DATE_FORMAT.format(new Date());
            String imgFolderPath = req.getServletContext().getRealPath(filePath);
            File imgFolder = new File(imgFolderPath);
            if (!imgFolder.exists()) {
                if (!imgFolder.mkdirs()) {
                    // 目录创建失败，记录日志并返回错误结果
                    logger.error("无法创建图片存储目录: {}", imgFolderPath);
                    return new Result<>(500,"error", "无法创建图片存储目录");
                }
            }
            String url = buildImageUrl(req, filePath);
            String imgName = UUID.randomUUID() + "_" + Objects.requireNonNull(image.getOriginalFilename()).replaceAll(" ", "");
            IOUtils.write(image.getBytes(), new FileOutputStream(new File(imgFolder, imgName)));
            url = url + "/" + imgName;
            return new Result<>(201,"success", url);
        } catch (IOException e) {
            logger.error("上传图片时发生错误", e);
            return new Result<>(500,"error", "上传失败!");
        }
    }

    private String buildImageUrl(HttpServletRequest req, String filePath) {
        return req.getScheme() +
                "://" +
                req.getServerName() +
                ":" +
                req.getServerPort() +
                req.getContextPath() +
                filePath;
    }

    @GetMapping("/all")
    public Map<String, Object> getArticleByState(@RequestParam(value = "state", defaultValue = "-1") Integer state,
                                                 @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                 @RequestParam(value = "count", defaultValue = "6") Integer count,
                                                 @RequestParam(value = "keywords", required = false) String keywords) {
        try {
            int totalCount = articleService.getArticleCountByState(state, Util.getCurrentUser().getId(), keywords);
            List<Article> articles = articleService.getArticleByState(state, page, count, keywords);
            Map<String, Object> map = new HashMap<>();
            map.put("totalCount", totalCount);
            map.put("articles", articles);
            return map;
        } catch (Exception e) {
            logger.error("获取文章列表时发生错误", e);
            return Collections.emptyMap();
        }
    }

    @GetMapping("/{aid}")
    public Article getArticleById(@PathVariable Long aid) {
        try {
            return articleService.getArticleById(aid);
        } catch (Exception e) {
            logger.error("获取文章详情时发生错误，文章 ID: {}", aid, e);
            return null;
        }
    }

    @PutMapping("/dustbin")
    public Result<?> updateArticleState(@RequestParam("aids") Long[] aids, @RequestParam("state") Integer state) {
        if (aids == null || aids.length == 0) {
            return new Result<>(400,"error", "文章 ID 数组不能为空");
        }
        try {
            if (articleService.updateArticleState(aids, state) == aids.length) {
                return new Result<>(204,"success", "删除成功!");
            }
            return new Result<>(500,"error", "删除失败!");
        } catch (Exception e) {
            logger.error("更新文章状态时发生错误", e);
            return new Result<>(500,"error", "更新文章状态时发生错误");
        }
    }

    @PutMapping("/restore")
    public Result<?> restoreArticle(@RequestParam("articleId") Integer articleId) {
        if (articleId == null) {
            return new Result<>(400,"error", "文章 ID 不能为空");
        }
        try {
            if (articleService.restoreArticle(articleId) == 1) {
                return new Result<>(204,"success", "还原成功!");
            }
            return new Result<>(500,"error", "还原失败!");
        } catch (Exception e) {
            logger.error("还原文章时发生错误，文章 ID: {}", articleId, e);
            return new Result<>(500,"error", "还原文章时发生错误");
        }
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
            logger.error("获取数据统计信息时发生错误", e);
            return Collections.emptyMap();
        }
    }
}