//package cn.sino.devtools.transformer.rabbitmq;
//
//import cn.sino.devtools.transformer.AbstractTransformer;
//import cn.sino.devtools.utils.Configs;
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
// * @className: cn.sino.devtools.transformer.rabbitmq.ReplaceRabbitmqRemotingAddress
// * @description: 切换rabbitmq 连接地址
// * @date 2021/12/15 4:54 下午
// */
//public class ReplaceRabbitmqRemotingAddress extends AbstractTransformer {
//
//    private String interestClassName = "com.rabbitmq.client.ConnectionFactory";
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
//            CtMethod getHost = ctClass.getDeclaredMethod("getHost");
//            String rabbitmqAddress = Configs.getProperty("rabbitmqAddress");
//            if (rabbitmqAddress != null && rabbitmqAddress.length() > 0) {
//                getHost.setBody(String.format("{return %s;}", rabbitmqAddress));
//            }
//            return ctClass.toBytecode();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return classfileBuffer;
//    }
//}
