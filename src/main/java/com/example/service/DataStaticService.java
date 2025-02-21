package com.example.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by sang on 2017/12/25.
 */
@Component
public class DataStaticService {
    final
    ArticleService articleService;

    public DataStaticService(ArticleService articleService) {
        this.articleService = articleService;
    }

    //每天执行一次，统计PV
    @Scheduled(cron = "1 0 0 * * ?")
    public void pvStatisticsPerDay() {
        articleService.pvStatisticsPerDay();
    }
}