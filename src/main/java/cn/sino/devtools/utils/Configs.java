package cn.sino.devtools.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author Sirhao
 * @className: cn.sino.devtools.utils.Configs
 * @description: 配置
 * @date 2021/10/22 2:23 下午
 */
public class Configs {
    private static final Logger LOG = LoggerFactory.getLogger(Configs.class);

    static Properties props = new Properties();

    static final String CONFIG_FILE_KEY = "devToolsConf";

    static final String CONFIG_FILE_NAME = "dev-tools-agent.properties";

    private static final String CONFIG_FILE_DEFAULT_PATH = "/opt/tmp/" + CONFIG_FILE_NAME;

    static {
        try {
            props.load(Configs.class.getResourceAsStream("/" + CONFIG_FILE_NAME));
            loadUserPropertiesIfNecessary(true);
        } catch (IOException e) {
            LOG.error("Configs load config file error", e);
        }
    }
    public static boolean enableInitCloudStream() {
        return Configs.getBoolean("cloud.stream.bindingLifecycle.init.async.enable", false);
    }
    public static List<String> getInitAsyncHoldersPriorityList() {
        return Configs.getList("cloud.stream.bindingLifecycle.init.async.holders.priority");
    }


    private static Long lastLoadTime;

    private static void userConfigFileChanged() {
        if (lastLoadTime == null || System.currentTimeMillis() - lastLoadTime > 20) {
            LOG.info("userConfigFileChanged...");
            lastLoadTime = System.currentTimeMillis();
            loadUserPropertiesIfNecessary(false);
        }
    }

    private static void loadUserPropertiesIfNecessary(boolean addWatch) {
        String configFile = System.getProperty(CONFIG_FILE_KEY);
        if (configFile == null || configFile.length() == 0) {
            configFile = CONFIG_FILE_DEFAULT_PATH;
        }
        File userConfigFile = new File(configFile);
        checkAndInitUserConfFile(userConfigFile);
        if (!userConfigFile.isFile() || !userConfigFile.canRead()) {
            return;
        }
        props = new Properties(props);
        try (FileInputStream in = new FileInputStream(configFile)) {
            props.load(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (addWatch) {
            new Thread(() -> watchUserConfigFile(userConfigFile), "watch-agent-config-file").start();
            LOG.info("★★★ use config file: {}", userConfigFile.getAbsolutePath());
        }
    }

    private static void checkAndInitUserConfFile(File userConfigFile) {
        if (!userConfigFile.exists()) {
            boolean parentExists = userConfigFile.getParentFile().exists();
            if (!parentExists) {
                File parentFile = userConfigFile.getParentFile();
                boolean createParentDir = parentFile.mkdir();
                if (!createParentDir) {
                    LOG.warn("can't create /tmp dir...");
                }
            }
            try (
                    InputStream inputStream = Configs.class.getResourceAsStream("/" + CONFIG_FILE_NAME);
                    FileOutputStream outputStream = new FileOutputStream(userConfigFile, false);
            ) {
                byte[] buffers = new byte[1024];
                int len = -1;
                while ((len = inputStream.read(buffers)) != -1) {
                    outputStream.write(buffers, 0, len);
                }
                LOG.info("★★★ write default config to file: {}", userConfigFile.getAbsolutePath());
                return;
            } catch (Exception e) {
                LOG.error("★★★ can't create config file: {}", userConfigFile.getAbsolutePath(), e);
                return;
            }
        }
    }

    private static void watchUserConfigFile(File userConfigFile) {
        try {
            String watchDir = userConfigFile.getParent();
            String fileName = userConfigFile.getName();

            WatchService watchService = FileSystems.getDefault().newWatchService();
            Path path = Paths.get(watchDir);
            path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            WatchKey watchKey;
            while ((watchKey = watchService.take()) != null) {
                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    if (fileName.equals(event.context().toString())) {
                        userConfigFileChanged();
                    }
                }
                watchKey.reset();
            }
        } catch (IOException | InterruptedException e) {
            LOG.error("★★★ watchUserConfigFile error", e);
        }
    }

    private static final String serviceMappingPrefix = "service.mappings.";

    private static final String envEurekaMappingPrefix = "eureka.server.";

    public static boolean registerWithEureka() {
        return Configs.getBoolean("registerWithEureka", false);
    }

    public static boolean rpcPrintLog() {
        return Configs.getBoolean("rpc.printLog", false);
    }

    public static Map<String, String> getServiceAndAddrMap() {
        return Configs.getPropertyByPrefix(serviceMappingPrefix)
                .stream()
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue, (o1, o2) -> o2));
    }

    public static Map<String, String> getEnvAndEurekaServerMap() {
        return Configs.getPropertyByPrefix(envEurekaMappingPrefix)
                .stream()
                //.peek(s-> System.out.printf("%s=%s\n", s.getKey(), s.getValue()))
                .collect(Collectors.toMap(e -> e.getKey().toLowerCase(), AbstractMap.SimpleEntry::getValue, (o1, o2) -> o2));
    }

    public static Set<AbstractMap.SimpleEntry<String, String>> getPropertyByPrefix(String prefix) {
        return props.entrySet().stream()
                .filter(e -> e.getKey().toString().startsWith(prefix))
                .map(e -> new AbstractMap.SimpleEntry<>(e.getKey().toString().replace(prefix, "").toLowerCase(), e.getValue().toString()))
                .collect(Collectors.toSet());
    }

    public static String getGlobalMapping() {
        return getProperty("service.globalMapping");
    }

    public static String getProperty(String key) {
        return props.getProperty(key, null);
    }

    public static String getProperty(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    public static boolean getBoolean(String key) {
        return getBoolean(key, false);
    }
    public static  List<String > getList(String key) {
        String property = props.getProperty(key, "");
       return Arrays.stream(property.split(",")).collect(Collectors.toList());

    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        Object o = props.getProperty(key);
        return o != null ? Boolean.parseBoolean(o.toString()) : defaultValue;
    }
}
