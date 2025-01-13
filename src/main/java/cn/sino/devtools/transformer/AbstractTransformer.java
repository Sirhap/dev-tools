package cn.sino.devtools.transformer;

import java.lang.instrument.ClassFileTransformer;
import java.util.Set;

/**
 * @author Sirhao
 * @className: cn.sino.devtools.transformer.AbstractTransformer
 * @description: class文件转换器抽象类
 * @date 2021/10/22 11:37 上午
 */
public abstract class AbstractTransformer implements ClassFileTransformer {
    /**
     * 拦截关注的类名集合
     *
     * @return
     */
    public abstract Set<String> interestClassNames();

    /**
     * 是否启用
     */
    public boolean isEnable() {
        return true;
    }
}
