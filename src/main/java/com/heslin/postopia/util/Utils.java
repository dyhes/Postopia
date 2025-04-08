package com.heslin.postopia.util;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Objects;

public class Utils {
    public static <T extends Enum<T>> T getEnumByOrdinal(Class<T> enumClass, int ordinal) {
        T[] values = enumClass.getEnumConstants();
        if (ordinal < 0 || ordinal >= values.length) {
            throw new IllegalArgumentException("Invalid ordinal for " + enumClass.getSimpleName());
        }
        return values[ordinal];
    }

    public static boolean allFieldsNonNull(Object record) {
        // 1. 验证对象是否为 Record 类型
        if (!record.getClass().isRecord()) {
            throw new IllegalArgumentException("Object is not a Record");
        }

        // 2. 获取 Record 的所有字段组件
        RecordComponent[] components = record.getClass().getRecordComponents();

        return Arrays.stream(components)
        .allMatch(component -> {
            try {
                // 3. 调用访问器方法获取字段值
                Object value = component.getAccessor().invoke(record);
                return Objects.nonNull(value);
            } catch (Exception e) {
                throw new RuntimeException("Failed to access field: " + component.getName(), e);
            }
        });
    }
}
