package com.example.service;

import com.example.common.exceptions.BusinessException;
import com.example.entity.Article;
import com.example.mapper.ArticleMapper;
import com.example.mapper.TagMapper;
import com.example.common.utils.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

@Service
@Slf4j
@Transactional
public class ArticleService {

    private static final int SUMMARY_LENGTH = 50;
    private final ArticleMapper articleMapper;
    private final TagMapper tagsMapper;

    public ArticleService(ArticleMapper articleMapper, TagMapper tagsMapper) {
        this.articleMapper = articleMapper;
        this.tagsMapper = tagsMapper;
    }

    public void addNewArticle(Article article) {
        validateArticle(article);
        handleSummary(article);
        setTimestampsAndUid(article);

        if (article.getId() == -1) {
            executeInsert(article);
        } else {
            executeUpdate(article);
        }

        handleTags(article);
    }

    private void validateArticle(Article article) {
        if (article == null) {
            log.error("传入的文章对象为空，无法添加新文章");
            throw new BusinessException(400, "传入的文章对象为空，无法添加新文章");
        }
    }

    private void handleSummary(Article article) {
        if (article.getSummary() == null || article.getSummary().isEmpty()) {
            String stripHtml = stripHtml(article.getHtmlContent());
            article.setSummary(stripHtml.substring(0, Math.min(stripHtml.length(), SUMMARY_LENGTH)));
        }
    }

    private void setTimestampsAndUid(Article article) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        if (article.getState() == 1) {
            article.setPublishDate(timestamp);
        }
        article.setEditTime(timestamp);
        article.setUid(Util.getCurrentUser().getId());
    }

    private void executeInsert(Article article) {
        int result = articleMapper.addNewArticle(article);
        if (result != 1) {
            throw new BusinessException(500, "文章保存失败");
        }
    }

    private void executeUpdate(Article article) {
        int result = articleMapper.updateArticle(article);
        if (result != 1) {
            throw new BusinessException(500, "文章更新失败");
        }
    }

    private void handleTags(Article article) {
        String[] dynamicTags = article.getDynamicTags();
        if (dynamicTags != null && dynamicTags.length > 0) {
            int tagsResult = addTagsToArticle(dynamicTags, article.getId());
            if (tagsResult != dynamicTags.length) {
                throw new BusinessException(500, "文章标签添加失败");
            }
        }
    }

    private int addTagsToArticle(String[] dynamicTags, Long aid) {
        try {
            tagsMapper.deleteTagsByAid(aid);
            tagsMapper.saveTags(dynamicTags);
            List<Long> tIds = tagsMapper.getTagsIdByTagName(dynamicTags);
            return tagsMapper.saveTags2ArticleTags(tIds, aid);
        } catch (Exception e) {
            log.error("给文章添加标签时出现异常，文章ID: {}", aid, e);
            throw new BusinessException(500, "给文章添加标签时出现异常");
        }
    }

    public String stripHtml(String content) {
        if (content == null) {
            return "";
        }
        return content.replaceAll("<p .*?>", "")
                .replaceAll("<br\\s*/?>", "")
                .replaceAll("<.*?>", "");
    }

    public List<Article> getArticleByState(Integer state, Integer page, Integer count, String keywords) {
        validatePaginationParams(state, page, count);
        int start = (page - 1) * count;
        Long uid = Util.getCurrentUser().getId();
        try {
            return articleMapper.getArticleByState(state, start, count, uid, keywords);
        } catch (Exception e) {
            log.error("获取文章列表时出现异常，状态: {}, 页码: {}, 每页数量: {}, 关键词: {}", state, page, count, keywords, e);
            throw new BusinessException(500, "获取文章列表时出现异常");
        }
    }

    private void validatePaginationParams(Integer state, Integer page, Integer count) {
        if (state == null || page == null || count == null) {
            log.error("获取文章列表时，状态、页码或每页数量参数为空");
            throw new BusinessException(400, "获取文章列表时，状态、页码或每页数量参数为空");
        }
    }

    public int getArticleCountByState(Integer state, Long uid, String keywords) {
        validateArticleCountParams(state, uid);
        try {
            return articleMapper.getArticleCountByState(state, uid, keywords);
        } catch (Exception e) {
            log.error("获取文章数量时出现异常，状态: {}, 用户ID: {}, 关键词: {}", state, uid, keywords, e);
            throw new BusinessException(500, "获取文章数量时出现异常");
        }
    }

    private void validateArticleCountParams(Integer state, Long uid) {
        if (state == null || uid == null) {
            log.error("获取文章数量时，状态或用户ID参数为空");
            throw new BusinessException(400, "获取文章数量时，状态或用户ID参数为空");
        }
    }

    public void updateArticleState(Long[] aids, Integer state) {
        validateUpdateArticleStateParams(aids, state);
        try {
            if (state == 2) {
                int result = articleMapper.deleteArticleById(aids);
                if (result != aids.length) {
                    throw new BusinessException(500, "文章删除失败");
                }
            } else {
                int result = articleMapper.updateArticleState(aids, 1);
                if (result != aids.length) {
                    throw new BusinessException(500, "文章状态更新失败");
                }
            }
        } catch (Exception e) {
            log.error("更新文章状态时出现异常，文章ID数组: {}, 状态: {}", aids, state, e);
            throw new BusinessException(500, "更新文章状态时出现异常");
        }
    }

    private void validateUpdateArticleStateParams(Long[] aids, Integer state) {
        if (aids == null || aids.length == 0 || state == null) {
            log.error("更新文章状态时，文章ID数组或状态参数为空");
            throw new BusinessException(400, "更新文章状态时，文章ID数组或状态参数为空");
        }
    }

    public void restoreArticle(Integer articleId) {
        if (articleId == null) {
            log.error("还原文章时，文章ID参数为空");
            throw new BusinessException(400, "还原文章时，文章ID参数为空");
        }
        try {
            int result = articleMapper.updateArticleStateById(articleId, 1);
            if (result != 1) {
                throw new BusinessException(500, "文章还原失败");
            }
        } catch (Exception e) {
            log.error("还原文章时出现异常，文章ID: {}", articleId, e);
            throw new BusinessException(500, "还原文章时出现异常");
        }
    }

    public Article getArticleById(Long aid) {
        if (aid == null) {
            log.error("获取文章详情时，文章ID参数为空");
            throw new BusinessException(400, "获取文章详情时，文章ID参数为空");
        }
        try {
            Article article = articleMapper.getArticleById(aid);
            articleMapper.pvIncrement(aid);
            return article;
        } catch (Exception e) {
            log.error("获取文章详情时出现异常，文章ID: {}", aid, e);
            throw new BusinessException(500, "获取文章详情时出现异常");
        }
    }

    public void pvStatisticsPerDay() {
        try {
            articleMapper.pvStatisticsPerDay();
        } catch (Exception e) {
            log.error("每日PV统计时出现异常", e);
            throw new BusinessException(500, "每日PV统计时出现异常");
        }
    }

    public List<String> getCategories() {
        Long uid = Util.getCurrentUser().getId();
        try {
            return articleMapper.getCategories(uid);
        } catch (Exception e) {
            log.error("获取文章分类时出现异常，用户ID: {}", uid, e);
            throw new BusinessException(500, "获取文章分类时出现异常");
        }
    }

    public List<Integer> getDataStatistics() {
        Long uid = Util.getCurrentUser().getId();
        try {
            return articleMapper.getDataStatistics(uid);
        } catch (Exception e) {
            log.error("获取数据统计信息时出现异常，用户ID: {}", uid, e);
            throw new BusinessException(500, "获取数据统计信息时出现异常");
        }
    }
}
