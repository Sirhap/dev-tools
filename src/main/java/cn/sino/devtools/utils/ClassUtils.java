package cn.sino.devtools.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author Sirhao
 * @className: cn.sino.devtools.utils.ClassUtils
 * @description: class工具类
 * @date 2021/10/22 11:42 上午
 */
public class ClassUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ClassUtils.class);

    public static final String DOT = ".";
    public static final String SLASH = "/";
    public static final String EMPTY = "";

    /** Class文件扩展名 */
    private static final String CLASS_EXT = ".class";
    /** Jar文件扩展名 */
    private static final String JAR_FILE_EXT = ".jar";
    /** 在Jar中的路径jar的扩展名形式 */
    private static final String JAR_PATH_EXT = ".jar!";
    /** 当Path为文件形式时, path会加入一个表示文件的前缀 */
    private static final String PATH_FILE_PRE = "file:";
    /** 扫描Class文件时空的过滤器，表示不过滤 */
    private static final ClassFilter NULL_CLASS_FILTER = null;

    private ClassUtils() {
    }

    /**
     * 扫面改包路径下所有class文件
     *
     * @param packageName 包路径 com | com. | com.abs | com.abs.
     */
    public static Set<Class<?>> scanPackage(String packageName) {
        LOG.debug("Scan classes from package [{}]...", packageName);
        return scanPackage(packageName, NULL_CLASS_FILTER);
    }

    /**
     * 扫面包路径下满足class过滤器条件的所有class文件，</br>
     * 如果包路径为 com.abs + A.class 但是输入 abs会产生classNotFoundException</br>
     * 因为className 应该为 com.abs.A 现在却成为abs.A,此工具类对该异常进行忽略处理,有可能是一个不完善的地方，以后需要进行修改</br>
     *
     * @param packageName 包路径 com | com. | com.abs | com.abs.
     * @param classFilter class过滤器，过滤掉不需要的class
     */
    public static Set<Class<?>> scanPackage(String packageName, ClassFilter classFilter) {
        if(packageName == null || packageName.trim().length() == 0) {
            throw new NullPointerException("packageName can't be blank!");
        }
        packageName = getWellFormedPackageName(packageName);

        final Set<Class<?>> classes = new HashSet<>();
        for (String classPath : getClassPaths(packageName)) {
            // LOG.debug("Scan classpath: [{}]", classPath);
            // 填充 classes
            fillClasses(classPath, packageName, classFilter, classes);
        }

        //如果在项目的ClassPath中未找到，去系统定义的ClassPath里找
        if(classes.isEmpty()) {
            for (String classPath : getJavaClassPaths()) {
                // LOG.debug("Scan java classpath: [{}]", classPath);
                // 填充 classes
                fillClasses(new File(classPath), packageName, classFilter, classes);
            }
        }
        return classes;
    }
    /**
     * 获得ClassPath
     * @param packageName 包名称
     */
    public static Set<String> getClassPaths(String packageName){
        String packagePath = packageName.replace(DOT, SLASH);
        Enumeration<URL> resources;
        try {
            resources = Thread.currentThread().getContextClassLoader().getResources(packagePath);
        } catch (IOException e) {
            LOG.error("Error when load classPath!", e);
            return null;
        }
        Set<String> paths = new HashSet<>();
        while(resources.hasMoreElements()) {
            paths.add(resources.nextElement().getPath());
        }
        return paths;
    }

    /**
     * 获得Java ClassPath路径，不包括 jre<br>
     */
    public static String[] getJavaClassPaths() {
        return System.getProperty("java.class.path").split(System.getProperty("path.separator"));
    }

    /**
     * 当前线程的class loader
     */
    public static ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    //--------------------------------------------------------------------------------------------------- Private method start
    /**
     * 文件过滤器，过滤掉不需要的文件<br>
     * 只保留Class文件、目录和Jar
     */
    private static FileFilter fileFilter = pathname -> isClass(pathname.getName()) || pathname.isDirectory() || isJarFile(pathname);

    /**
     * 改变 com -> com. 避免在比较的时候把比如 completeTestSuite.class类扫描进去，如果没有"."
     * </br>那class里面com开头的class类也会被扫描进去,其实名称后面或前面需要一个 ".",来添加包的特征
     *
     * @param packageName
     */
    private static String getWellFormedPackageName(String packageName) {
        return packageName.lastIndexOf(DOT) != packageName.length() - 1 ? packageName + DOT : packageName;
    }

    /**
     * 填充满足条件的class 填充到 classes<br>
     * 同时会判断给定的路径是否为Jar包内的路径，如果是，则扫描此Jar包
     *
     * @param path Class文件路径或者所在目录Jar包路径
     * @param packageName 需要扫面的包名
     * @param classFilter class过滤器
     * @param classes List 集合
     */
    private static void fillClasses(String path, String packageName, ClassFilter classFilter, Set<Class<?>> classes) {
        //判定给定的路径是否为
        int index = path.lastIndexOf(JAR_PATH_EXT);
        if(index != -1) {
            path = path.substring(0, index + JAR_FILE_EXT.length());
            path = removePrefix(path, PATH_FILE_PRE);
            processJarFile(new File(path), packageName, classFilter, classes);
        }else {
            fillClasses(new File(path), packageName, classFilter, classes);
        }
    }

    public static String removePrefix(String str, String prefix) {
        if(str != null && str.startsWith(prefix)) {
            return str.substring(prefix.length());
        }
        return str;
    }

    /**
     * 填充满足条件的class 填充到 classes
     *
     * @param file Class文件或者所在目录Jar包文件
     * @param packageName 需要扫面的包名
     * @param classFilter class过滤器
     * @param classes List 集合
     */
    private static void fillClasses(File file, String packageName, ClassFilter classFilter, Set<Class<?>> classes) {
        if (file.isDirectory()) {
            processDirectory(file, packageName, classFilter, classes);
        } else if (isClassFile(file)) {
            processClassFile(file, packageName, classFilter, classes);
        } else if (isJarFile(file)) {
            processJarFile(file, packageName, classFilter, classes);
        }
    }

    /**
     * 处理如果为目录的情况,需要递归调用 fillClasses方法
     *
     * @param directory
     * @param packageName
     * @param classFilter
     * @param classes
     */
    private static void processDirectory(File directory, String packageName, ClassFilter classFilter, Set<Class<?>> classes) {
        for (File file : directory.listFiles(fileFilter)) {
            fillClasses(file, packageName, classFilter, classes);
        }
    }

    /**
     * 处理为class文件的情况,填充满足条件的class 到 classes
     *
     * @param file
     * @param packageName
     * @param classFilter
     * @param classes
     */
    private static void processClassFile(File file, String packageName, ClassFilter classFilter, Set<Class<?>> classes) {
        final String filePathWithDot = file.getAbsolutePath().replace(File.separator, DOT);
        int subIndex = -1;
        if ((subIndex = filePathWithDot.indexOf(packageName)) != -1) {
            final String className = filePathWithDot.substring(subIndex).replace(CLASS_EXT, EMPTY);
            fillClass(className, packageName, classes, classFilter);
        }
    }

    /**
     * 处理为jar文件的情况，填充满足条件的class 到 classes
     *
     * @param file
     * @param packageName
     * @param classFilter
     * @param classes
     */
    private static void processJarFile(File file, String packageName, ClassFilter classFilter, Set<Class<?>> classes) {
        try {
            for (JarEntry entry : Collections.list(new JarFile(file).entries())) {
                if (isClass(entry.getName())) {
                    final String className = entry.getName().replace(SLASH, DOT).replace(CLASS_EXT, EMPTY);
                    fillClass(className, packageName, classes, classFilter);
                }
            }
        } catch (Throwable ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    /**
     * 填充class 到 classes
     *
     * @param className
     * @param packageName
     * @param classes
     * @param classFilter
     */
    private static void fillClass(String className, String packageName, Set<Class<?>> classes, ClassFilter classFilter) {
        if (className.startsWith(packageName)) {
            try {
                final Class<?> clazz = Class.forName(className, false, Thread.currentThread().getContextClassLoader());
                if (classFilter == NULL_CLASS_FILTER || classFilter.accept(clazz)) {
                    classes.add(clazz);
                }
            } catch (Throwable ex) {
                LOG.error("", ex);
            }
        }
    }

    private static boolean isClassFile(File file) {
        return isClass(file.getName());
    }

    private static boolean isClass(String fileName) {
        return fileName.endsWith(CLASS_EXT);
    }

    private static boolean isJarFile(File file) {
        return file.getName().contains(JAR_FILE_EXT);
    }

    public interface ClassFilter {
        boolean accept(Class<?> clazz);
    }
}
