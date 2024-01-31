package com.aims.logic.ide.configuration;

import com.aims.logic.runtime.env.LogicAppConfig;
import com.aims.logic.runtime.util.RuntimeUtil;
import com.aims.logic.runtime.util.SpringContextUtil;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(1)
public class AfterApplicationRunner implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {
        RuntimeUtil.AppConfig = SpringContextUtil.getBean(LogicAppConfig.class);
        log.info("加载yaml配置:");
        log.info(JSON.toJSONString(RuntimeUtil.AppConfig));
    }
}
