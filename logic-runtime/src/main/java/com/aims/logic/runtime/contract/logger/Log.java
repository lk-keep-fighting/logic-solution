package com.aims.logic.runtime.contract.logger;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Getter
@Setter
public class Log {
    String msgId;
    boolean success = true;
    String msg;
    Error error;
}
