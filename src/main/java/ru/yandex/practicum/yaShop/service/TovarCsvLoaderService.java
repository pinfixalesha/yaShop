package ru.yandex.practicum.yaShop.service;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.yaShop.entities.Tovar;
import ru.yandex.practicum.yaShop.repositories.TovarRepository;
import com.opencsv.CSVReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class TovarCsvLoaderService {

    @Autowired
    private TovarRepository tovarRepository;


    public String uploadCsv(MultipartFile file) {
        if (file.isEmpty()) {
            return "Файл пуст!";
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             CSVReader csvReader = new CSVReaderBuilder(reader)
                     .withCSVParser(new CSVParserBuilder()
                             .withSeparator(';')
                             .withQuoteChar('"')
                             .withEscapeChar('\\')
                             .build())
                     .build();

            String[] nextRecord;
            List<Tovar> tovars = new ArrayList<>();
            int count = 0;

            while ((nextRecord = csvReader.readNext()) != null) {
                if (nextRecord.length == 4) {
                    Tovar tovar = new Tovar();
                    tovar.setName(nextRecord[0]); // Наименование
                    tovar.setPicture(nextRecord[1]); // Картинка
                    tovar.setDescription(nextRecord[2]); // Описание
                    tovar.setPrice(BigDecimal.valueOf(Double.parseDouble(nextRecord[3]))); // Цена
                    tovars.add(tovar);
                    count++;
                }
            }

            tovarRepository.saveAll(tovars);
            return "Успешно загружено " + count + " товаров.";

        } catch (Exception e) {
            return "Ошибка при чтении файла: " + e.getMessage();
        }
    }
}
