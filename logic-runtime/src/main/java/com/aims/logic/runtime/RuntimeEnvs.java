package com.aims.logic.runtime;

import com.aims.logic.contract.enums.EnvEnum;
import com.aims.logic.contract.enums.LogicConfigModelEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RuntimeEnvs {
    private EnvEnum ENV;
    private LogicConfigModelEnum LOGIC_CONFIG_MODEL;
    private String LOG;
    private String IDE_HOST;
    private String FORM_HOST;
    private String LOGIC_RUNTIME_HOST;
}

