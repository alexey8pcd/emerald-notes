package ru.alexey_ovcharov.webserver.common.util;

import java.math.BigDecimal;
import org.apache.commons.lang.StringUtils;

/**
 * @author Alexey
 */
public class LangUtil {

    @Nullable
    public static BigDecimal toDecimalOrNullIfEmpty(String source) {
        if (StringUtils.isNotEmpty(source)) {
            return new BigDecimal(source);
        } else {
            return null;
        }
    }

}
