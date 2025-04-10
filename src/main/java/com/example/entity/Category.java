package com.example.entity;

import lombok.*;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    private Long id;
    private String cateName;
    private Timestamp date;
}
