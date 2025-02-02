package be.sugoi.wopr.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class StringUtils {
    public static String rpad(String str, int length, char padChar) {
        return String.format("%-" + length + "s", str).replace(' ', padChar);
    }

    public static String rpad(String str, int length) {
        return rpad(str, length, ' ');
    }

    public static String fill(int length) {
        return rpad(" ", length, ' ');
    }

    public static String fill(int length, char c) {
        return rpad(" ", length, c);
    }

    public static String formatWithThousandSeparator(long number) {
        var decimalFormat = new DecimalFormat("#,###");
        return decimalFormat.format(number);
    }

    public static String formatToPercentage(double number, int digits) {
        NumberFormat percentFormat = NumberFormat.getPercentInstance();
        percentFormat.setMinimumFractionDigits(digits);
        return percentFormat.format(number);
    }

    public static String formatToPercentage(double number) {
        return formatToPercentage(number, 0);
    }
}
