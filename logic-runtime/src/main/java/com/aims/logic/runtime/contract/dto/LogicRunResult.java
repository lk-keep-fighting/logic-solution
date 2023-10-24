package com.aims.logic.runtime.contract.dto;

import com.aims.logic.runtime.contract.log.LogicLog;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class LogicRunResult {
    public LogicRunResult(){
    }
    boolean success=true;
    String msg;
    Object data;
    LogicLog logicLog;
}
