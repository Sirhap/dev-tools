package cn.sino.devtools.transformer.netflix;

import cn.sino.devtools.transformer.AbstractTransformer;
import cn.sino.devtools.utils.Configs;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Sirhao
 * @className: cn.sino.devtools.transformer.netflix.RegisterWithEurekaTransformer
 * @description: 是否注册到 eureka 注册中心
 * @date 2021/10/22 2:21 下午
 */
public class RegisterWithEurekaTransformer extends AbstractTransformer {
    private static final Logger LOG = LoggerFactory.getLogger(RegisterWithEurekaTransformer.class);

    private static final String CLIENT_CONFIG_BEAN = "org.springframework.cloud.netflix.eureka.EurekaClientConfigBean";

    @Override
    public Set<String> interestClassNames() {
        return Stream.of(CLIENT_CONFIG_BEAN).collect(Collectors.toSet());
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass ctClass = pool.get(CLIENT_CONFIG_BEAN);
            CtMethod declaredMethod = ctClass.getDeclaredMethod("shouldRegisterWithEureka");
            LOG.info("register to eureka server ?  {}", Configs.registerWithEureka());
            declaredMethod.setBody("{" +
                    "return " + Configs.registerWithEureka() + ";" +
                    "}");
            return ctClass.toBytecode();
        } catch (Exception e) {
            LOG.error("RegisterWithEurekaTransformer error", e);
        }
        return classfileBuffer;
    }
}
