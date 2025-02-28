package ru.yandex.practicum.yaShop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.yaShop.service.TovarCsvLoaderService;

@Controller
public class TovarCsvLoaderController {

    @Autowired
    private TovarCsvLoaderService tovarCsvLoaderService;

    @GetMapping("/upload-csv")
    public String showUploadForm() {
        return "upload-csv";
    }

    @PostMapping("/upload-csv")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, Model model) {
        String message = tovarCsvLoaderService.uploadCsv(file);
        model.addAttribute("message", message);
        return "upload-csv";
    }
}
