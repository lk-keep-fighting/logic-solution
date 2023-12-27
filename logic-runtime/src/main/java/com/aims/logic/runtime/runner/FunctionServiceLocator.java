package com.aims.logic.runtime.runner;

import com.aims.logic.runtime.runner.functions.ILogicItemFunctionRunner;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author liukun
 * 自动加载节点运行时实现类到Map中，用于执行时检索执行
 */
@Component
public class FunctionServiceLocator implements ApplicationContextAware {
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        var list = applicationContext.getBeansOfType(ILogicItemFunctionRunner.class);
        for (ILogicItemFunctionRunner f : list.values()) {
            var itemType = f.getItemType();
            Functions.functions.put(itemType, f);
        }
    }
}
