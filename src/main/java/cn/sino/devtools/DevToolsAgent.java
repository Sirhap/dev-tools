package cn.sino.devtools;

import cn.sino.devtools.transformer.MainTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;

/**
 * @author Sirhao
 * @className: cn.sino.devtools.DevToolsAgent
 * @description: 代理类
 * @date 2021/10/22 11:00
 */
public class DevToolsAgent {

    private static Logger log = LoggerFactory.getLogger(DevToolsAgent.class);

    public static void premain(String options, Instrumentation ins) {
        ins.addTransformer(new MainTransformer());
    }
}
