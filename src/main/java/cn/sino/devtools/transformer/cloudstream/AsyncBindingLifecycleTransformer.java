package cn.sino.devtools.transformer.cloudstream;

import cn.sino.devtools.transformer.AbstractTransformer;
import cn.sino.devtools.transformer.netflix.RegisterWithEurekaTransformer;
import cn.sino.devtools.utils.Configs;
import com.sun.source.doctree.BlockTagTree;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.Set;

/**
 * 异步加载AbstractBindingLifecycle的start 方法
 */
public class AsyncBindingLifecycleTransformer extends AbstractTransformer {
    private static final Logger LOG = LoggerFactory.getLogger(AsyncBindingLifecycleTransformer.class);

    private final static String CLIENT_CONFIG_BEAN = "org.springframework.cloud.stream.binding.AbstractBindingLifecycle";
    @Override
    public Set<String> interestClassNames() {
        return Set.of(CLIENT_CONFIG_BEAN);
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass ctClass = pool.get(CLIENT_CONFIG_BEAN);
            CtMethod declaredMethod = ctClass.getDeclaredMethod("start");
            LOG.info("是否异步加载AbstractBindingLifecycle的start方法： {}", Configs.enableInitCloudStream());
            List<String> beanList = Configs.getInitAsyncHoldersPriorityList();


            String str = "new Thread(()->{\n" +
                    " List<String> beanList = Configs.getInitAsyncHoldersPriorityList();\n"+
                    "    if (!this.running) {\n" +
                    "       if (this.context != null) {\n" +
                    "          this.bindables.putAll(context.getBeansOfType(Bindable.class));\n" +
                    "       }\n" +
                    "       List<Bindable> collect = this.bindables.values().stream().filter(item -> {\n" +
                    "          for (String s : beanList) {\n" +
                    "             return item.getOutputs().contains(s) || item.getInputs().contains(s);\n" +
                    "          }\n" +
                    "          return false;\n" +
                    "       }).collect(Collectors.toList());\n" +
                    "       collect.forEach(this::doStopWithBindable);\n" +
                    "       this.bindables.values().forEach(item->{\n" +
                    "          for (String s : beanList) {\n" +
                    "             if (item.getOutputs().contains(s) || item.getInputs().contains(s)) {\n" +
                    "                return;\n" +
                    "             }\n" +
                    "          }\n" +
                    "          doStopWithBindable(item);\n" +
                    "       });\n" +
                    "       this.running = true;\n" +
                    "    }\n" +
                    "}).start();";


            String newMethodBody =
                    "{"
                            + "new java.lang.Thread(() -> {"
                            + "     org.slf4j.LoggerFactory.getLogger("+CLIENT_CONFIG_BEAN+").info(\"执行AbstractBindingLifecycle.start方法---异步加载MQ信息-----开始\");"
                            + "        if (!this.running) {"
                            + "            if (this.context != null) {"
                            + "                this.bindables.putAll(context.getBeansOfType(Bindable.class));"
                            + "            }"
                            + "            this.bindables.values().forEach(this::doStartWithBindable);"
                            + "            this.running = true;"
                            + "        }"
                            + "      org.slf4j.LoggerFactory.getLogger("+CLIENT_CONFIG_BEAN+").info(\"执行AbstractBindingLifecycle.start方法----异步加载MQ信息----完成\");"
                            + "    }).start();"
                            + "}";
            declaredMethod.setBody(newMethodBody);
            return ctClass.toBytecode();
        } catch (Exception e) {
            LOG.error("AsyncBindingLifecycleTransformer error", e);
        }
        return classfileBuffer;
    }
}
