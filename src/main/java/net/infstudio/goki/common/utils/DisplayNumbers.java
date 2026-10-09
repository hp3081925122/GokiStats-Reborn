package net.infstudio.goki.common.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class DisplayNumbers {
    private DisplayNumbers() {}

    public static String integer(double value) {
        if (!Double.isFinite(value)) return Double.toString(value);
        return BigDecimal.valueOf(value).setScale(0, RoundingMode.HALF_UP).toPlainString();
    }
}
