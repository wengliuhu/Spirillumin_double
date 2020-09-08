package com.example.spirillumin.zzcsoft.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright:   Copyright(C) 2010-2015 KEDACOM LTD.
 * Project:     Demo
 * Module:      com.kedacom.common
 * Description: DESC
 * Author:      zhoubing
 * Createdate:  2019/5/6
 * Version:     V
 */
public class StringUtil {

    private StringUtil() {
        throw new AssertionError();
    }

    public static final String EMPTY = "";

    public static final String THUMB_STR = "_thumb";

    /**nullStrToDefault
     * is null or its length is 0 or it is made by space
     * <p>
     * <pre>
     * isBlank(null) = true;
     * isBlank(&quot;&quot;) = true;
     * isBlank(&quot;  &quot;) = true;
     * isBlank(&quot;a&quot;) = false;
     * isBlank(&quot;a &quot;) = false;
     * isBlank(&quot; a&quot;) = false;
     * isBlank(&quot;a b&quot;) = false;
     * </pre>
     *
     * @param str
     * @return if string is null or its size is 0 or it is made by space, return true, else return false.
     */
    public static boolean isBlank(String str) {
        return (str == null || str.trim().length() == 0);
    }

    /**
     * is null or its length is 0
     * <p>
     * <pre>
     * isEmpty(null) = true;
     * isEmpty(&quot;&quot;) = true;
     * isEmpty(&quot;  &quot;) = false;
     * </pre>
     *
     * @param str
     * @return if string is null or its size is 0, return true, else return false.
     */
    public static boolean isEmpty(CharSequence str) {
        return (str == null || str.length() == 0);
    }

    /**
     * get length of CharSequence
     * <p>
     * <pre>
     * length(null) = 0;
     * length(\"\") = 0;
     * length(\"abc\") = 3;
     * </pre>
     *
     * @param str
     * @return if str is null or empty, return 0, else return {@link CharSequence#length()}.
     */
    public static int length(CharSequence str) {
        return str == null ? 0 : str.length();
    }

    /**
     * null Object to empty string
     * <p>
     * <pre>
     * nullStrToEmpty(null) = &quot;&quot;;
     * nullStrToEmpty(&quot;&quot;) = &quot;&quot;;
     * nullStrToEmpty(&quot;aa&quot;) = &quot;aa&quot;;
     * </pre>
     *
     * @param str
     * @return
     */
    public static String nullStrToEmpty(Object str) {
        return (str == null ? "" : (str instanceof String ? (String) str : str.toString()));
    }


    /**
     * <use default str>
     *
     * @param str        the str
     * @param defaultStr the default str
     * @return the string
     * @author: guoxiangxun
     * @date: Feb 28, 2018 9:22:48 PM
     * @version: v1.0
     */
    public static String nullStrToDefault(Object str, Object defaultStr) {
        defaultStr = nullStrToEmpty(defaultStr);

        return (str != null ? (str instanceof String ? (String) str : str.toString())
                : (defaultStr instanceof String ? (String) defaultStr : defaultStr.toString()));
    }

    /**
     * capitalize first letter
     * <p>
     * <pre>
     * capitalizeFirstLetter(null)     =   null;
     * capitalizeFirstLetter("")       =   "";
     * capitalizeFirstLetter("2ab")    =   "2ab"
     * capitalizeFirstLetter("a")      =   "A"
     * capitalizeFirstLetter("ab")     =   "Ab"
     * capitalizeFirstLetter("Abc")    =   "Abc"
     * </pre>
     *
     * @param str
     * @return
     */
    public static String capitalizeFirstLetter(String str) {
        if (isEmpty(str)) {
            return str;
        }

        char c = str.charAt(0);
        return (!Character.isLetter(c) || Character.isUpperCase(c)) ? str : new StringBuilder(str.length())
                .append(Character.toUpperCase(c)).append(str.substring(1)).toString();
    }

    /**
     * encoded in utf-8
     * <p>
     * <pre>
     * utf8Encode(null)        =   null
     * utf8Encode("")          =   "";
     * utf8Encode("aa")        =   "aa";
     * utf8Encode("啊啊啊啊")   = "%E5%95%8A%E5%95%8A%E5%95%8A%E5%95%8A";
     * </pre>
     *
     * @param str
     * @return
     * @throws UnsupportedEncodingException if an error occurs
     */
    public static String utf8Encode(String str) {
        if (!isEmpty(str) && str.getBytes().length != str.length()) {
            try {
                return URLEncoder.encode(str, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("UnsupportedEncodingException occurred. ", e);
            }
        }
        return str;
    }

    /**
     * encoded in utf-8, if exception, return defultReturn
     *
     * @param str
     * @param defultReturn
     * @return
     */
    public static String utf8Encode(String str, String defultReturn) {
        if (!isEmpty(str) && str.getBytes().length != str.length()) {
            try {
                return URLEncoder.encode(str, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                return defultReturn;
            }
        }
        return str;
    }

    /**
     * get innerHtml from href
     * <p>
     * <pre>
     * getHrefInnerHtml(null)                                  = ""
     * getHrefInnerHtml("")                                    = ""
     * getHrefInnerHtml("mp3")                                 = "mp3";
     * getHrefInnerHtml("&lt;a innerHtml&lt;/a&gt;")                    = "&lt;a innerHtml&lt;/a&gt;";
     * getHrefInnerHtml("&lt;a&gt;innerHtml&lt;/a&gt;")                    = "innerHtml";
     * getHrefInnerHtml("&lt;a&lt;a&gt;innerHtml&lt;/a&gt;")                    = "innerHtml";
     * getHrefInnerHtml("&lt;a href="baidu.com"&gt;innerHtml&lt;/a&gt;")               = "innerHtml";
     * getHrefInnerHtml("&lt;a href="baidu.com" title="baidu"&gt;innerHtml&lt;/a&gt;") = "innerHtml";
     * getHrefInnerHtml("   &lt;a&gt;innerHtml&lt;/a&gt;  ")                           = "innerHtml";
     * getHrefInnerHtml("&lt;a&gt;innerHtml&lt;/a&gt;&lt;/a&gt;")                      = "innerHtml";
     * getHrefInnerHtml("jack&lt;a&gt;innerHtml&lt;/a&gt;&lt;/a&gt;")                  = "innerHtml";
     * getHrefInnerHtml("&lt;a&gt;innerHtml1&lt;/a&gt;&lt;a&gt;innerHtml2&lt;/a&gt;")        = "innerHtml2";
     * </pre>
     *
     * @param href
     * @return <ul>
     * <li>if href is null, return ""</li>
     * <li>if not match regx, return source</li>
     * <li>return the last string that match regx</li>
     * </ul>
     */
    public static String getHrefInnerHtml(String href) {
        if (isEmpty(href)) {
            return "";
        }

        String hrefReg = ".*<[\\s]*a[\\s]*.*>(.+?)<[\\s]*/a[\\s]*>.*";
        Pattern hrefPattern = Pattern.compile(hrefReg, Pattern.CASE_INSENSITIVE);
        Matcher hrefMatcher = hrefPattern.matcher(href);
        if (hrefMatcher.matches()) {
            return hrefMatcher.group(1);
        }
        return href;
    }

    /**
     * process special char in html
     * <p>
     * <pre>
     * htmlEscapeCharsToString(null) = null;
     * htmlEscapeCharsToString("") = "";
     * htmlEscapeCharsToString("mp3") = "mp3";
     * htmlEscapeCharsToString("mp3&lt;") = "mp3<";
     * htmlEscapeCharsToString("mp3&gt;") = "mp3\>";
     * htmlEscapeCharsToString("mp3&amp;mp4") = "mp3&mp4";
     * htmlEscapeCharsToString("mp3&quot;mp4") = "mp3\"mp4";
     * htmlEscapeCharsToString("mp3&lt;&gt;&amp;&quot;mp4") = "mp3\<\>&\"mp4";
     * </pre>
     *
     * @param source
     * @return
     */
    public static String htmlEscapeCharsToString(String source) {
        return StringUtil.isEmpty(source) ? source : source.replaceAll("&lt;", "<").replaceAll("&gt;", ">")
                .replaceAll("&amp;", "&").replaceAll("&quot;", "\"");
    }

    /**
     * transform half width char to full width char
     * <p>
     * <pre>
     * fullWidthToHalfWidth(null) = null;
     * fullWidthToHalfWidth("") = "";
     * fullWidthToHalfWidth(new String(new char[] {12288})) = " ";
     * fullWidthToHalfWidth("！＂＃＄％＆) = "!\"#$%&";
     * </pre>
     *
     * @param s
     * @return
     */
    public static String fullWidthToHalfWidth(String s) {
        if (isEmpty(s)) {
            return s;
        }

        char[] source = s.toCharArray();
        for (int i = 0; i < source.length; i++) {
            if (source[i] == 12288) {
                source[i] = ' ';
                // } else if (source[i] == 12290) {
                // source[i] = '.';
            } else if (source[i] >= 65281 && source[i] <= 65374) {
                source[i] = (char) (source[i] - 65248);
            } else {
                source[i] = source[i];
            }
        }
        return new String(source);
    }

    /**
     * transform full width char to half width char
     * <p>
     * <pre>
     * halfWidthToFullWidth(null) = null;
     * halfWidthToFullWidth("") = "";
     * halfWidthToFullWidth(" ") = new String(new char[] {12288});
     * halfWidthToFullWidth("!\"#$%&) = "！＂＃＄％＆";
     * </pre>
     *
     * @param s
     * @return
     */
    public static String halfWidthToFullWidth(String s) {
        if (isEmpty(s)) {
            return s;
        }

        char[] source = s.toCharArray();
        for (int i = 0; i < source.length; i++) {
            if (source[i] == ' ') {
                source[i] = (char) 12288;
                // } else if (source[i] == '.') {
                // source[i] = (char)12290;
            } else if (source[i] >= 33 && source[i] <= 126) {
                source[i] = (char) (source[i] + 65248);
            } else {
                source[i] = source[i];
            }
        }
        return new String(source);
    }

    /**
     * <格式化String, 使用占位符>
     * <p>
     * 例如： "this is ${name}"
     * map.put("name", "zhangsan");
     * 输出：this is zhangsan
     *
     * @param template the template
     * @param values   the values
     * @return the string
     * @author: guoxiangxun
     * @date: Jun 30, 2016 7:34:58 PM
     * @version: v1.0
     */
    public static String format(final String template, final Map<String, String> values) {
        final StringBuffer sb = new StringBuffer();
        final Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}", Pattern.DOTALL);
        final Matcher matcher = pattern.matcher(template);
        while (matcher.find()) {
            final String key = matcher.group(1);
            final String replacement = values.get(key);
            if (replacement == null) {
                throw new IllegalArgumentException("Template contains unmapped key: " + key);
            }
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * <是否不为空>
     *
     * @param str the str
     * @return the boolean
     * @author: guoxiangxun
     * @date: Jul 25, 2017 4:25:59 PM
     * @version: v1.0
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * <是否为空白字符>
     *
     * @param str the str
     * @return the boolean
     * @author: guoxiangxun
     * @date: Jul 26, 2017 1:36:20 PM
     * @version: v1.0
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * !对字符串(用逗号分隔)的字符串处理
     *
     * @param str
     * @return
     */
    public static List<String> splitToListByComma(String str) {
        List<String> codes = new ArrayList<String>();
        if (isNotEmpty(str)) {
            String[] split = str.split(",");
            if (split != null && split.length > 1) {
                for (int i = 0; i < split.length; i++) {
                    codes.add(split[i].trim());
                }
            }
        }
        return codes;
    }

    /**
     * <过滤通过逗号分割的重复的数据>
     *
     * @param s the s
     * @return the string
     * @author: guoxiangxun
     * @date: Jun 16, 2016 4:33:50 PM
     * @version: v1.0
     */
    public static String filterRepeatByComma(String s) {
        Pattern p = Pattern.compile("(\\b[\\w|_]+\\b,?)(.*)\\b\\1\\b(.*)");
        Matcher matcher = p.matcher(s);

        while (matcher.find()) {
            s = matcher.replaceAll("$1$2$3");
            matcher = p.matcher(s);
        }
        if (s.endsWith(","))
            s = s.substring(0, s.length() - 1);

        return s;
    }

    /**
     * 转换sql语句like字段中的通配符
     *
     * @param keyWord
     * @return
     */
    public static String toEscapeChar(String keyWord) {
        keyWord = keyWord.replace("/", "//");
        keyWord = keyWord.replace("'", "''");
        keyWord = keyWord.replace("[", "/[");
        keyWord = keyWord.replace("]", "/]");
        keyWord = keyWord.replace("%", "/%");
        keyWord = keyWord.replace("&", "/&");
        keyWord = keyWord.replace("_", "/_");
        keyWord = keyWord.replace("(", "/(");
        keyWord = keyWord.replace(")", "/)");
        return keyWord;
    }

    /**
     * Tokenize the given {@code String} into a {@code String} array via a
     * {@link StringTokenizer}.
     * <p>Trims tokens and omits empty tokens.
     * <p>The given {@code delimiters} string can consist of any number of
     * delimiter characters. Each of those characters can be used to separate
     * tokens. A delimiter is always a single character; for multi-character
     * delimiters, consider using {@link #delimitedListToStringArray}.
     *
     * @param str        the {@code String} to tokenize
     * @param delimiters the delimiter characters, assembled as a {@code String}
     *                   (each of the characters is individually considered as a delimiter)
     * @return an array of the tokens
     * @see StringTokenizer
     * @see String#trim()
     * @see #delimitedListToStringArray
     */
    public static String[] tokenizeToStringArray(String str, String delimiters) {
        return tokenizeToStringArray(str, delimiters, true, true);
    }

    /**
     * Tokenize the given {@code String} into a {@code String} array via a
     * {@link StringTokenizer}.
     * <p>The given {@code delimiters} string can consist of any number of
     * delimiter characters. Each of those characters can be used to separate
     * tokens. A delimiter is always a single character; for multi-character
     * delimiters, consider using {@link #delimitedListToStringArray}.
     *
     * @param str               the {@code String} to tokenize
     * @param delimiters        the delimiter characters, assembled as a {@code String}
     *                          (each of the characters is individually considered as a delimiter)
     * @param trimTokens        trim the tokens via {@link String#trim()}
     * @param ignoreEmptyTokens omit empty tokens from the result array
     *                          (only applies to tokens that are empty after trimming; StringTokenizer
     *                          will not consider subsequent delimiters as token in the first place).
     * @return an array of the tokens ({@code null} if the input {@code String}
     * was {@code null})
     * @see StringTokenizer
     * @see String#trim()
     * @see #delimitedListToStringArray
     */
    public static String[] tokenizeToStringArray(
            String str, String delimiters, boolean trimTokens, boolean ignoreEmptyTokens) {

        if (str == null) {
            return null;
        }

        StringTokenizer st = new StringTokenizer(str, delimiters);
        List<String> tokens = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (trimTokens) {
                token = token.trim();
            }
            if (!ignoreEmptyTokens || token.length() > 0) {
                tokens.add(token);
            }
        }
        return toStringArray(tokens);
    }

    /**
     * Take a {@code String} that is a delimited list and convert it into a
     * {@code String} array.
     * <p>A single {@code delimiter} may consist of more than one character,
     * but it will still be considered as a single delimiter string, rather
     * than as bunch of potential delimiter characters, in contrast to
     * {@link #tokenizeToStringArray}.
     *
     * @param str       the input {@code String}
     * @param delimiter the delimiter between elements (this is a single delimiter,
     *                  rather than a bunch individual delimiter characters)
     * @return an array of the tokens in the list
     * @see #tokenizeToStringArray
     */
    public static String[] delimitedListToStringArray(String str, String delimiter) {
        return delimitedListToStringArray(str, delimiter, null);
    }

    /**
     * Take a {@code String} that is a delimited list and convert it into
     * a {@code String} array.
     * <p>A single {@code delimiter} may consist of more than one character,
     * but it will still be considered as a single delimiter string, rather
     * than as bunch of potential delimiter characters, in contrast to
     * {@link #tokenizeToStringArray}.
     *
     * @param str           the input {@code String}
     * @param delimiter     the delimiter between elements (this is a single delimiter,
     *                      rather than a bunch individual delimiter characters)
     * @param charsToDelete a set of characters to delete; useful for deleting unwanted
     *                      line breaks: e.g. "\r\n\f" will delete all new lines and line feeds in a {@code String}
     * @return an array of the tokens in the list
     * @see #tokenizeToStringArray
     */
    public static String[] delimitedListToStringArray(String str, String delimiter, String charsToDelete) {
        if (str == null) {
            return new String[0];
        }
        if (delimiter == null) {
            return new String[]{str};
        }

        List<String> result = new ArrayList<String>();
        if ("".equals(delimiter)) {
            for (int i = 0; i < str.length(); i++) {
                result.add(deleteAny(str.substring(i, i + 1), charsToDelete));
            }
        } else {
            int pos = 0;
            int delPos;
            while ((delPos = str.indexOf(delimiter, pos)) != -1) {
                result.add(deleteAny(str.substring(pos, delPos), charsToDelete));
                pos = delPos + delimiter.length();
            }
            if (str.length() > 0 && pos <= str.length()) {
                // Add rest of String, but not in case of empty input.
                result.add(deleteAny(str.substring(pos), charsToDelete));
            }
        }
        return toStringArray(result);
    }

    /**
     * Delete any character in a given {@code String}.
     *
     * @param inString      the original {@code String}
     * @param charsToDelete a set of characters to delete.
     *                      E.g. "az\n" will delete 'a's, 'z's and new lines.
     * @return the resulting {@code String}
     */
    public static String deleteAny(String inString, String charsToDelete) {
        if (!isNotEmpty(inString) || !isNotEmpty(charsToDelete)) {
            return inString;
        }

        StringBuilder sb = new StringBuilder(inString.length());
        for (int i = 0; i < inString.length(); i++) {
            char c = inString.charAt(i);
            if (charsToDelete.indexOf(c) == -1) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Copy the given {@code Collection} into a {@code String} array.
     * <p>The {@code Collection} must contain {@code String} elements only.
     *
     * @param collection the {@code Collection} to copy
     * @return the {@code String} array ({@code null} if the supplied
     * {@code Collection} was {@code null})
     */
    public static String[] toStringArray(Collection<String> collection) {
        if (collection == null) {
            return null;
        }

        return collection.toArray(new String[collection.size()]);
    }


    static final String IPV4_REGX = "^(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])$";
    static final Pattern IPV4_PATTERN = Pattern.compile(IPV4_REGX);

    /**
     * <是否为合法IP>
     *
     * @param host the host
     * @return the boolean
     * @author: guoxiangxun
     * @date: Sep 14, 2017 11:52:32 AM
     * @version: v1.0
     */
    public static boolean isIPV4(String host) {
        if (isEmpty(host)) {
            return false;
        }

        /**
         * 判断IP格式和范围
         */
        Matcher mat = IPV4_PATTERN.matcher(host);
        boolean rt = mat.find();

        return rt;
    }

    /**
     * <是否为合法端口>
     *
     * @param port the port
     * @return the boolean
     * @author: guoxiangxun
     * @date: Sep 14, 2017 11:59:42 AM
     * @version: v1.0
     */
    public static boolean isPort(String port) {
        if (isEmpty(port)) {
            return false;
        }
        try {
            int p = Integer.parseInt(port);
            if (p > 0 && p <= 65535) {
                return true;
            }

        } catch (NumberFormatException e) {
        }

        return false;
    }

    /**
     * 插入String
     *
     * @param original
     * @return
     */
    public static String insertThumbFromLastDot(String original) {
        return insertFromLastDot(original, THUMB_STR);
    }


    /**
     * 插入String
     *
     * @param original
     * @param insertStr
     * @return
     */
    public static String insertFromLastDot(String original, String insertStr) {
        if (isEmpty(original)) return original;

        StringBuilder sb = new StringBuilder(original);
        int lastIndexOf = sb.lastIndexOf(".");
        if (lastIndexOf != -1) {
            sb.insert(lastIndexOf, insertStr);
        } else {
            //append directly
            sb.append(insertStr);
        }
        return sb.toString();
    }


    /**
     * 移除_thumb字符串
     */
    public static String removeThumbFromLastDot(String original) {
        return removeFromLastDot(original, THUMB_STR);
    }

    /**
     * 移除字符串
     */
    public static String removeFromLastDot(String original, String insertStr) {
        if (StringUtil.isEmpty(original)) {
            return original;
        } else {
            StringBuilder sb = new StringBuilder(original);
            int lastThumbIndexOf = sb.lastIndexOf(THUMB_STR);
            if (lastThumbIndexOf != -1) {
                sb.replace(lastThumbIndexOf, lastThumbIndexOf + THUMB_STR.length(), "");
            }
            return sb.toString();
        }
    }

    /**
     * 生成uuid字符串
     *
     * @return
     */
    public static String getUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 删除至开始字符(如果某个字符串以某个开始)
     *
     * @param str
     * @param startWith
     * @return
     */
    public static String deleteStartWith(String str, String startWith) {

        if (StringUtil.isEmpty(str) || StringUtil.isEmpty(startWith)) {
            return str;
        }

        if (str.startsWith(startWith)) {
            return str.substring(startWith.length());
        }

        return str;
    }


    /**
     * 判断是否以某个字符结束，不区分大小写
     *
     * @param str
     * @param suffix
     * @return
     */
    public static boolean endWithIgnoreCase(String str, String suffix) {

        if (StringUtil.isEmpty(str) || StringUtil.isEmpty(suffix)) {
            return false;
        }

        return str.toUpperCase().endsWith(suffix.toUpperCase());
    }


    /**
     * 判断是否以某个字符开始，不区分大小写
     *
     * @param str
     * @param prefix
     * @return
     */
    public static boolean startWithIgnoreCase(String str, String prefix) {

        if (StringUtil.isEmpty(str) || StringUtil.isEmpty(prefix)) {
            return false;
        }

        return str.toUpperCase().startsWith(prefix.toUpperCase());
    }

    /**
     * 截取错误信息
     */
    public static String getErrMsg(String errMsg) {
        String restultStr = "";
        if (errMsg != null && errMsg.contains("errorMsg")) {
            restultStr = errMsg.split("errorMsg = ")[1];
        }
        return restultStr;

    }
}
