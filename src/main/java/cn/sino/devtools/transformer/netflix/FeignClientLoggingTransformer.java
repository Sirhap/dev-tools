package cn.sino.devtools.transformer.netflix;

import cn.sino.devtools.transformer.AbstractTransformer;
import cn.sino.devtools.utils.SnippetUtils;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Sirhao
 * @className: cn.sino.devtools.transformer.netflix.FeignClientLoggingTransformer
 * @description: eureka rpc feign client形式调用，增加请求参数和返回参数的打印
 * @date 2021/10/22 11:49 上午
 */
public class FeignClientLoggingTransformer extends AbstractTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(FeignClientLoggingTransformer.class);


    private static final String FEIGN_CLIENT_CLASS_NAME = "feign.Client";

    @Override
    public Set<String> interestClassNames() {
        return Stream.of(FEIGN_CLIENT_CLASS_NAME).collect(Collectors.toSet());
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
//        try {
//            ClassPool pool = ClassPool.getDefault();
//            pool.appendClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));
//            CtClass ctClass = pool.get(FEIGN_CLIENT_CLASS_NAME);
//            CtClass[] nestedClasses = ctClass.getNestedClasses();
//            for (CtClass nestedClass : nestedClasses) {
//                String name = nestedClass.getClassFile().getName();
//                if ("feign.Client$Default".equals(name)) {
//                    nestedClass.addField(CtField.make("private static org.slf4j.Logger _log = org.slf4j.LoggerFactory.getLogger(feign.Client.Default.class);", nestedClass));
//                    CtMethod convertAndSend = nestedClass.getDeclaredMethod("convertAndSend");
//                    convertAndSend.insertBefore(SnippetUtils.getSnippetSrc("RpcRequestLog.java"));
//                    CtMethod convertResponse = nestedClass.getDeclaredMethod("convertResponse");
//                    convertResponse.insertAfter(SnippetUtils.getSnippetSrc("RpcResponseLog.java"));
//                    nestedClass.toClass(loader, protectionDomain);
//                    break;
//                }
//            }
//
//            return ctClass.toBytecode();
//        } catch (Exception e) {
//            LOG.error("FeignClientLoggingTransformer error", e);
//        }
        return classfileBuffer;

    }
}
