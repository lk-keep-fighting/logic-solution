package com.aims.logic.ide.controller.dto;

import lombok.Data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Data
public class MethodSourceCodeDto {
    public MethodSourceCodeDto() {
    }

    public MethodSourceCodeDto(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public MethodSourceCodeDto(String sourceFilePath, String sourceCode, int beginLine, int endLine) {
        this.filePath = sourceFilePath;
        this.sourceCode = sourceCode;
        this.beginLine = beginLine;
        this.endLine = endLine;
    }

    String filePath;
    String sourceCode;
    int beginLine;
    int endLine;
    GitInfo gitInfo = new GitInfo();

    /**
     * 通过指定的文件路径和行号，利用git命令行工具读取git信息
     *
     * @return GitInfo 对象，包含git信息
     */
    public GitInfo readGitInfo() {
        if (sourceCode != null && beginLine > 0) {
            ProcessBuilder processBuilder = new ProcessBuilder("git", "blame", "-L", beginLine + "," + endLine, "--", filePath);
            processBuilder.redirectErrorStream(true);

            try {
                Process process = processBuilder.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder gitInfoBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    gitInfoBuilder.append(line).append("\n");
                    if (gitInfoBuilder.length() > 500) break;
                }
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    gitInfo.setMemo(gitInfoBuilder.toString());
                } else {
                    throw new RuntimeException("Failed to read git info. Exit code: " + exitCode);
                }
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException("Error reading git info", e);
            }
        }
        return gitInfo;
    }


}
