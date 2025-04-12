package com.example.common.utils;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@MappedJdbcTypes(JdbcType.DATE)
@MappedTypes(String.class)
public class DateTypeUtil implements TypeHandler<String> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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