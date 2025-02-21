package com.example.entity;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;

@Setter
@Getter
public class Article {
    private Long id;
    private String title;
    private String mdContent;
    private String htmlContent;
    private String summary;
    private Long cid;
    private Long uid;
    private Timestamp publishDate;
    private Integer state;
    private Integer pageView;
    private Timestamp editTime;
    private String[] dynamicTags;
    private String cateName;
    private List<Tag> tags;
    private String stateStr;

}
