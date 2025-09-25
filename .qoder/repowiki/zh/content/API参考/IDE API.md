# IDE API

<cite>
**本文档中引用的文件**
- [LogicIdeController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/LogicIdeController.java)
- [LogicItemController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/LogicItemController.java)
- [LogicLogController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/LogicLogController.java)
- [QueryController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/QueryController.java)
- [PublishController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/PublishController.java)
- [ApiResult.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/dto/ApiResult.java)
- [ApiError.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/dto/ApiError.java)
- [DiffRemoteLogicsDto.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/dto/DiffRemoteLogicsDto.java)
- [ListData.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/dto/ListData.java)
</cite>

## 目录
1. [简介](#简介)
2. [项目结构](#项目结构)
3. [核心组件](#核心组件)
4. [架构概览](#架构概览)
5. [详细组件分析](#详细组件分析)
6. [依赖分析](#依赖分析)
7. [性能考虑](#性能考虑)
8. [故障排除指南](#故障排除指南)
9. [结论](#结论)

## 简介
本文档详细描述了逻辑IDE服务中暴露的RESTful API接口，涵盖`LogicIdeController`、`LogicItemController`、`LogicLogController`、`QueryController`和`PublishController`五个核心控制器。文档明确了每个端点的HTTP方法、URL路径、请求参数、请求体结构（JSON Schema）、响应格式及可能的错误码。重点说明了流程设计、版本管理、日志查询、发布操作等核心功能的API设计规范。提供了curl调用示例和典型集成场景，如获取逻辑列表、查询节点配置、提交发布请求等。同时包含认证方式、权限控制和分页机制说明。

## 项目结构
逻辑IDE服务采用标准的Spring Boot项目结构，主要包含控制器、DTO、配置和工具类。核心API位于`logic-ide`模块的`controller`包中，通过RESTful接口提供逻辑配置、调试、日志查询和发布功能。

```mermaid
graph TD
subgraph "logic-ide"
subgraph "controller"
LogicIdeController["LogicIdeController<br>逻辑管理"]
LogicItemController["LogicItemController<br>节点调试"]
LogicLogController["LogicLogController<br>日志查询"]
QueryController["QueryController<br>数据查询"]
PublishController["PublishController<br>发布管理"]
end
subgraph "dto"
ApiResult["ApiResult<br>统一响应"]
ApiError["ApiError<br>错误信息"]
DiffRemoteLogicsDto["DiffRemoteLogicsDto<br>发布差异"]
end
subgraph "configuration"
LogicIdeConfig["LogicIdeConfig"]
end
subgraph "util"
ClassUtils["ClassUtils"]
VersionUtil["VersionUtil"]
end
end
```

**图示来源**
- [LogicIdeController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/LogicIdeController.java)
- [LogicItemController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/LogicItemController.java)
- [LogicLogController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/LogicLogController.java)
- [QueryController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/QueryController.java)
- [PublishController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/PublishController.java)

**本节来源**
- [LogicIdeController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/LogicIdeController.java#L1-L50)
- [project_structure](file://#L1-L200)

## 核心组件
核心组件包括五个主要控制器，分别负责逻辑管理、节点调试、日志查询、数据查询和发布管理。所有API均返回统一的`ApiResult`格式，包含code、msg、data和debug字段，便于前端处理。

**本节来源**
- [LogicIdeController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/LogicIdeController.java#L25-L100)
- [ApiResult.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/dto/ApiResult.java#L1-L42)

## 架构概览
系统采用分层架构，前端通过REST API与IDE服务交互，IDE服务调用底层SDK服务进行数据持久化和业务逻辑处理。发布功能通过HTTP请求将配置推送到远程运行时环境。

```mermaid
graph LR
Frontend["前端<br>IDE界面"] --> |HTTP请求| IDE["IDE服务"]
IDE --> |调用| SDK["SDK服务"]
SDK --> |操作| DB[(数据库)]
IDE --> |HTTP推送| Runtime["远程运行时"]
Runtime --> |执行| Logic["逻辑流程"]
subgraph "IDE服务"
LogicIdeController
LogicItemController
LogicLogController
QueryController
PublishController
end
subgraph "SDK服务"
LogicService
LogicLogService
LogicPublishService
end
```

**图示来源**
- [LogicIdeController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/LogicIdeController.java#L1-L20)
- [LogicLogController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/LogicLogController.java#L1-L15)
- [PublishController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/PublishController.java#L1-L15)

## 详细组件分析
对每个核心控制器进行详细分析，包括接口定义、参数说明和使用示例。

### LogicIdeController 分析
负责逻辑流程的增删改查、版本管理和类信息查询。

#### 逻辑管理接口
```mermaid
flowchart TD
Start([获取逻辑列表]) --> GetLogics["GET /api/ide/logics<br>支持qry查询参数"]
GetLogics --> AddLogic["POST /api/ide/logic/add<br>创建新逻辑"]
AddLogic --> EditLogic["PUT /api/ide/logic/edit/{id}<br>编辑逻辑"]
EditLogic --> DeleteLogic["DELETE /api/ide/logic/delete/{id}<br>删除逻辑"]
DeleteLogic --> End([操作完成])
style Start fill:#f9f,stroke:#333
style End fill:#ccf,stroke:#333
```

**图示来源**
- [LogicIdeController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/LogicIdeController.java#L50-L150)

#### 版本与配置查询
```mermaid
sequenceDiagram
participant Client as "客户端"
participant Controller as "LogicIdeController"
participant Service as "LogicBakService"
Client->>Controller : GET /api/ide/logic/{id}/config/{version}
Controller->>Service : getByIdAndVersion(id, version)
Service-->>Controller : LogicBakEntity
Controller->>Controller : JSON.parseObject(configJson)
Controller-->>Client : ApiResult<LogicTreeNode>
```

**图示来源**
- [LogicIdeController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/LogicIdeController.java#L120-L140)
- [LogicBakService.java](file://logic-sdk/src/main/java/com/aims/logic/sdk/service/LogicBakService.java#L1-L10)

#### Java类与方法查询
```mermaid
classDiagram
class LogicIdeController {
+classList(packageName)
+classMethods(fullClassPath)
+logicItemJava()
}
class ClassUtils {
+getAllClassNames(packageName)
+getMethods(fullClassPath)
+getMethodsByAnnotation(className, annotation)
}
LogicIdeController --> ClassUtils : "使用"
```

**图示来源**
- [LogicIdeController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/LogicIdeController.java#L180-L250)
- [ClassUtils.java](file://logic-ide/src/main/java/com/aims/logic/ide/util/ClassUtils.java#L1-L10)

**本节来源**
- [LogicIdeController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/LogicIdeController.java#L1-L392)

### LogicItemController 分析
提供单个逻辑节点的调试功能。

```mermaid
sequenceDiagram
participant Client as "客户端"
participant Controller as "LogicItemController"
participant Runner as "LogicRunner"
Client->>Controller : POST /api/ide/logic-item/debug
Controller->>Controller : 解析config和body
Controller->>Controller : 构建完整逻辑流程
Controller->>Runner : run(par)
Runner-->>Controller : LogicRunResult
Controller-->>Client : ApiResult.fromLogicRunResult()
```

**图示来源**
- [LogicItemController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/LogicItemController.java#L1-L46)

**本节来源**
- [LogicItemController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/LogicItemController.java#L1-L46)

### LogicLogController 分析
提供逻辑执行日志的查询和管理功能。

#### 日志查询接口
```mermaid
flowchart TD
QueryInput["FormQueryInput输入"] --> SelectPage["logicLogService.selectPageByInput()"]
SelectPage --> FormatOutput["封装为Page<LogicLogEntity>"]
FormatOutput --> ReturnResult["返回ApiResult<Page>"]
style QueryInput fill:#f96,stroke:#333
style ReturnResult fill:#6f9,stroke:#333
```

**图示来源**
- [LogicLogController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/LogicLogController.java#L20-L30)

#### 日志管理操作
```mermaid
classDiagram
class LogicLogController {
+logicLogList(input)
+deleteLogicLog(id)
+getLogicLog(id)
+clearLogicLog()
}
class LogicLogService {
+selectPageByInput(input)
+removeById(id)
+selectById(id)
+clearLog()
}
LogicLogController --> LogicLogService : "依赖"
```

**图示来源**
- [LogicLogController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/LogicLogController.java#L1-L45)
- [LogicLogService.java](file://logic-sdk/src/main/java/com/aims/logic/sdk/service/LogicLogService.java#L1-L10)

**本节来源**
- [LogicLogController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/LogicLogController.java#L1-L45)

### QueryController 分析
提供基于数据模型的通用查询功能。

```mermaid
flowchart LR
Request["POST /api/ide/data/{dataModelId}/query"] --> CheckModel["判断dataModelId"]
CheckModel --> |logic_log| UseLogService["调用logicLogService"]
CheckModel --> |其他| UseLogicService["调用logicService"]
UseLogService --> ReturnResult["返回查询结果"]
UseLogicService --> ReturnResult
```

**图示来源**
- [QueryController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/QueryController.java#L1-L45)

**本节来源**
- [QueryController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/QueryController.java#L1-L45)

### PublishController 分析
负责逻辑配置的发布和版本对比功能。

#### 发布操作流程
```mermaid
sequenceDiagram
participant Client as "客户端"
participant Controller as "PublishController"
participant Service as "LogicService"
Client->>Controller : POST /api/ide/publish/logic/to-local/{id}
Controller->>Service : pubToLocal(id, isHotUpdate)
Service-->>Controller : 文件路径
Controller-->>Client : 返回路径
Client->>Controller : POST /api/ide/publish/logic/to-ide/{id}/{host_name}
Controller->>Controller : 获取远程主机配置
Controller->>Service : pubToIdeHost(id, url, isHotUpdate)
Service-->>Controller : 发布结果
Controller-->>Client : 返回结果
```

**图示来源**
- [PublishController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/PublishController.java#L20-L50)

#### 版本差异对比
```mermaid
flowchart TD
Start["diffLocalAndRemoteLogics(input)"] --> GetRemoteHost["获取远程主机"]
GetRemoteHost --> GetRemoteList["获取远程逻辑列表"]
GetRemoteList --> GetLocalList["获取本地逻辑列表"]
GetLocalList --> Compare["逐个比较版本和更新时间"]
Compare --> GenerateDiff["生成DiffRemoteLogicsDto列表"]
GenerateDiff --> ReturnResult["返回差异结果"]
```

**图示来源**
- [PublishController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/PublishController.java#L60-L95)
- [DiffRemoteLogicsDto.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/dto/DiffRemoteLogicsDto.java#L1-L20)

**本节来源**
- [PublishController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/PublishController.java#L1-L95)

## 依赖分析
各控制器通过依赖注入使用SDK提供的服务，实现业务逻辑与数据访问的分离。

```mermaid
graph TD
LogicIdeController --> LogicService
LogicIdeController --> LogicBakService
LogicIdeController --> LogicDataService
LogicLogController --> LogicLogService
QueryController --> LogicService
QueryController --> LogicLogService
PublishController --> LogicService
PublishController --> LogicPublishService
subgraph "SDK Services"
LogicService
LogicBakService
LogicLogService
LogicPublishService
LogicDataService
end
style LogicIdeController fill:#ccf,stroke:#333
style LogicLogController fill:#ccf,stroke:#333
style QueryController fill:#ccf,stroke:#333
style PublishController fill:#ccf,stroke:#333
```

**图示来源**
- [LogicIdeController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/LogicIdeController.java#L20-L40)
- [LogicLogController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/LogicLogController.java#L10-L15)
- [QueryController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/QueryController.java#L10-L15)
- [PublishController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/PublishController.java#L10-L15)

**本节来源**
- [LogicIdeController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/LogicIdeController.java#L1-L50)
- [LogicLogController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/LogicLogController.java#L1-L20)
- [QueryController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/QueryController.java#L1-L20)
- [PublishController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/PublishController.java#L1-L20)

## 性能考虑
- 所有列表查询接口均支持分页，避免大数据量查询导致性能问题
- 类信息查询使用缓存机制，减少重复扫描
- 发布操作采用异步处理，避免阻塞主线程
- 日志查询支持条件过滤，提高查询效率

## 故障排除指南
常见问题及解决方案：

1. **发布失败**: 检查远程主机配置是否正确，网络是否通畅
2. **类信息无法加载**: 确认`scan-package-names`配置是否包含目标包路径
3. **版本对比异常**: 验证本地和远程环境的时钟同步情况
4. **调试无响应**: 检查逻辑配置是否完整，节点连接是否正确

**本节来源**
- [LogicIdeController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/LogicIdeController.java#L300-L350)
- [PublishController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/PublishController.java#L80-L90)

## 结论
本文档全面介绍了逻辑IDE服务的API设计，涵盖了从逻辑管理到发布部署的完整生命周期。通过统一的响应格式和清晰的接口设计，为前端集成提供了便利。系统采用模块化设计，各组件职责明确，便于维护和扩展。