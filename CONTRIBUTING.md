# 贡献指南

感谢您对 Logic IDE 项目的兴趣！我们非常欢迎社区的贡献。

## 如何贡献

### 报告 Bug

1. 检查 [现有 Issues](https://github.com/logic-ide/logic-solution/issues) 确保问题未被报告
2. 使用 Bug 报告模板创建新 Issue
3. 提供详细的重现步骤和环境信息

### 功能建议

1. 在 [GitHub Discussions](https://github.com/logic-ide/logic-solution/discussions) 中先讨论想法
2. 获得维护者确认后，创建 Feature Request Issue
3. 提供详细的需求说明和使用场景

### 代码贡献

#### 开发环境设置

```bash
# 1. Fork 项目到你的 GitHub 账户

# 2. 克隆你的 Fork
git clone https://github.com/YOUR_USERNAME/logic-solution.git
cd logic-solution

# 3. 添加上游远程仓库
git remote add upstream https://github.com/logic-ide/logic-solution.git

# 4. 安装依赖
mvn clean install

# 5. 运行测试
mvn test
```

#### 开发流程

1. **创建分支**：
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **开发代码**：
   - 遵循项目代码规范
   - 添加必要的测试用例
   - 更新相关文档

3. **本地测试**：
   ```bash
   mvn clean test
   mvn clean package
   ```

4. **提交代码**：
   ```bash
   git add .
   git commit -m "feat: add your feature description"
   ```

5. **推送分支**：
   ```bash
   git push origin feature/your-feature-name
   ```

6. **创建 Pull Request**

#### 提交信息规范

使用 [Conventional Commits](https://conventionalcommits.org/) 格式：

- `feat: 新功能`
- `fix: 修复 Bug`
- `docs: 文档更新`
- `style: 代码格式调整`
- `refactor: 代码重构`
- `test: 测试相关`
- `chore: 构建过程或辅助工具变动`

示例：
```
feat: add support for custom node types
fix: resolve NPE in LogicRunner.updateStatus
docs: update installation guide
```

## 代码规范

### Java 代码规范

1. **编码标准**：
   - 遵循 [阿里巴巴 Java 开发手册](https://github.com/alibaba/p3c)
   - 使用 4 空格缩进，不使用 Tab
   - 行长度不超过 120 字符

2. **命名规范**：
   - 类名：大驼峰 (PascalCase) - `LogicRunner`
   - 方法名：小驼峰 (camelCase) - `runByMap`
   - 常量名：全大写下划线 - `DEFAULT_TIMEOUT`
   - 包名：全小写点分隔 - `com.logic.runtime`

3. **注释规范**：
   ```java
   /**
    * 执行逻辑实例
    *
    * @param logicId 逻辑编号
    * @param params  执行参数
    * @return 执行结果
    * @author yourname
    * @since 1.0.0
    */
   public LogicRunResult runByMap(String logicId, Map<String, Object> params) {
       // 方法实现
   }
   ```

### 测试要求

1. **单元测试**：
   - 新功能必须包含单元测试
   - 测试覆盖率不低于 70%
   - 使用 JUnit 5 和 Mockito

2. **测试命名**：
   ```java
   @Test
   void shouldReturnSuccessWhenLogicExecutedCorrectly() {
       // 测试内容
   }
   ```

3. **集成测试**：
   - 重要功能需要集成测试
   - 使用 `@SpringBootTest` 注解

## Pull Request 指南

### PR 标题格式

与提交信息格式相同，例如：
- `feat: add support for async logic execution`
- `fix: resolve memory leak in cache service`

### PR 描述模板

```markdown
## 变更类型
- [ ] 新功能
- [ ] Bug 修复
- [ ] 文档更新
- [ ] 代码重构
- [ ] 其他

## 变更说明
简要描述这个 PR 解决了什么问题或添加了什么功能。

## 测试
- [ ] 已添加单元测试
- [ ] 已添加集成测试
- [ ] 所有测试都通过
- [ ] 手动测试已完成

## 检查清单
- [ ] 代码遵循项目规范
- [ ] 已更新相关文档
- [ ] 没有引入 breaking changes（如有，请在描述中说明）
- [ ] PR 标题和描述清晰准确
```

### 代码审查

1. **自检清单**：
   - [ ] 代码编译无错误
   - [ ] 所有测试通过
   - [ ] 代码格式正确
   - [ ] 没有明显的性能问题
   - [ ] 异常处理完善

2. **审查重点**：
   - 代码逻辑正确性
   - 异常边界处理
   - 性能影响
   - 安全性考虑
   - 向后兼容性

## 社区参与

### 讨论和交流

- [GitHub Discussions](https://github.com/logic-ide/logic-solution/discussions) - 功能讨论和技术交流
- [Issues](https://github.com/logic-ide/logic-solution/issues) - Bug 报告和功能请求

### 文档贡献

- 改进现有文档
- 添加使用示例
- 翻译文档到其他语言
- 录制教程视频

### 帮助其他用户

- 回答 Issues 中的问题
- 在 Discussions 中分享经验
- 协助新用户入门

## 发布流程

1. **版本规划**：遵循 [语义化版本](https://semver.org/lang/zh-CN/)
2. **发布候选**：在 `release/x.x.x` 分支进行测试
3. **正式发布**：创建 Git Tag 和 GitHub Release
4. **Maven Central**：自动发布到 Maven 中央仓库

## 联系方式

如有任何问题，可以通过以下方式联系：

- 创建 GitHub Issue
- 在 GitHub Discussions 中提问
- 发送邮件至 maintainers@logic-ide.org

感谢您的贡献！🎉
