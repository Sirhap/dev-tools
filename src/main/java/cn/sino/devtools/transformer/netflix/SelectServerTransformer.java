package cn.sino.devtools.transformer.netflix;

import cn.sino.devtools.transformer.AbstractTransformer;
import cn.sino.devtools.utils.SnippetUtils;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.ProtectionDomain;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Sirhao
 * @className: cn.sino.devtools.transformer.netflix.SelectServerTransformer
 * @description: eureka 选择指定的服务
 * @date 2021/10/22 2:21 下午
 */
public class SelectServerTransformer extends AbstractTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(RegisterWithEurekaTransformer.class);

    private static final String LOAD_BALANCER_COMMAND_CLASS = "com.netflix.loadbalancer.reactive.LoadBalancerCommand";

    @Override
    public Set<String> interestClassNames() {
        return Stream.of(LOAD_BALANCER_COMMAND_CLASS).collect(Collectors.toSet());
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass ctClass = pool.get(LOAD_BALANCER_COMMAND_CLASS);
            loader.loadClass("cn.sino.devtools.utils.Configs");
            loader.loadClass("cn.sino.devtools.utils.EurekaUtils");

            CtClass[] nestedClasses = ctClass.getNestedClasses();

            for (CtClass nestedClass : nestedClasses) {

                Set<String> interfaces = Stream.of(nestedClass.getClassFile().getInterfaces()).collect(Collectors.toSet());
                if (!interfaces.contains("rx.Observable$OnSubscribe")) {
                    continue;
                }
                System.out.println("interfaces:" + interfaces.toString());
                System.out.println("nestedClass:" + nestedClass.toString());
//                CtField serviceAndAddrMapField = CtField.make("static java.util.Map _serviceAndAddrMap;", nestedClass);
//                nestedClass.addField(serviceAndAddrMapField);
//                nestedClass.addField(CtField.make("private static boolean _serviceAndAddMapInitialized;", nestedClass));
//                Map<String, String> serviceAndAddrMap = Conf.getServiceAndAddrMap();
                CtMethod call = nestedClass.getDeclaredMethod("call");
                LOG.info("call:{}", call);
                call.setBody(SnippetUtils.getSnippetSrc("NetflixLoadBalancerRouteAddress.java"));
                LOG.info("call:{}", call);

                nestedClass.toClass(loader, protectionDomain);
            }
            return ctClass.toBytecode();
        } catch (Exception e) {
            LOG.error("SelectServerTransformer error", e);
        }
        return classfileBuffer;
    }
}
