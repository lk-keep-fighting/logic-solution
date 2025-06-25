package com.aims.logic.ide.controller;

import com.aims.logic.sdk.service.LogicLogService;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@RestController
public class LogicAiController {
    @Autowired
    LogicLogService logicLogService;
    @Autowired
    private RestTemplate restTemplate;

    private final WebClient webClient = WebClient.create();

    @PostMapping(value = "/api/ide/ai/chat/completions", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestBody JSONObject body) {
        String ollamaUrl = "http://192.168.44.151:11434/v1/chat/completions";
        return webClient.post()
                .uri(ollamaUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class);
    }
//    @PostMapping(value = "/api/ai/chat/completions", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public ResponseEntity<StreamingResponseBody> chat(@RequestBody String body) {
//        String ollamaUrl = "http://192.168.44.151:11434/v1/chat/completions";
//
//        WebClient webClient = WebClient.create();
//
//        return ResponseEntity.ok()
//                .contentType(MediaType.TEXT_EVENT_STREAM)
//                .body(outputStream -> {
//                    webClient.post()
//                            .uri(ollamaUrl)
//                            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
//                            .accept(MediaType.TEXT_EVENT_STREAM)
//                            .bodyValue(body)
//                            .retrieve()
//                            .bodyToFlux(String.class)
//                            .subscribe(data -> {
//                                try {
//                                    outputStream.write(data.getBytes(StandardCharsets.UTF_8));
//                                    outputStream.flush();
//                                } catch (IOException e) {
//                                    throw new RuntimeException(e);
//                                }
//                            }, throwable -> {
//                                try {
//                                    outputStream.close();
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//                            }, () -> {
//                                try {
//                                    outputStream.close();
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//                            });
//                });
//    }

//    @PostMapping(value = "/api/ai/chat/completions", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public ResponseEntity<StreamingResponseBody> chat(@RequestBody String body) {
//        String ollamaUrl = "http://192.168.44.151:11434/v1/chat/completions";
//
//        RestTemplate restTemplate = new RestTemplate();
//        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
//
//        return ResponseEntity.ok()
//                .contentType(new MediaType("text", "event-stream", StandardCharsets.UTF_8))
//                .body(outputStream -> {
//                    restTemplate.execute(ollamaUrl, HttpMethod.POST,
//                            request -> {
//                                request.getBody().write(body.getBytes(StandardCharsets.UTF_8));
//                            },
//                            response -> {
//                                try (InputStream inputStream = response.getBody()) {
//                                    byte[] buffer = new byte[1024];
//                                    int bytesRead;
//                                    while ((bytesRead = inputStream.read(buffer)) != -1) {
//                                        outputStream.write(buffer, 0, bytesRead);
//                                        outputStream.flush();
//                                    }
//                                }
//                                return null;
//                            });
//                });
//    }
}
