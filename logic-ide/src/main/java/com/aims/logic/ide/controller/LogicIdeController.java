package com.aims.logic.ide.controller;

import com.aims.logic.ide.configuration.LogicIdeConfig;
import com.aims.logic.ide.controller.dto.*;
import com.aims.logic.ide.util.ClassUtils;
import com.aims.logic.runtime.contract.dsl.LogicItemTreeNode;
import com.aims.logic.runtime.contract.dsl.LogicTreeNode;
import com.aims.logic.runtime.contract.dsl.ParamTreeNode;
import com.aims.logic.runtime.contract.dsl.ReturnTreeNode;
import com.aims.logic.runtime.contract.dsl.basic.TypeAnnotationTreeNode;
import com.aims.logic.runtime.contract.parser.TypeAnnotationParser;
import com.aims.logic.sdk.annotation.LogicItem;
import com.aims.logic.sdk.dto.DataFilterInput;
import com.aims.logic.sdk.dto.FormQueryInput;
import com.aims.logic.sdk.dto.Page;
import com.aims.logic.sdk.entity.LogicEntity;
import com.aims.logic.sdk.service.LogicBakService;
import com.aims.logic.sdk.service.LogicService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
@Slf4j
public class LogicIdeController {
    private final LogicService logicService;
    private final LogicBakService logicBakService;

    @Value("${logic.scan-package-names}")
    private List<String> ScanPackageNames;
    @Autowired
    LogicIdeConfig logicIdeConfig;

    @Autowired
    public LogicIdeController(
            LogicService logicService,
            LogicBakService logicBakService) {
        this.logicService = logicService;
        this.logicBakService = logicBakService;
    }


    @PostMapping("/api/ide/logic/add")
    public ApiResult<Boolean> addLogic(@RequestBody LogicEntity body) {
        try {
            logicService.insert(body);
            return new ApiResult<Boolean>().setData(true);
        } catch (Exception ex) {
            return ApiResult.fromException(ex);
        }
    }

    @PostMapping("/api/ide/logics")
    public ApiResult<Page<LogicEntity>> logicList(@RequestBody FormQueryInput input) {
        var list = logicService.selectPage(input);
        return new ApiResult<Page<LogicEntity>>().setData(list);
    }

    @GetMapping("/api/ide/logics")
    public ApiResult<Page<LogicEntity>> getLogicList(@RequestParam(required = false) String qry) {
        List<DataFilterInput> filters = new ArrayList<>();
        if (qry != null && !qry.isEmpty()) {
            filters.add(new DataFilterInput().setDataIndex("id").setValues(Collections.singletonList(qry)));
            filters.add(new DataFilterInput().setDataIndex("name").setValues(Collections.singletonList(qry)));
        }
        var input = new FormQueryInput().setFilters(filters).setPageSize(1000);
        var list = logicService.selectPage(input);
        return new ApiResult<Page<LogicEntity>>().setData(list);
    }

    @PutMapping("/api/ide/logic/edit/{id}")
    public ApiResult<Integer> editLogic(@PathVariable String id, @RequestBody LogicEntity body) {
        var res = logicService.editAndBak(id, body);
        return new ApiResult<Integer>().setData(res);
    }

    @DeleteMapping("/api/ide/logic/delete/{id}")
    public ApiResult deleteLogic(@PathVariable String id) {
        var res = logicService.removeById(id);
        return new ApiResult<Integer>().setData(res);
    }

    @GetMapping("/api/ide/logic/{id}")
    public ApiResult<LogicEntity> getLogic(@PathVariable String id) {
        var logicEntity = logicService.selectById(id);
        return new ApiResult<LogicEntity>().setData(logicEntity);
    }

    @GetMapping("/api/ide/modules")
    public ApiResult<ListData> getModules() {
        var modules = logicService.getModuleList();
        return new ApiResult<ListData>().setData(new ListData(modules));
    }

    @GetMapping("/api/ide/logic/{id}/config")
    public ApiResult<LogicTreeNode> getLogicConfig(@PathVariable String id) {
        var logicEntity = logicService.selectById(id);
        if (logicEntity != null) {
            var config = logicEntity.getConfigJson();
            var res = JSON.isValid(config) ? JSON.parseObject(config, LogicTreeNode.class) : null;
            return new ApiResult<LogicTreeNode>().setData(res);
        }
        return new ApiResult<LogicTreeNode>();
    }

    @GetMapping("/api/ide/logic/{id}/config/{version}")
    public ApiResult<LogicTreeNode> getLogicConfigByVersion(@PathVariable String id, @PathVariable String version) {
        var logicBakEntityEntity = logicBakService.getByIdAndVersion(id, version);
        if (logicBakEntityEntity != null) {
            var config = logicBakEntityEntity.getConfigJson();
            var res = JSON.isValid(config) ? JSON.parseObject(config, LogicTreeNode.class) : null;
            return new ApiResult<LogicTreeNode>().setData(res);
        }
        int[] n = {1, 2, 3};
        return new ApiResult<>();
    }

    @GetMapping("/api/ide/asset/v1/java/classes/{packageName}")
    public ApiResult classList(@PathVariable String packageName) {
        var res = ClassUtils.getAllClassNames(packageName);
        return new ApiResult().setData(res);
    }

    @GetMapping("/api/ide/asset/v1/java/classes")
    public ApiResult curPackageClassList() {
//        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
//        var res = classLoader.getDefinedPackages();
        log.info("扫描指定package:{}下的类", ScanPackageNames);
        List<LogicClassDto> classDtos = new ArrayList<>();
        for (String name : ScanPackageNames) {
            var res = ClassUtils.getAllClassNames(name);
            classDtos.addAll(res);
        }

        return new ApiResult().setData(classDtos);
    }

    private static final DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();

    @GetMapping("/api/ide/asset/v1/java/class/{fullClassPath}/methods")
    public ApiResult<List<LogicClassMethodDto>> classMethods(@PathVariable String fullClassPath) throws Exception {
        List<LogicClassMethodDto> methodDtos = ClassUtils.getMethods(fullClassPath).stream()
                .map(m -> {
//                    var m = mdto.getMethod();
                    log.info("开始解析方法:{}", m.getName());
                    var dto = new LogicClassMethodDto().setName(m.getName());
                    var paramNames = discoverer.getParameterNames(m);
                    var paramTypes = m.getGenericParameterTypes();
                    if (paramNames != null) {
                        var pars = IntStream.range(0, paramTypes.length)
                                .mapToObj(i -> createParamTreeNode(paramNames[i], paramTypes[i]))
                                .collect(Collectors.toList());
                        dto.setParameters(pars);
                    }
                    var returnType = createParamTreeNode("返回值", m.getGenericReturnType());
                    ReturnTreeNode returnTreeNode = new ReturnTreeNode("返回值");
                    returnTreeNode.setTypeAnnotation(returnType.getTypeAnnotation());
                    dto.setReturnType(returnTreeNode);
//                    if (mdto.getSourceCodeDto() != null) {
//                        mdto.getSourceCodeDto().readGitInfo();
//                    }
//                    dto.setCodeInfo(mdto.getSourceCodeDto());
                    return dto;
                })
                .collect(Collectors.toList());

        return new ApiResult<List<LogicClassMethodDto>>().setData(methodDtos);
    }

    @GetMapping("/api/ide/asset/v1/logic-item/readFromCode")
    public ApiResult<Map<String, List<LogicClassMethodDto>>> logicItemJava() throws ClassNotFoundException {
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
        for (String name : ScanPackageNames) {
            var res = ClassUtils.getAllClassNames(name);
            classDtos.addAll(res);
        }
        classDtos.forEach(c -> {
            try {
                ClassUtils.getMethodsByAnnotation(c.getValue(), LogicItem.class)
                        .forEach(method -> {
                            var dto = new LogicClassMethodDto().setName(method.getName());
                            var anno = method.getAnnotation(LogicItem.class);
                            dto.setName(anno.name());
                            dto.setGroup(anno.group());
                            var shape = anno.shape();
                            if (shape.isEmpty()) {//如果未指定，则获取当前分组指定的形状
                                shape = groupShapeMap.get(dto.getGroup());
                            }
                            dto.setShape(shape);
                            dto.setOrder(anno.order());
                            LogicItemTreeNode logicItemTreeNode = new LogicItemTreeNode()
                                    .setName(anno.name())
                                    .setMemo(anno.memo())
                                    .setType(anno.type());
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
                            dto.setLogicItem(logicItemTreeNode);
                            methodsByGroup.computeIfAbsent(anno.group(), k -> new ArrayList<>());
                            methodsByGroup.get(anno.group()).add(dto);
                        });
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        return new ApiResult<Map<String, List<LogicClassMethodDto>>>().setData(methodsByGroup);
    }

    @PostMapping("/api/ide/remote-runtimes")
    public ApiResult remoteRuntimeList() {
        return new ApiResult().setData(logicIdeConfig.getRemoteRuntimes());
    }

    private ParamTreeNode createParamTreeNode(String paramName, Type paramType) {
        log.debug("解析参数:" + paramName + ",参数类型：" + paramType);
        ParamTreeNode p = new ParamTreeNode(paramName)
                .setTypeAnnotation(TypeAnnotationParser.createTypeAnnotationTreeNode(paramType));
//        if (paramType instanceof Class<?> clazz) {//通过NotNull注解判断是否必填
//            p.setRequired(clazz.getAnnotation(NotNull.class) != null);
//        }
        return p;
    }

    @PostMapping("/api/ide/asset/v1/java/class/{fullClassPath}/method/{methodName}/params")
    public ApiResult<List<ParamTreeNode>> getMethodParams(@RequestBody TypeAnnotationTreeNode[]
                                                                  typeParames, @PathVariable String fullClassPath, @PathVariable String methodName) throws
            ClassNotFoundException, NoSuchMethodException {
        ApiResult<List<ParamTreeNode>> res = new ApiResult<>();
        if (typeParames != null) {
            var typeArrays = Arrays.stream(typeParames).map(p -> p.getTypeName()).toList();
            var method = ClassUtils.getDeclaredMethod(fullClassPath, methodName, typeArrays);
            List<ParamTreeNode> paramTreeNodes = new ArrayList<>();
            var paramNames = discoverer.getParameterNames(method);
            if (paramNames != null) {
                for (int i = 0; i < paramNames.length; i++) {
                    paramTreeNodes.add(new ParamTreeNode(paramNames[i]).setTypeAnnotation(typeParames[i]));
                }
            }
            res.setData(paramTreeNodes);
        }
        return res;
    }


    @PostMapping("/api/ide/asset/v1/java/sourceCode")
    public ApiResult<MethodSourceCodeDto> getMethodSourceCode(@RequestBody JSONObject body) throws
            ClassNotFoundException {
        ApiResult<MethodSourceCodeDto> res = new ApiResult<>();
        LogicItemTreeNode item = body.to(LogicItemTreeNode.class);
        String methodName = item.getMethod().split("\\(")[0];
        var method = ClassUtils.getMethodSourceCode(item.getUrl(), methodName, item.getParams());
        res.setData(method);
        return res;
    }


//    @PostMapping("/api/ide/settings/asset/{type}/{code}")
//    public ApiResult setAsset(@PathVariable String type, @PathVariable String code, @RequestBody String config) {
//        LogicAssetEntity logicAsset = new LogicAssetEntity();
//        logicAsset.setId(type + '-' + code);
//        logicAsset.setType(type);
//        logicAsset.setCode(code);
//        logicAsset.setConfig(config);
//        return new ApiResult().setData(logicAsset.insertOrUpdate());
//    }
//
//    @GetMapping("/api/ide/settings/asset/{type}/{code}")
//    public ApiResult<LogicAssetEntity> getAsset(@PathVariable String type, @PathVariable String code) {
//        Map<String, String> map = new HashMap<>();
//        map.put("type", type);
//        map.put("code", code);
//        var res = logicAssetService.query().allEq(map).oneOpt();
//        return new ApiResult().setData(res.orElse(null));
//    }
//
//    public List<LogicClassDto> getAllClassNames_bak(String packageName) {
//        String packagePath = packageName.replace(".", "/");
//        List<LogicClassDto> classNames = new ArrayList<>();
//        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
//        var res = classLoader.getResource(packagePath);
//        if (res == null) return classNames;
//        File packageDir = new File(res.getFile());
//        if (packageDir.exists() && packageDir.isDirectory()) {
//            File[] files = packageDir.listFiles();
//            for (File file : files) {
//                String fileName = file.getName();
//                if (file.isFile() && fileName.endsWith(".class")) {
//                    String className = packageName + "." + fileName.substring(0, fileName.lastIndexOf(".class"));
//                    classNames.add(new LogicClassDto(className));
//                } else if (file.isDirectory()) {
//                    String subPackageName = packageName + "." + fileName;
//                    List<LogicClassDto> subClassNames = ClassUtils.getAllClassNames(subPackageName);
//                    classNames.addAll(subClassNames);
//                }
//            }
//        }
//        return classNames;
//    }
//

}
