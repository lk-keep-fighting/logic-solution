package com.aims.logic.ide.controller.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * git信息
 */
@Data
@Accessors(chain = true)
public class GitInfo {
    String memo;
    String url;
    String branch;
    String commitId;
    int beginLine;
    int endLine;
}
