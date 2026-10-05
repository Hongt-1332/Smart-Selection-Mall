package edu.fafu.exception;

import java.math.BigDecimal;
import java.util.Collection;

public interface BusinessExceptionInterface {

    default void ensureNotNull(Object obj, String message) {
        if (obj == null) throw new BusinessException(message);
    }

    default void ensureNull(Object obj, String message) {
        if (obj != null) throw new BusinessException(message);
    }

    default void ensureTrue(boolean condition, String message) {
        if (!condition) throw new BusinessException(message);
    }

    default void ensureFalse(boolean condition, String message) {
        if (condition) throw new BusinessException(message);
    }

    default void ensureEquals(Object a, Object b, String message) {
        if (a == null && b == null) return;
        if (a == null || !a.equals(b)) throw new BusinessException(message);
    }

    default void ensureNotEquals(Object a, Object b, String message) {
        if (a != null && a.equals(b)) throw new BusinessException(message);
    }

    default void ensureNotEmpty(Collection<?> collection, String message) {
        if (collection == null || collection.isEmpty()) throw new BusinessException(message);
    }

    default void ensureEmpty(Collection<?> collection, String message) {
        if (collection != null && !collection.isEmpty()) throw new BusinessException(message);
    }

    default void ensureNotBlank(String str, String message) {
        if (str == null || str.isBlank()) throw new BusinessException(message);
    }

    default void ensurePositive(BigDecimal value, String message) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) throw new BusinessException(message);
    }

    default void ensureSufficientBalance(BigDecimal balance, BigDecimal required, String message) {
        if (balance == null || balance.compareTo(required) < 0) throw new BusinessException(message);
    }

    default void fail(String message) {
        throw new BusinessException(message);
    }

}