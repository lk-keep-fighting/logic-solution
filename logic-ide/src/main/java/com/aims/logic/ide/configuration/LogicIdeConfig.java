package com.aims.logic.ide.configuration;

import com.aims.logic.runtime.contract.dto.LogicItemGroupDto;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Getter
@Component
@ConfigurationProperties(prefix = "logic")
public class LogicIdeConfig {
    private List<LogicItemGroupDto> logicItemGroups = Collections.emptyList();
    private List<RemoteRuntimeDto> remoteRuntimes = Collections.emptyList();

    public void setLogicItemGroups(List<LogicItemGroupDto> logicItemGroups) {
        this.logicItemGroups = logicItemGroups == null ? Collections.emptyList() : logicItemGroups;
    }
    public void setRemoteRuntimes(List<RemoteRuntimeDto> remoteRuntimes) {
        this.remoteRuntimes = remoteRuntimes == null ? Collections.emptyList() : remoteRuntimes;
    }

}
