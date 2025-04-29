package com.aims.logic.ide.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DiffRemoteLogicsDto {
    private String id;
    private String name;
    private String localVersion;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime localVersionUpdateTime;
    private String remoteVersion;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime remoteVersionUpdateTime;
    private String diffType;
}
