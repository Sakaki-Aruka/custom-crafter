package com.github.sakakiaruka.customcrafter.customcrafter.util;

import com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CalcUtil {

    public static String getContent(Map<String, String> data, String formula) {
        return setEvalValue(setPlaceholderValue(data, formula));
    }

    public static String recipeContainerTagPlaceholder(Set<String> data, String formula) {
        StringBuilder result = new StringBuilder();
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < formula.length(); i++) {
            char c = formula.charAt(i);
            if (buffer.isEmpty()) {
                if (c == '%' && (i == 0 || formula.charAt(i - 1) != '\\')) {
                    buffer.append(c);
                } else result.append(c);
            } else if (c != '%' || formula.charAt(i - 1) == '\\') {
                buffer.append((i < formula.length() - 1 && formula.charAt(i + 1) == '%' && c == '\\') ? "" : c);
            } else {
                result.append(data.stream().anyMatch(e -> e.matches(buffer.substring(1)))); // skip the first character. (= "%")
                buffer.setLength(0);
            }
        }
        return result.append(!buffer.isEmpty() ? buffer : "").toString();
    }

    public static String setPlaceholderValue(Map<String, String> data, String formula) {
        // debug (in release, this method must be private)
        // %variableName% -> replace
        // \%variableName\% -> not replace
        StringBuilder result = new StringBuilder();
        StringBuilder buffer = new StringBuilder();
        int flag = 0;
        for (int i = 0; i < formula.length(); i++) {
            char c = formula.charAt(i);
            if (c != '%' && c != '\\' && flag == 0) {
                result.append(c);
            } else if (c == '\\' && i <= formula.length() - 2 && flag == 0) {
                if (formula.charAt(i + 1) != '%') {
                    result.append(c);
                } else {
                    // next char is '%'
                    result.append('%');
                    i++;
                }
            } else if (c == '%' && flag == 0) {
                flag = 1;
            } else if (c != '%' && flag == 1) {
                buffer.append(c);
            } else if (c == '%') {
                flag = 0;
                String key = buffer.toString();
                if (!data.containsKey(key)) {
                    if (key.matches("random\\[([0-9-]+)?:([0-9-]+)?]")) {
                        result.append(getRandomNumber(key, Integer.MIN_VALUE, Integer.MAX_VALUE));
                    } else result.append(data.getOrDefault("@default@", "None"));
                } else result.append(data.get(buffer.toString()));
                buffer.setLength(0);
            }
        }
        return result + (!buffer.isEmpty() ? buffer.toString() : "");
    }

    public static String setEvalValue(String formula) {
        // debug (in release, this method must be private)
        // {2+3} -> 5
        // {2^3} -> 8
        // setEvalValue(data, setPlaceholderValue(data, "{%TEST%+%TEST_2%}"))
        // - TEST=1, TEST=2 -> 3
        StringBuilder result = new StringBuilder();
        StringBuilder buffer = new StringBuilder();
        int flag = 0;
        for (int i = 0; i < formula.length(); i++) {
            char c = formula.charAt(i);
            if (c != '{' && c != '}' && c != '\\' && flag == 0) {
                result.append(c);
            } else if (c == '\\' && i <= formula.length() - 2 && flag == 0) {
                char after = formula.charAt(i + 1);
                if (after != '{' && after != '}') {
                    result.append(c);
                } else {
                    // next char is '{' or '}'
                    result.append(after);
                    i++;
                }
            } else if (c == '{' && flag == 0) {
                flag = 1;
            } else if (c != '}' && flag == 1) {
                buffer.append(c);
            } else if (c == '}') {
                flag = 0;
                String key = buffer.toString();
                if (key.startsWith("long:")) {
                    // {long:~~~~~}
                    result.append((long) Expression.eval(key.substring(5)).asDouble());
                } else if (key.startsWith("double:")) {
                    // {double:~~~~}
                    result.append(Expression.eval(key.substring(7)).asDouble());
                } else {
                    // {~~~~}
                    result.append(Expression.eval(key).asString());
                }
                buffer.setLength(0);
            }
        }
        return result + (!buffer.isEmpty() ? buffer.toString() : "");
    }

    public static double doubleRound(String formula) {
        // pattern = "double->([0-9]+)p:(-?[0-9]+)\.([0-9]+)"
        // ~~~.0123456789
        // e.g.) double->-3p:{1/3}
        // --> 0.333
        Matcher matcher = Pattern.compile("double->([1-9]([0-9])?)p:(-?[0-9]+)\\.([0-9]+)").matcher(formula);
        if (!matcher.matches()) {
            CustomCrafter.getInstance().getLogger().warning("Double round failed. Follow 'double->([1-9]([0-9])?)p:(-?[0-9]+)\\.([0-9]+)'.");
            CustomCrafter.getInstance().getLogger().warning("The system returned 0.0 as a result of a expression '" + formula + "'.");
            return 0.0d;
        }
        int limit = Integer.parseInt(matcher.group(1));
        double d = Double.parseDouble(matcher.group(3) + "." + matcher.group(4));
        DecimalFormat format = new DecimalFormat("#.#");
        format.setMaximumFractionDigits(Math.abs(limit));
        return Double.parseDouble(format.format(d));
    }

    public static long longRound(String formula) {
        // pattern = "long->([0-9]+)p:([0-9]+)"
        // if result's length is shorter than required, return direct
        Matcher matcher = Pattern.compile("long->([1-9]([0-9])?)p:(-?[0-9]+)\\.([0-9]+)").matcher(formula);
        if (!matcher.matches()) {
            CustomCrafter.getInstance().getLogger().warning("Long round failed. Follow 'long->([1-9]([0-9])?)p:(-?[0-9]+)'.");
            CustomCrafter.getInstance().getLogger().warning("The system returned 0 as a result of a expression '" + formula + "'.");
            return 0L;
        }
        int limit = Integer.parseInt(matcher.group(1));
        String number = matcher.group(3);
        long l = Long.parseLong(number);
        long beforeDigits = Math.round(Math.floor(Math.log10(l)));
        if (beforeDigits + 1 <= limit) return l;

        do {
            l = Math.round(l / 10d);
        } while (Math.round(Math.floor(Math.log10(l))) + 1 > limit);

        long afterDigits = Math.round(Math.floor(Math.log10(l)));
        long tens = beforeDigits - afterDigits;
        return (long) (Math.pow(10, tens)) * l;
    }

    public static int getRandomNumber(String formula, int underLimit, int upperLimit) {
        // e.g. [3:] (3 ~ upper limit)
        // e.g. [:10] (under limit ~ 10)
        // e.g. [5:20] (5 ~ 20)
        // e.g. [:] (under limit ~ upper limit)

        final String pattern = "random\\[(-?[0-9]{1,10})?:(-?[0-9]{1,10})?]";
        Matcher parsed = Pattern.compile(pattern).matcher(formula);
        if (!parsed.matches()) {
            if (formula.matches("-?[0-9]{1,10}+")) {
                int value = Integer.parseInt(formula);
                if (underLimit <= value && value <= upperLimit) return value;
                else if (value < underLimit) return underLimit;
                else return upperLimit;
            }
            return underLimit;
        }

        if (underLimit == upperLimit) return upperLimit;
        if (upperLimit < underLimit) {
            int temp = underLimit;
            underLimit = upperLimit;
            upperLimit = temp;
        }
        if (parsed.group(1) == null && parsed.group(2) == null) {
            // [:]
            return getInRange(underLimit, upperLimit + 1);
        } else if (parsed.group(1) != null && parsed.group(2) == null) {
            // [([0-9-]+):]
            return getInRange(Math.max(Integer.parseInt(parsed.group(1)), underLimit), upperLimit + 1);
        } else if (parsed.group(1) == null && parsed.group(2) != null) {
            // [:([0-9-]+)]
            return getInRange(underLimit, Math.min(Integer.parseInt(parsed.group(2)), upperLimit) + 1);
        } else {
            // [([0-9-]+):([0-9-]+)]
            return getInRange(Math.max(Integer.parseInt(parsed.group(1)), underLimit), Math.min(Integer.parseInt(parsed.group(2)), upperLimit) + 1);
        }
    }

    private static int getInRange(int under, int upper) {
        return new Random().ints(1, under, upper).toArray()[0];
    }
}
