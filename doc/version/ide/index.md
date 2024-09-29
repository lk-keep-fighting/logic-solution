## 0.0.23-SNAPSHOT
  Fix:
- 修复switch老版本图中，当default包含when属性时不会命中的问题；
  Feat:
- 配合其他包新增了默认在headers中追加当前请求的Authorization
## 0.0.22-SNAPSHOT
  Feat：
- 前端逻辑列表增加模块树便于筛选列表；
- 新增清空日志方法；
- 关闭日志时不产生运行日志(logic_log)，原先为不产生节点日志(logic_log.itemsLog)；
## 0.0.21-SNAPSHOT
  Fix:
- 执行日志中嵌套对象会被后面节点覆盖问题；
- 执行日志无法根据环境查询；
  Feat:
- 实例、日志支持复制编号；
  Refactor:
- 重试功能优化；
## 0.0.20-SNAPSHOT
  Fix:
- 修复多个java节点切换时当java方法不属于同一个类会造成选项丢失问题；
  Feat：
- 浏览器页签显示当前编排名称与版本；
  [图片]
- 支持抛出LogicBizException异常，此类型被判断为业务系统主动抛出给用户的异常消息，消息原样抛出，同样支持事务特性；
## 0.0.19-SNAPSHOT
  Feat:
- 支持选中区域自动布局；
- 支持switch、switch-case、switch-default、js脚本与java节点共享事务组；
## 0.0.18-SNAPSHOT
  Feat:
- java节点支持配置事务组，实现相邻java节点共享一个事务；
- 运行日志支持可视化查看；
  Fix:
- 复用逻辑内部方法抛出异常的嵌套事务问题；
  Perf：
- 实例回放左侧优化为请求记录，一次请求只产生一条，一次请求执行过个节点会在中间图中用顺序号标注，点击序号可查看右侧详细；