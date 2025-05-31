package com.example.common.enums;

import com.example.common.exceptions.BadRequestException;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public interface BusinessType {
    String getName();          // 业务名

    String getDescription();   // 业务描述

    String getPrefix();        // Redis键前缀

    long getExpireTime();      // 过期时间

    TimeUnit getTimeUnit();    // 时间单位

    // 根据name和枚举类类型查找枚举（泛型方法）
    static <T extends Enum<T> & BusinessType> T fromName(String name, Class<T> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants())
                .filter(
                        type -> type
                                .getName()
                                .equals(name)
                )
                .findFirst()
                .orElseThrow(() -> new BadRequestException(400, "未知业务类型: " + name));
    }

    // 默认方法：计算过期时间（毫秒）
    default long getExpireTimeInterval() {
        return TimeUnit.MILLISECONDS.convert(getExpireTime(), getTimeUnit());
    }
}
