package com.example.common.utils;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@MappedJdbcTypes(JdbcType.DATE)
@MappedTypes(String.class)
public class DateTypeUtil implements TypeHandler<String> {
    // 定义东八区时区
    public static final ZoneId ZONE_SHANGHAI = ZoneId.of("Asia/Shanghai");
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    // 新增静态方法，用于格式化 LocalDateTime
    public static String formatDateTime(ZonedDateTime zonedDateTime) {
        // 将带时区的时间转换为东八区的 LocalDateTime 再格式化
        LocalDateTime localDateTime = zonedDateTime.withZoneSameInstant(ZONE_SHANGHAI).toLocalDateTime();
        return localDateTime.format(FORMATTER);
    }

    @Override
    public void setParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        if (parameter == null) {
            ps.setNull(i, jdbcType.TYPE_CODE);
        } else {
            // 将 String 类型的日期转换为 java.sql.Date 类型
            LocalDate localDate = LocalDate.parse(parameter, FORMATTER);
            ps.setDate(i, java.sql.Date.valueOf(localDate));
        }
    }

    @Override
    public String getResult(ResultSet rs, String columnName) throws SQLException {
        Date date = rs.getDate(columnName);
        return date == null ? null : date.toLocalDate().format(FORMATTER);
    }

    @Override
    public String getResult(ResultSet rs, int columnIndex) throws SQLException {
        Date date = rs.getDate(columnIndex);
        return date == null ? null : date.toLocalDate().format(FORMATTER);
    }

    @Override
    public String getResult(CallableStatement cs, int columnIndex) throws SQLException {
        Date date = cs.getDate(columnIndex);
        return date == null ? null : date.toLocalDate().format(FORMATTER);
    }
}