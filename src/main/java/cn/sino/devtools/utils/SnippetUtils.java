package cn.sino.devtools.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author Sirhao
 * @className: cn.sino.devtools.utils.SnippetsUtils
 * @description: Snippet工具类
 * @date 2021/10/22 11:42 上午
 */
public class SnippetUtils {

    public static String getSnippetSrc(String id) throws IOException {
        try (InputStream in = SnippetUtils.class.getResourceAsStream("/snippets/" + id)) {
            return StreamUtils.copyToString(in, StandardCharsets.UTF_8);
        }
    }
}
