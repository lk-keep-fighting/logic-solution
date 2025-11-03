package com.aims.logic.ide.util;

import com.aims.logic.ide.configuration.LogicIdeConfig;
import com.aims.logic.ide.controller.dto.LogicClassDto;
import com.aims.logic.ide.controller.dto.LogicClassMethodDto;
import com.aims.logic.ide.controller.dto.LogicItemGroupDto;
import com.aims.logic.runtime.contract.dsl.LogicItemTreeNode;
import com.aims.logic.runtime.contract.dsl.ParamTreeNode;
import com.aims.logic.runtime.contract.dsl.ReturnTreeNode;
import com.aims.logic.runtime.contract.parser.TypeAnnotationParser;
import com.aims.logic.sdk.annotation.LogicItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Slf4j
public class LogicItemUtil {
    @Autowired
    LogicIdeConfig logicIdeConfig;

    @Value("${logic.scan-package-names}")
    private List<String> ScanPackageNames;

    private static final DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();


    public Map<String, List<LogicClassMethodDto>> readFromCode(boolean isNeedGroup) {
        List<LogicClassDto> classDtos = new ArrayList<>();
        LinkedHashMap<String, List<LogicClassMethodDto>> methodsByGroup = new LinkedHashMap<>();
        Map<String, String> groupShapeMap = new HashMap<>();
        if (!logicIdeConfig.getLogicItemGroups().isEmpty()) {
            logicIdeConfig.getLogicItemGroups().stream().sorted(Comparator.comparing(LogicItemGroupDto::getOrder)).forEach(g -> {
                var groupName = g.getName();
                methodsByGroup.put(groupName, new ArrayList<>());
                groupShapeMap.put(groupName, g.getShape());
            });
        }
        classDtos = ScanPackageNames.stream().flatMap(name -> ClassUtils.getAllClassNames(name).stream()).toList();
        classDtos.forEach(c -> {
            try {
                ClassUtils.getMethodsByAnnotation(c.getValue(), LogicItem.class)
                        .forEach(method -> {
                            var dto = new LogicClassMethodDto();
                            var anno = method.getAnnotation(LogicItem.class);
                            var shape = anno.shape();
                            LogicItemTreeNode logicItemTreeNode = new LogicItemTreeNode()
                                    .setName(anno.name())
                                    .setVersion(anno.version())
                                    .setGroup(anno.group())
                                    .setMemo(anno.memo())
                                    .setType(anno.type());
                            dto.setLogicItem(logicItemTreeNode);
                            if (shape.isEmpty()) {//如果未指定，则获取当前分组指定的形状
                                shape = groupShapeMap.get(dto.getGroup());
                            }
                            dto.setShape(shape);
                            dto.setOrder(anno.order());

                            var paramNames = discoverer.getParameterNames(method);
                            logicItemTreeNode.setMethod(method.getName(), paramNames);
                            logicItemTreeNode.setBody("return _par;");
                            logicItemTreeNode.setUrl(c.getValue());
                            var paramTypes = method.getGenericParameterTypes();
                            if (paramNames != null) {
                                var pars = IntStream.range(0, paramTypes.length)
                                        .mapToObj(i -> createParamTreeNode(paramNames[i], paramTypes[i]))
                                        .collect(Collectors.toList());
                                logicItemTreeNode.setParams(pars);
                            }
                            var returnType = createParamTreeNode("返回值", method.getGenericReturnType());
                            ReturnTreeNode returnTreeNode = new ReturnTreeNode("返回值");
                            returnTreeNode.setTypeAnnotation(returnType.getTypeAnnotation());
                            logicItemTreeNode.setReturnType(returnTreeNode);
                            if (isNeedGroup) {
                                methodsByGroup.computeIfAbsent(anno.group(), k -> new ArrayList<>());
                                methodsByGroup.get(anno.group()).add(dto);
                            } else {
                                methodsByGroup.computeIfAbsent("def", k -> new ArrayList<>());
                                methodsByGroup.get("def").add(dto);
                            }

                        });
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return methodsByGroup;
    }

    public ParamTreeNode createParamTreeNode(String paramName, Type paramType) {
        log.debug("解析参数:" + paramName + ",参数类型：" + paramType);
        ParamTreeNode p = new ParamTreeNode(paramName);
        try {
            p.setTypeAnnotation(TypeAnnotationParser.createTypeAnnotationTreeNode(null, paramType));
//        if (paramType instanceof Class<?> clazz) {//通过NotNull注解判断是否必填
//            p.setRequired(clazz.getAnnotation(NotNull.class) != null);
//        }
        } catch (Exception e) {
            log.error("参数解析错误：" + paramName + ",参数类型：" + paramType, e);
            e.printStackTrace();
        }

        return p;
    }

    /**
     * 读取所有逻辑项并输出为AI易于理解的MD文档
     */
    public String readAllLogicItemsToDoc() {
        Map<String, List<LogicClassMethodDto>> methodsByGroup = readFromCode(true);
        StringBuilder doc = new StringBuilder();
        
        doc.append("# Logic Items 逻辑组件文档\n\n");
        doc.append("本文档列出所有可用的逻辑组件，按分组组织。\n\n");
        doc.append("---\n\n");
        
        // 统计信息
        int totalItems = methodsByGroup.values().stream()
                .mapToInt(List::size)
                .sum();
        doc.append(String.format("**总计**: %d 个分组，%d 个组件\n\n", methodsByGroup.size(), totalItems));
        
        // 遍历每个分组
        for (Map.Entry<String, List<LogicClassMethodDto>> entry : methodsByGroup.entrySet()) {
            String groupName = entry.getKey();
            List<LogicClassMethodDto> methods = entry.getValue();
            
            if (methods.isEmpty()) {
                continue;
            }
            
            doc.append(String.format("## %s\n\n", groupName));
            doc.append(String.format("*分组包含 %d 个组件*\n\n", methods.size()));
            
            // 按order排序
            methods.sort(Comparator.comparing(m -> m.getOrder() == null ? "" : m.getOrder()));
            
            for (LogicClassMethodDto method : methods) {
                LogicItemTreeNode item = method.getLogicItem();
                
                // 组件标题
                doc.append(String.format("### %s\n\n", item.getName()));
                
                // 基本信息
                doc.append("**基本信息**:\n\n");
                doc.append(String.format("- **类型**: `%s`\n", item.getType()));
                doc.append(String.format("- **版本**: `%s`\n", item.getVersion()));
                doc.append(String.format("- **ID**: `%s`\n", item.getItemId()));
                doc.append(String.format("- **实现类**: `%s`\n", item.getUrl()));
                doc.append(String.format("- **方法**: `%s`\n", item.getMethod()));
                
                if (item.getMemo() != null && !item.getMemo().isEmpty()) {
                    doc.append(String.format("- **说明**: %s\n", item.getMemo()));
                }
                
                doc.append("\n");
                
                // 参数列表
                if (item.getParams() != null && !item.getParams().isEmpty()) {
                    doc.append("**参数**:\n\n");
                    doc.append("| 参数名 | 类型 | 必填 | 默认值 |\n");
                    doc.append("|--------|------|------|--------|\n");
                    
                    for (ParamTreeNode param : item.getParams()) {
                        String paramName = param.getName();
                        String typeName = param.getTypeAnnotation() != null ? 
                                param.getTypeAnnotation().getTypeName() : "Unknown";
                        String required = param.isRequired() ? "是" : "否";
                        String defaultValue = param.getDefaultValue() != null ? 
                                param.getDefaultValue() : "-";
                        
                        doc.append(String.format("| `%s` | `%s` | %s | %s |\n", 
                                paramName, typeName, required, defaultValue));
                    }
                    doc.append("\n");
                }
                
                // 返回值
                if (item.getReturnType() != null) {
                    doc.append("**返回值**:\n\n");
                    String returnTypeName = item.getReturnType().getTypeAnnotation() != null ?
                            item.getReturnType().getTypeAnnotation().getTypeName() : "void";
                    doc.append(String.format("`%s`\n\n", returnTypeName));
                }
                
                // 事务配置
                if (item.getTranScope() != null) {
                    doc.append("**事务配置**:\n\n");
                    doc.append(String.format("- 事务范围: `%s`\n", item.getTranScope()));
                    if (item.getTranGroupId() != null) {
                        doc.append(String.format("- 事务组: `%s`\n", item.getTranGroupId()));
                    }
                    doc.append("\n");
                }
                
                doc.append("---\n\n");
            }
        }
        
        return doc.toString();
    }
}
