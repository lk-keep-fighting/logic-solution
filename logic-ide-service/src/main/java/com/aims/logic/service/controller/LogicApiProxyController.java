package com.aims.logic.service.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/ide/papi/")
@Slf4j
@RestController
public class LogicApiProxyController {

//    @Autowired
//    private LogicIdeConfig logicIdeConfig;

//    @GetMapping(value = "/**", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
//    public void proxy(HttpServletRequest request, HttpServletResponse response) throws IOException, URISyntaxException {
//        // String url = URLDecoder.decode(request.getRequestURL().toString(), "UTF-8");
//        var matchProxy = logicIdeConfig.getRemoteRuntimes().stream().filter(url -> request.getServletPath().startsWith("/api/ide/papi/" + url.getName() + "/")).findFirst().orElse(null);
//        if (matchProxy == null) {
//            ResponseEntity.status(HttpStatus.NOT_FOUND).body("未发现RemoteRuntimes的配置");
//        }
//        URI uri = new URI(request.getRequestURI());
//        String path = uri.getPath();
//        String query = request.getQueryString();
//        path = path.substring(request.getContextPath().length() + ("/api/ide/papi/" + matchProxy.getName()).length());
//        String target = matchProxy.getUrl() + path;
//        if (query != null && !query.equals("") && !query.equals("null")) {
//            target = target + "?" + query;
//        }
//        URI newUri = new URI(target);
//
//        // 执行代理查询
//        String methodName = request.getMethod();
//        HttpMethod httpMethod = HttpMethod.valueOf(methodName);
//        if (httpMethod == null) {
//            return;
//        }
//        ClientHttpRequest delegate = new SimpleClientHttpRequestFactory().createRequest(newUri, httpMethod);
//        Enumeration<String> headerNames = request.getHeaderNames();
//        // 设置请求头
//        while (headerNames.hasMoreElements()) {
//            String headerName = headerNames.nextElement();
//            Enumeration<String> v = request.getHeaders(headerName);
//            List<String> arr = new ArrayList<>();
//            while (v.hasMoreElements()) {
//                arr.add(v.nextElement());
//            }
//            delegate.getHeaders().addAll(headerName, arr);
//        }
//        StreamUtils.copy(request.getInputStream(), delegate.getBody());
//        // 执行远程调用
//        try (ClientHttpResponse clientHttpResponse = delegate.execute()) {
//            response.setStatus(clientHttpResponse.getStatusCode().value());
//            // 设置响应头
//            clientHttpResponse.getHeaders().forEach((key, value) -> value.forEach(it -> {
//                response.setHeader(key, it);
//            }));
//            if (clientHttpResponse.getStatusCode().isError()) {
//                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
//                response.getWriter().write(JSONObject.of("error", clientHttpResponse.getBody().toString()).toJSONString());
//            } else {
//                StreamUtils.copy(clientHttpResponse.getBody(), response.getOutputStream());
//            }
//        } catch (Exception e) {
//            log.error("api proxy error", e);
//            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
//            response.getWriter().write(e.getMessage());
//        }
//
//    }
//
//    @PostMapping(value = "/**", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
//    @PutMapping(value = "/**", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
//    @DeleteMapping(value = "/**", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
//    public ResponseEntity handleRequest(HttpServletRequest request) throws IOException, URISyntaxException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
//        String method = request.getMethod();
//        var matchProxy = logicIdeConfig.getRemoteRuntimes().stream().filter(url -> request.getServletPath().startsWith("/api/ide/papi/" + url.getName() + "/")).findFirst().orElse(null);
//        if (matchProxy == null) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("未发现RemoteRuntime配置");
//        }
//        String path = getPath(request);
//        path = path.substring(request.getContextPath().length() + ("/api/ide/papi/" + matchProxy.getName()).length());
//        URI targetUri = new URI(matchProxy.getUrl() + path);
//        System.out.println(targetUri);
//        HttpHeaders headers = getRequestHeaders(request);
//        HttpEntity<?> entity = new HttpEntity<>(headers);
//        log.info("api proxy: " + request.getMethod() + " " + targetUri);
//        RestTemplate restTemplate = new RestTemplate(getSecureHttpRequestFactory());
//        if (method.equalsIgnoreCase(HttpMethod.GET.name()) || method.equalsIgnoreCase(HttpMethod.DELETE.name())) {
//            return restTemplate.exchange(targetUri, HttpMethod.valueOf(method), entity, JSONObject.class);
//        } else {
//            String requestBody = getRequestBody(request);
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            HttpEntity<String> postEntity = new HttpEntity<>(requestBody, headers);
//            ResponseEntity res;
//            try {
//                res = restTemplate.exchange(targetUri, HttpMethod.valueOf(method), postEntity, JSONObject.class);
//            } catch (Exception e) {
//                res = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(JSONObject.of("error", e.getMessage()));
//            }
//            return res;
//        }
//    }
//
//    private String getPath(HttpServletRequest request) {
//        String contextPath = request.getContextPath();
//        String servletPath = request.getServletPath();
//        String pathInfo = request.getPathInfo() != null ? request.getPathInfo() : "";
//        return contextPath + servletPath + pathInfo;
//    }
//
//    private HttpHeaders getRequestHeaders(HttpServletRequest request) {
//        HttpHeaders headers = new HttpHeaders();
//        Enumeration<String> headerNames = request.getHeaderNames();
//        while (headerNames.hasMoreElements()) {
//            String headerName = headerNames.nextElement();
//            List<String> headerValues = Collections.list(request.getHeaders(headerName));
//            headers.put(headerName, headerValues);
//        }
//        return headers;
//    }
//
//    private String getRequestBody(HttpServletRequest request) throws IOException {
//        return request.getReader().lines().reduce("", (accumulator, actual) -> accumulator + actual);
//    }
//
//    private HttpComponentsClientHttpRequestFactory getSecureHttpRequestFactory() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
//        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
//            public X509Certificate[] getAcceptedIssuers() {
//                return null;
//            }
//
//            public void checkClientTrusted(X509Certificate[] certs, String authType) {
//            }
//
//            public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
//            }
//        }};
//
//        SSLContext sslContext = SSLContext.getInstance("TLS");
//        sslContext.init(null, trustAllCerts, null);
//
//        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
//
//        // 创建 HttpClient 并设置连接池和超时
//        var httpClient = HttpClients.custom()
//                .setSSLSocketFactory(csf)
//                .build();
//        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
//        requestFactory.setHttpClient(httpClient);
//        requestFactory.setConnectionRequestTimeout(10000);
//        requestFactory.setConnectTimeout(10000);
//        requestFactory.setReadTimeout(10000);
//        return requestFactory;
//    }

}