package arabic.ner;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * s0ul on 2/24/15.
 */
public class DateExtractor {
    private static final String EN_MONTH = "JANUARY|JAN|FEBRUARY|FEB|MARCH|MAR|APRIL|APR|MAY|JUNE|JUN|JULY|JUL|AUGUST|AUG|SEPTEMBER|SEP|OCTOBER|OCT|NOVEMBER|NOV|DECEMBER|DEC";
    private static final String AR_MONTH = "يناير|فبراير|مارس|ابريل|مايو|يونيو|يونيه|يوليو|يوليو|اغسطس|سبتمبر|اكتوبر|نوفمبر|ديسمبر";
    private static final String IA_NUMERAL = "[٠|١|٢|٣|٤|٥|٦|٧|٨|٩]";
    private static final String AR_YEAR = "(?<Year>(19|20)\\d{2})";
    private static final String IA_YEAR = String.format("(?<Year>%s{2}(?:٠٢|٩١))", IA_NUMERAL);
    private static final String IA_YEAR2 = String.format("%s{2}(?:٠٢|٩١)", IA_NUMERAL);
    private static final String AR_YYYY_MM_DD = String.format("(?<Day>(\\d|%s){1,2}).(?<Month>%s).(?<Year>(\\d{2}(?:91|02)|%s))", IA_NUMERAL, AR_MONTH, IA_YEAR2);
    private static final String AR_MM_DD_YYYY = String.format("(?<Month>%s{1,2}).(?<Day>%s{1,2}).%s", IA_NUMERAL, IA_NUMERAL, IA_YEAR);
    private static final Pattern P_AR_YYYY_MM_DD = Pattern.compile(AR_YYYY_MM_DD, Pattern.CASE_INSENSITIVE);
    private static final Pattern P_AR_MM_DD_YYYY = Pattern.compile(AR_MM_DD_YYYY, Pattern.CASE_INSENSITIVE);
    private static final Pattern P_AR_MONTH = Pattern.compile(AR_MONTH, Pattern.CASE_INSENSITIVE);
    private static final Pattern P_EN_MONTH = Pattern.compile(EN_MONTH, Pattern.CASE_INSENSITIVE);
    private static final Pattern P_AR_YEAR = Pattern.compile(AR_YEAR, Pattern.CASE_INSENSITIVE);
    private static final Pattern P_IA_YEAR = Pattern.compile(IA_YEAR, Pattern.CASE_INSENSITIVE);

    private static Map<String, String> allToAR = loadARMap();
    private static Map<String, String> allToNum = loadNumeralMap();

    public static Map<String, String> loadARMap() {
        Map<String, String> map = new HashMap<>();

        map.put("jan", "يناير");
        map.put("feb", "فبراير");
        map.put("mar", "مارس");
        map.put("apr", "ابريل");
        map.put("jun", "يونيو");
        map.put("jul", "يوليو");
        map.put("aug", "اغسطس");
        map.put("sep", "سبتمبر");
        map.put("oct", "اكتوبر");
        map.put("nov", "نوفمبر");
        map.put("dec", "ديسمبر");

        map.put("january", "يناير");
        map.put("february", "فبراير");
        map.put("march", "مارس");
        map.put("april", "ابريل");
        map.put("may", "مايو");
        map.put("june", "يونيو");
        map.put("july", "يوليو");
        map.put("august", "اغسطس");
        map.put("september", "سبتمبر");
        map.put("october", "اكتوبر");
        map.put("november", "نوفمبر");
        map.put("december", "ديسمبر");

        map.put("1", "يناير");
        map.put("2", "فبراير");
        map.put("3", "مارس");
        map.put("4", "ابريل");
        map.put("5", "مايو");
        map.put("6", "يونيو");
        map.put("7", "يوليو");
        map.put("8", "اغسطس");
        map.put("9", "سبتمبر");
        map.put("10", "اكتوبر");
        map.put("11", "نوفمبر");
        map.put("12", "ديسمبر");

        map.put("يناير", "يناير");
        map.put("فبراير", "فبراير");
        map.put("مارس", "مارس");
        map.put("ابريل", "ابريل");
        map.put("مايو", "مايو");
        map.put("يونيو", "يونيو");
        map.put("يوليو", "يوليو");
        map.put("اغسطس", "اغسطس");
        map.put("سبتمبر", "سبتمبر");
        map.put("اكتوبر", "اكتوبر");
        map.put("نوفمبر", "نوفمبر");
        map.put("ديسمبر", "ديسمبر");

        return map;
    }
    public static Map<String, String> loadNumeralMap() {
        Map<String, String> map = new HashMap<>();

        map.put("0", "٠");
        map.put("1", "١");
        map.put("2", "٢");
        map.put("3", "٣");
        map.put("4", "٤");
        map.put("5", "٥");
        map.put("6", "٦");
        map.put("7", "٧");
        map.put("8", "٨");
        map.put("9", "٩");

        map.put("٠", "٠");
        map.put("١", "١");
        map.put("٢", "٢");
        map.put("٣", "٣");
        map.put("٤", "٤");
        map.put("٥", "٥");
        map.put("٦", "٦");
        map.put("٧", "٧");
        map.put("٨", "٨");
        map.put("٩", "٩");

        return map;
    }

    public static String extract(String content) {
        Matcher matcher = P_AR_YYYY_MM_DD.matcher(content);
        if (matcher.find()) {
            String day = Arrays.stream(matcher.group("Day").split("")).map(allToNum::get).reduce("", (a, b) -> b + a);
            String year = Arrays.stream(matcher.group("Year").split("")).map(allToNum::get).reduce("", (a, b) -> b + a);
            return String.format("%s/%s/%s", day, allToAR.get(matcher.group("Month").toLowerCase()), year);
        }

        matcher = P_AR_MM_DD_YYYY.matcher(content);
        if (matcher.find()) {
            String day = Arrays.stream(matcher.group("Day").split("")).map(allToNum::get).reduce("", (a, b) -> a + b);
            String year = Arrays.stream(matcher.group("Year").split("")).map(allToNum::get).reduce("", (a, b) -> b + a);
            return String.format("%s/%s/%s", day, allToAR.get(matcher.group("Month").toLowerCase()), year);
        }

        return null;
    }
}
