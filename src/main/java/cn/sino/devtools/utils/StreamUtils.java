package cn.sino.devtools.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * @author Sirhao
 * @className: cn.sino.devtools.utils.StreamUtils
 * @description: 流工具类
 * @date 2021/10/22 12:04 下午
 */
public class StreamUtils {
    public static final int BUFFER_SIZE = 4096;

    public static String copyToString(InputStream in, Charset charset) throws IOException {
        if (in == null) {
            return "";
        }

        StringBuilder out = new StringBuilder();
        InputStreamReader reader = new InputStreamReader(in, charset);
        char[] buffer = new char[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = reader.read(buffer)) != -1) {
            out.append(buffer, 0, bytesRead);
        }
        return out.toString();
    }
}
