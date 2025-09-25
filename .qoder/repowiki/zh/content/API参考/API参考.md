# API参考

<cite>
**本文档中引用的文件**  
- [LogicRuntimeController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/LogicRuntimeController.java)
- [PublishController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/PublishController.java)
- [QueryController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/QueryController.java)
- [LogicApiProxyController.java](file://logic-ide-service/src/main/java/com/aims/logic/service/controller/LogicApiProxyController.java)
</cite>

## 目录
1. [简介](#简介)
2. [逻辑执行接口](#逻辑执行接口)
3. [发布接口](#发布接口)
4. [查询接口](#查询接口)
5. [代理接口](#代理接口)
6. [认证与速率限制](#认证与速率限制)

## 简介
本API参考文档全面覆盖`logic-ide`、`logic-ide-service`和`logic-runtime`模块暴露的所有RESTful接口。文档详细说明了每个端点的HTTP方法、URL路径、请求参数、请求体结构、响应格式及可能的错误码。重点阐述了`LogicRuntimeController`中逻辑执行、发布和查询相关接口的设计规范。通过提供curl示例和典型调用场景，帮助集成方快速对接系统。

**Section sources**
- [LogicRuntimeController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/LogicRuntimeController.java#L1-L20)

## 逻辑执行接口
`LogicRuntimeController`提供了核心的逻辑执行功能，支持运行、调试、重试和强制完成等操作。

### 获取运行时配置
获取指定逻辑的运行时配置。

- **HTTP方法**: GET
- **URL路径**: `/api/runtime/logic/v1/config/{id}`
- **路径参数**:
  - `id` (string): 逻辑ID
- **响应格式**:
```json
{
  "code": 200,
  "data": {
    "id": "string",
    "name": "string",
    "nodes": [...]
  }
}
```
- **错误码**:
  - 500: 内部服务器错误

**Section sources**
- [LogicRuntimeController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/LogicRuntimeController.java#L22-L27)

### 执行逻辑（API模式）
以API模式执行指定逻辑。

- **HTTP方法**: POST
- **URL路径**: `/api/runtime/logic/v1/run-api/{id}`
- **路径参数**:
  - `id` (string): 逻辑ID
- **查询参数**:
  - `debug` (boolean, 可选): 是否开启调试模式，默认为`false`
- **请求头**: 原样传递到逻辑执行环境中
- **请求体** (可选, JSON):
```json
{
  "param1": "value1",
  "param2": "value2"
}
```
- **响应格式**:
```json
{
  "code": 200,
  "data": {...},
  "debug": {...} // 当debug=true时存在
}
```
- **curl示例**:
```bash
curl -X POST "http://localhost:8080/api/runtime/logic/v1/run-api/logic123?debug=true" \
  -H "Content-Type: application/json" \
  -H "X-Custom-Header: value" \
  -d '{"input": "data"}'
```

**Section sources**
- [LogicRuntimeController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/LogicRuntimeController.java#L29-L47)

### 执行业务实例
执行指定的业务实例。

- **HTTP方法**: POST
- **URL路径**: `/api/runtime/logic/v1/run-biz/{id}/{bizId}`
- **路径参数**:
  - `id` (string): 逻辑ID
  - `bizId` (string): 业务实例ID
- **查询参数**:
  - `debug` (boolean, 可选): 是否开启调试模式
- **请求体**: 业务数据（JSON）
- **响应格式**: 同`run-api`接口

**Section sources**
- [LogicRuntimeController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/LogicRuntimeController.java#L49-L67)

### 重置业务实例
重置业务实例的下一个执行节点。

- **HTTP方法**: POST
- **URL路径**: `/api/runtime/logic/v1/resetBiz/{id}/{bizId}`
- **路径参数**:
  - `id` (string): 逻辑ID
  - `bizId` (string): 业务实例ID
- **查询参数**:
  - `debug` (boolean, 可选): 是否开启调试模式
- **请求体**:
```json
{
  "startNodeId": "node1",
  "startNodeName": "开始节点",
  "varsJson": "{\"var1\": \"value1\"}"
}
```
- **响应格式**: 标准API响应

**Section sources**
- [LogicRuntimeController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/LogicRuntimeController.java#L85-L100)

### 重试失败业务
重试执行失败的业务实例。

- **HTTP方法**: POST
- **URL路径**: `/api/runtime/logic/v1/retry-error-biz/{id}/{bizId}`
- **路径参数**:
  - `id` (string): 逻辑ID
  - `bizId` (string): 业务实例ID
- **查询参数**:
  - `debug` (boolean, 可选): 是否开启调试模式
- **响应格式**: 标准API响应

**Section sources**
- [LogicRuntimeController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/LogicRuntimeController.java#L102-L111)

### 强制完成业务
强制完成指定的业务实例。

- **HTTP方法**: POST
- **URL路径**: `/api/runtime/logic/v1/force-complete-biz/{logicId}/{bizId}`
- **路径参数**:
  - `logicId` (string): 逻辑ID
  - `bizId` (string): 业务实例ID
- **响应格式**: 标准API响应

**Section sources**
- [LogicRuntimeController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/LogicRuntimeController.java#L113-L119)

## 发布接口
`PublishController`提供了逻辑配置的发布和管理功能。

### 发布到本地文件
将逻辑配置发布到本地文件系统。

- **HTTP方法**: POST
- **URL路径**: `/api/ide/publish/logic/to-local/{id}`
- **路径参数**:
  - `id` (string): 逻辑ID
- **查询参数**:
  - `isHotUpdate` (boolean, 可选): 是否热更新
- **响应格式**:
```json
{
  "code": 200,
  "data": "/path/to/published/file.json"
}
```

**Section sources**
- [PublishController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/PublishController.java#L20-L25)

### 发布到IDE主机
将逻辑配置发布到远程IDE主机。

- **HTTP方法**: POST
- **URL路径**: `/api/ide/publish/logic/to-ide/{id}/{host_name}`
- **路径参数**:
  - `id` (string): 逻辑ID
  - `host_name` (string): 目标主机名称
- **查询参数**:
  - `isHotUpdate` (boolean, 可选): 是否热更新
- **响应格式**: 同`to-local`接口

**Section sources**
- [PublishController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/PublishController.java#L34-L48)

### 查询已发布逻辑
分页查询已发布的逻辑列表。

- **HTTP方法**: POST
- **URL路径**: `/api/ide/published/logics`
- **请求体**:
```json
{
  "current": 1,
  "size": 10,
  "sort": "updateTime",
  "order": "desc"
}
```
- **响应格式**:
```json
{
  "code": 200,
  "data": {
    "records": [...],
    "total": 100,
    "size": 10,
    "current": 1
  }
}
```

**Section sources**
- [PublishController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/PublishController.java#L50-L55)

### 比较本地与远程逻辑
比较本地与远程环境中的逻辑配置差异。

- **HTTP方法**: POST
- **URL路径**: `/api/ide/published/logics/diff`
- **请求体**:
```json
{
  "hostName": "test-env",
  "queryInput": {
    "current": 1,
    "size": 10
  }
}
```
- **响应格式**:
```json
{
  "code": 200,
  "data": [
    {
      "id": "logic1",
      "name": "逻辑1",
      "localVersion": "1.0.0",
      "remoteVersion": "1.0.1",
      "diffType": "低于远程环境"
    }
  ]
}
```

**Section sources**
- [PublishController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/PublishController.java#L57-L93)

## 查询接口
`QueryController`提供了数据查询功能。

### JSON查询
通过JSON输入进行数据查询。

- **HTTP方法**: POST
- **URL路径**: `/api/ide/data/{dataModelId}/query`
- **路径参数**:
  - `dataModelId` (string): 数据模型ID，特殊值`logic_log`用于查询逻辑日志
- **请求体**:
```json
{
  "from": {
    "mainTable": "table_name"
  },
  "select": ["field1", "field2"],
  "where": {
    "field1": "value1"
  },
  "page": {
    "current": 1,
    "size": 10
  }
}
```
- **响应格式**:
```json
{
  "code": 200,
  "data": {
    "records": [
      {"field1": "value1", "field2": "value2"}
    ],
    "total": 100
  }
}
```

**Section sources**
- [QueryController.java](file://logic-ide/src/main/java/com/aims/logic/ide/controller/QueryController.java#L16-L43)

## 代理接口
`LogicApiProxyController`提供了API代理功能，用于转发请求到远程运行时。

### API代理
代理请求到配置的远程运行时。

- **HTTP方法**: GET, POST, PUT, DELETE
- **URL路径**: `/api/ide/papi/{proxy_name}/**`
- **路径参数**:
  - `proxy_name` (string): 代理名称，对应配置中的远程运行时
- **行为**: 将请求完整转发到目标远程运行时，包括请求头、请求体和查询参数
- **响应**: 返回远程运行时的响应

**Section sources**
- [LogicApiProxyController.java](file://logic-ide-service/src/main/java/com/aims/logic/service/controller/LogicApiProxyController.java#L1-L156)

## 认证与速率限制
当前API接口未实现统一的认证机制，依赖于部署环境的安全策略。建议在生产环境中通过反向代理或API网关添加认证层。

速率限制策略未在代码中显式实现，依赖于应用服务器或外部网关的配置。建议根据实际部署环境设置适当的速率限制，防止API被滥用。