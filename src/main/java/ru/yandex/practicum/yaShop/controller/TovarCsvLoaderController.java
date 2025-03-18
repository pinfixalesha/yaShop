package ru.yandex.practicum.yaShop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.yaShop.service.TovarCsvLoaderService;

@Controller
public class TovarCsvLoaderController {

    @Autowired
    private TovarCsvLoaderService tovarCsvLoaderService;

    @GetMapping("/upload-csv")
    public Mono<String> showUploadForm() {
        return Mono.just("upload-csv");
    }

    @PostMapping(value = "/upload-csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<Rendering> handleFileUpload(
            @RequestPart(required = false, name = "file") FilePart file) {
        return tovarCsvLoaderService.uploadCsv(file)
                .map(message -> Rendering.view("upload-csv")
                        .modelAttribute("message", message)
                        .build());

    }
}
