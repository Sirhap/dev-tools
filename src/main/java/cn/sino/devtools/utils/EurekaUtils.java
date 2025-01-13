package cn.sino.devtools.utils;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static java.lang.String.format;

/**
 * @author Sirhao
 * @className: cn.sino.devtools.utils.EurekaUtils
 * @description:
 * @date 2021/10/22 2:23 下午
 */
public class EurekaUtils {
    private static final Logger LOG = LoggerFactory.getLogger(EurekaUtils.class);

    private static Map<String, Long> serverRequestTimeStampMap = new ConcurrentHashMap<>();

    private static Map<String, Map<String, String>> serverAndInstanceMap = new ConcurrentHashMap<>();

    private final static Pattern POSITIVE_INTEGER_PATTERN = Pattern.compile("^[0-9]+$");

    /**
     * 获取eureka 注册的服务
     */
    public static Map<String, String> getServiceAndInstanceMap(String addr) {
        try {
            String cacheMinutesStr = Configs.getProperty("eureka.cacheInstanceMinutes");
            int cacheMinutes = 30;
            if (cacheMinutesStr != null && POSITIVE_INTEGER_PATTERN.matcher(cacheMinutesStr).matches()) {
                cacheMinutes = Integer.parseInt(cacheMinutesStr);
            }
            Long lastRequestTime = serverRequestTimeStampMap.get(addr);
            long now = System.currentTimeMillis();
            if (lastRequestTime != null && TimeUnit.MILLISECONDS.toMinutes(now - lastRequestTime) < cacheMinutes) {
                Map<String, String> cacheMap = serverAndInstanceMap.get(addr);
                if (cacheMap != null) {
                    return cacheMap;
                }
            }
            LOG.info("to fetch instance: {}", addr);
            HttpURLConnection connection = (HttpURLConnection)
                    new URL(addr + (addr.endsWith("/") ? "" : "/") + "eureka/apps").openConnection();
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);
            connection.setRequestMethod("GET");
            connection.addRequestProperty("Accept", "application/json");
            int status = connection.getResponseCode();

            LOG.info("fetch instance, eureka server: [{}], response status: {}, cost: {}ms", addr, status, (System.currentTimeMillis() - now));

            if (status < 0) {
                throw new IOException(format("Invalid status(%s) executing %s %s", status,
                        connection.getRequestMethod(), connection.getURL()));
            }
            if (status >= 400) {
                String body = StreamUtils.copyToString(connection.getErrorStream(), StandardCharsets.UTF_8);
                throw new RuntimeException(body);
            }
            InputStream stream = connection.getInputStream();
            String body = StreamUtils.copyToString(stream, StandardCharsets.UTF_8);
            Map<String, String> serviceNameAndAddrMap = new HashMap<>();

            JsonObject json = Json.parse(body).asObject();
            JsonArray jsonArray = json.get("applications").asObject().get("application").asArray();
            for (JsonValue jsonValue : jsonArray) {
                JsonObject members = jsonValue.asObject();
                String name = members.get("name").asString();
                JsonArray instances = members.get("instance").asArray();
                if (instances.isEmpty()) {
                    continue;
                }
                JsonObject oneInstance = instances.get(0).asObject();
                String homePageUrl = oneInstance.get("homePageUrl").asString();
                serviceNameAndAddrMap.put(name.toLowerCase(), homePageUrl);
            }
            serverRequestTimeStampMap.put(addr, now);
            serverAndInstanceMap.put(addr, serviceNameAndAddrMap);
            // System.out.println(serviceNameAndAddrMap);
            return serviceNameAndAddrMap;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
