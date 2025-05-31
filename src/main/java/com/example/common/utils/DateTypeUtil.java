package com.example.common.utils;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

@MappedJdbcTypes(JdbcType.TIMESTAMP)
@MappedTypes(LocalDateTime.class)
public class DateTypeUtil implements TypeHandler<LocalDateTime> {
    // 定义东八区时区
    public static final ZoneId ZONE_SHANGHAI = ZoneId.of("Asia/Shanghai");
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // 格式化ZonedDateTime为东八区时间字符串
    public static String formatDateTime(ZonedDateTime zonedDateTime) {
        return zonedDateTime.withZoneSameInstant(ZONE_SHANGHAI)
                .format(DATE_TIME_FORMATTER);
    }

    // 格式化LocalDateTime为字符串
    public static String formatDateTime(LocalDateTime localDateTime) {
        return localDateTime.format(DATE_TIME_FORMATTER);
    }

    // 格式化LocalDate为字符串
    public static String formatDate(LocalDate localDate) {
        return localDate.format(DATE_FORMATTER);
    }

    // 解析字符串为LocalDateTime
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        return LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
    }

    // 解析字符串为LocalDate
    public static LocalDate parseDate(String dateStr) {
        return LocalDate.parse(dateStr, DATE_FORMATTER);
    }

    @Override
    public void setParameter(PreparedStatement ps, int i, LocalDateTime parameter, JdbcType jdbcType) throws SQLException {
        if (parameter == null) {
            ps.setNull(i, Types.TIMESTAMP);
        } else {
            // 将LocalDateTime转换为带时区的时间戳
            ZonedDateTime zonedDateTime = parameter.atZone(ZONE_SHANGHAI);
            ps.setTimestamp(i, Timestamp.valueOf(zonedDateTime.toLocalDateTime()));
        }
    }

    @Override
    public LocalDateTime getResult(ResultSet rs, String columnName) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(columnName);
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    @Override
    public LocalDateTime getResult(ResultSet rs, int columnIndex) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(columnIndex);
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    @Override
    public LocalDateTime getResult(CallableStatement cs, int columnIndex) throws SQLException {
        Timestamp timestamp = cs.getTimestamp(columnIndex);
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
