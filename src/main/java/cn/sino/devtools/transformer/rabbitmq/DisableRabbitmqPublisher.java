//package cn.sino.devtools.transformer.rabbitmq;
//
//import cn.sino.devtools.transformer.AbstractTransformer;
//import javassist.ClassPool;
//import javassist.CtClass;
//import javassist.CtMethod;
//
//import java.lang.instrument.IllegalClassFormatException;
//import java.security.ProtectionDomain;
//import java.util.Set;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
///**
// * @author Sirhao
// * @className: cn.sino.devtools.transformer.rabbitmq.DisableRabbitmqConsumer
// * @description: 类作用描述
// * @date 2021/12/15 4:54 下午
// */
//public class DisableRabbitmqPublisher extends AbstractTransformer {
//    private String interestClassName = "org.springframework.amqp.rabbit.core.RabbitTemplate";
//
//    @Override
//    public Set<String> interestClassNames() {
//        return Stream.of(interestClassName).collect(Collectors.toSet());
//    }
//
//    @Override
//    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
//        try {
//            ClassPool pool = ClassPool.getDefault();
//            CtClass ctClass = pool.get(interestClassName);
//
//            CtMethod getLocalAddress = ctClass.getDeclaredMethod("doSend");
//            getLocalAddress.insertBefore("{" +
//                    "return ;" +
//                    "}");
//            return ctClass.toBytecode();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return classfileBuffer;
//    }
//}
