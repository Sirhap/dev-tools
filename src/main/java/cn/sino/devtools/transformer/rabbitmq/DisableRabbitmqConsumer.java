package cn.sino.devtools.transformer.rabbitmq;

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
 * @className: cn.sino.devtools.transformer.rabbitmq.DisableRabbitmqConsumer
 * @description: sino专用 - 关闭rabbitmq消费
 * @date 2021/12/15 4:54 下午
 */
@Deprecated
public class DisableRabbitmqConsumer extends AbstractTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(DisableRabbitmqConsumer.class);

    private String interestClassName = "cn.sino.common.message.MqMessageConsumeCommon";

    @Override
    public Set<String> interestClassNames() {
        return Stream.of(interestClassName).collect(Collectors.toSet());
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass ctClass = pool.get(interestClassName);
            CtMethod getLocalAddress = ctClass.getDeclaredMethod("consume");
            boolean disableRabbitmqConsumer = Configs.getBoolean("disableRabbitmqConsumer");
            LOG.info(" config DisableRabbitmqConsumer: {}", disableRabbitmqConsumer);
            if (disableRabbitmqConsumer) {
                LOG.warn("dev-tools-agent disabled rabbit mq Consumer, to avoid local consume message.");
                getLocalAddress.setBody("{}");
            }
            return ctClass.toBytecode();
        } catch (Exception e) {
            LOG.error("DisableRabbitmqConsumer error", e);
        }
        return classfileBuffer;
    }
}
