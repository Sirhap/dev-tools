package cn.sino.devtools.transformer;

import cn.sino.devtools.utils.ClassUtils;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Sirhao
 * @className: cn.sino.devtools.transformer.MainTransformer
 * @description: class文件主转换器
 * @date 2021/10/22 11:35 上午
 */
public class MainTransformer implements ClassFileTransformer {

    static Map<String, AbstractTransformer> classNameAndTransformer = new HashMap<>();

    static Set<String> transClassNames;

    static {
        Set<Class<?>> transformers = ClassUtils.scanPackage("cn.sino.devtools.transformer", clazz ->
                AbstractTransformer.class.isAssignableFrom(clazz) && !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers()));
        for (Class<?> transformerClass : transformers) {
            instantiationAndAddTransformer(transformerClass);
        }
        transClassNames = classNameAndTransformer.keySet();
    }

    private static void instantiationAndAddTransformer(Class<?> transformerClass) {
        try {
            AbstractTransformer transformer = (AbstractTransformer) transformerClass.newInstance();
            if (transformer.isEnable()) {
                addTransformerToMap(transformer);
            }
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static void addTransformerToMap(AbstractTransformer transformer) {
        for (String supportClassName : transformer.interestClassNames()) {
            classNameAndTransformer.put(supportClassName.replace(".", "/"), transformer);
        }
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (!transClassNames.contains(className)) {
            return classfileBuffer;
        }
        return classNameAndTransformer.get(className).transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
    }
}
