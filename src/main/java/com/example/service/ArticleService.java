package com.example.service;

import com.example.entity.Article;
import com.example.mapper.ArticleMapper;
import com.example.mapper.TagMapper;
import com.example.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class ArticleService {

    private static final Logger logger = LoggerFactory.getLogger(ArticleService.class);
    private static final int SUMMARY_LENGTH = 50;

    private final ArticleMapper articleMapper;
    private final TagMapper tagsMapper;

    public ArticleService(ArticleMapper articleMapper, TagMapper tagsMapper) {
        this.articleMapper = articleMapper;
        this.tagsMapper = tagsMapper;
    }

    public int addNewArticle(Article article) {
        if (article == null) {
            logger.error("传入的文章对象为空，无法添加新文章");
            return -1;
        }
        // 处理文章摘要
        if (article.getSummary() == null || article.getSummary().isEmpty()) {
            String stripHtml = stripHtml(article.getHtmlContent());
            article.setSummary(stripHtml.substring(0, Math.min(stripHtml.length(), SUMMARY_LENGTH)));
        }
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        if (article.getState() == 1) {
            article.setPublishDate(timestamp);
        }
        article.setEditTime(timestamp);
        article.setUid(Util.getCurrentUser().getId());

        int result;
        if (article.getId() == -1) {
            // 添加操作
            result = articleMapper.addNewArticle(article);
        } else {
            // 更新操作
            result = articleMapper.updateArticle(article);
        }

        // 打标签
        String[] dynamicTags = article.getDynamicTags();
        if (dynamicTags != null && dynamicTags.length > 0) {
            int tagsResult = addTagsToArticle(dynamicTags, article.getId());
            if (tagsResult == -1) {
                return tagsResult;
            }
        }
        return result;
    }

    private int addTagsToArticle(String[] dynamicTags, Long aid) {
        try {
            // 1. 删除该文章目前所有的标签
            tagsMapper.deleteTagsByAid(aid);
            // 2. 将上传上来的标签全部存入数据库
            tagsMapper.saveTags(dynamicTags);
            // 3. 查询这些标签的id
            List<Long> tIds = tagsMapper.getTagsIdByTagName(dynamicTags);
            // 4. 重新给文章设置标签
            int i = tagsMapper.saveTags2ArticleTags(tIds, aid);
            return i == dynamicTags.length ? i : -1;
        } catch (Exception e) {
            logger.error("给文章添加标签时出现异常，文章ID: {}", aid, e);
            return -1;
        }
    }

    public String stripHtml(String content) {
        if (content == null) {
            return "";
        }
        content = content.replaceAll("<p .*?>", "");
        content = content.replaceAll("<br\\s*/?>", "");
        content = content.replaceAll("<.*?>", "");
        return content;
    }

    public List<Article> getArticleByState(Integer state, Integer page, Integer count, String keywords) {
        if (state == null || page == null || count == null) {
            logger.error("获取文章列表时，状态、页码或每页数量参数为空");
            return Collections.emptyList();
        }
        int start = (page - 1) * count;
        Long uid = Util.getCurrentUser().getId();
        try {
            return articleMapper.getArticleByState(state, start, count, uid, keywords);
        } catch (Exception e) {
            logger.error("获取文章列表时出现异常，状态: {}, 页码: {}, 每页数量: {}, 关键词: {}", state, page, count, keywords, e);
            return Collections.emptyList();
        }
    }

    public int getArticleCountByState(Integer state, Long uid, String keywords) {
        if (state == null || uid == null) {
            logger.error("获取文章数量时，状态或用户ID参数为空");
            return 0;
        }
        try {
            return articleMapper.getArticleCountByState(state, uid, keywords);
        } catch (Exception e) {
            logger.error("获取文章数量时出现异常，状态: {}, 用户ID: {}, 关键词: {}", state, uid, keywords, e);
            return 0;
        }
    }

    public int updateArticleState(Long[] aids, Integer state) {
        if (aids == null || aids.length == 0 || state == null) {
            logger.error("更新文章状态时，文章ID数组或状态参数为空");
            return 0;
        }
        try {
            if (state == 2) {
                return articleMapper.deleteArticleById(aids);
            } else {
                return articleMapper.updateArticleState(aids, 1); // 放入到回收站中
            }
        } catch (Exception e) {
            logger.error("更新文章状态时出现异常，文章ID数组: {}, 状态: {}", aids, state, e);
            return 0;
        }
    }

    public int restoreArticle(Integer articleId) {
        if (articleId == null) {
            logger.error("还原文章时，文章ID参数为空");
            return 0;
        }
        try {
            return articleMapper.updateArticleStateById(articleId, 1); // 从回收站还原在原处
        } catch (Exception e) {
            logger.error("还原文章时出现异常，文章ID: {}", articleId, e);
            return 0;
        }
    }

    public Article getArticleById(Long aid) {
        if (aid == null) {
            logger.error("获取文章详情时，文章ID参数为空");
            return null;
        }
        try {
            Article article = articleMapper.getArticleById(aid);
            articleMapper.pvIncrement(aid);
            return article;
        } catch (Exception e) {
            logger.error("获取文章详情时出现异常，文章ID: {}", aid, e);
            return null;
        }
    }

    public void pvStatisticsPerDay() {
        try {
            articleMapper.pvStatisticsPerDay();
        } catch (Exception e) {
            logger.error("每日PV统计时出现异常", e);
        }
    }

    /**
     * 获取最近七天的日期
     */
    public List<String> getCategories() {
        Long uid = Util.getCurrentUser().getId();
        try {
            return articleMapper.getCategories(uid);
        } catch (Exception e) {
            logger.error("获取文章分类时出现异常，用户ID: {}", uid, e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取最近七天的数据
     */
    public List<Integer> getDataStatistics() {
        Long uid = Util.getCurrentUser().getId();
        try {
            return articleMapper.getDataStatistics(uid);
        } catch (Exception e) {
            logger.error("获取数据统计信息时出现异常，用户ID: {}", uid, e);
            return Collections.emptyList();
        }
    }
}