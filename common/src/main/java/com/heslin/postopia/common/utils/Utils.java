package com.heslin.postopia.common.utils;

import com.heslin.postopia.common.exception.BadRequestException;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Utils {

    @FunctionalInterface
    public interface TriFunction<T, U, V, R> {
        R apply(T t, U u, V v);
    }

    @FunctionalInterface
    public interface QuaFunction<T, U, V, W, R> {
        R apply(T t, U u, V v, W w);
    }

    public static <T, U, V, W, R> List<R> quaMerge(
    List<T> mainRecords,
    List<U> subRecords1,
    Function<U, Long> subKeyExtractor1,
    BiFunction<T, Map<Long, U>, U> subRecordExtractor1,
    List<V> subRecords2,
    Function<V, Long> subKeyExtractor2,
    BiFunction<T, Map<Long, V>, V> subRecordExtractor2,
    List<W> subRecords3,
    Function<W, Long> subKeyExtractor3,
    BiFunction<T, Map<Long, W>, W> subRecordExtractor3,
    QuaFunction<T, U, V, W, R> recordConstructor) { // 新增构造器参数

        Map<Long, U> subMap1 = subRecords1.stream()
        .collect(Collectors.toMap(subKeyExtractor1, Function.identity()));
        Map<Long, V> subMap2 = subRecords2.stream()
        .collect(Collectors.toMap(subKeyExtractor2, Function.identity()));
        Map<Long, W> subMap3 = subRecords3.stream()
        .collect(Collectors.toMap(subKeyExtractor3, Function.identity()));

        return mainRecords.stream()
        .map(main -> {
            U sub1 = subRecordExtractor1.apply(main, subMap1);
            V sub2 = subRecordExtractor2.apply(main, subMap2);
            W sub3 = subRecordExtractor3.apply(main, subMap3);
            return recordConstructor.apply(main, sub1, sub2, sub3);
        })
        .toList();
    }

    public static <T, U, V, R> List<R> triMerge(
    List<T> mainRecords,
    List<U> subRecords1,
    Function<U, Long> subKeyExtractor1,
    BiFunction<T, Map<Long, U>, U> subRecordExtractor1,
    List<V> subRecords2,
    Function<V, Long> subKeyExtractor2,
    BiFunction<T, Map<Long, V>, V> subRecordExtractor2,
    TriFunction<T, U, V, R> recordConstructor) { // 新增构造器参数

        Map<Long, U> subMap1 = subRecords1.stream()
        .collect(Collectors.toMap(subKeyExtractor1, Function.identity()));
        Map<Long, V> subMap2 = subRecords2.stream()
        .collect(Collectors.toMap(subKeyExtractor2, Function.identity()));

        return mainRecords.stream()
        .map(main -> {
            U sub1 = subRecordExtractor1.apply(main, subMap1);
            V sub2 = subRecordExtractor2.apply(main, subMap2);
            return recordConstructor.apply(main, sub1, sub2);
        })
        .toList();
    }


    public static <T extends Enum<T>> T getEnumByOrdinal(Class<T> enumClass, int ordinal) {
        T[] values = enumClass.getEnumConstants();
        if (ordinal < 0 || ordinal >= values.length) {
            throw new IllegalArgumentException("Invalid ordinal for " + enumClass.getSimpleName());
        }
        return values[ordinal];
    }

    public static void checkRequestBody(Object record) {
        if (!allFieldsNonNull(record)) {
            throw new BadRequestException("parameters are required");
        }
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
