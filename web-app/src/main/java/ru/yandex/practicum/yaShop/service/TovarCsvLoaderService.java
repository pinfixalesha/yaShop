package ru.yandex.practicum.yaShop.service;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.yaShop.entities.Tovar;
import ru.yandex.practicum.yaShop.repositories.TovarRepository;
import com.opencsv.CSVReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class TovarCsvLoaderService {

    @Autowired
    private TovarRepository tovarRepository;

    @Autowired
    private TovarRedisCacheService tovarRedisCacheService;

    public Mono<String> uploadCsv(FilePart file) {
        if (file==null) {
            return Mono.just("Файл пуст!");
        }

        return tovarRedisCacheService.evictTotalTovarCountFromCache()
                .then(processCsvFile(file))
                .onErrorResume(error -> Mono.just("Ошибка при обработке файла: " + error.getMessage()));
    }

    private Mono<String> processCsvFile(FilePart file) {
        return DataBufferUtils.join(file.content())
                .map(dataBuffer -> dataBuffer.asInputStream())
                .flatMap(inputStream -> parseCsv(inputStream))
                .flatMap(tovars -> saveTovarsToDB(tovars));
    }

    private Mono<List<Tovar>> parseCsv(InputStream inputStream) {
        return Mono.fromCallable(() -> parseCsvRecords(inputStream));
    }

    private List<Tovar> parseCsvRecords(InputStream inputStream) {
        try {
            CSVReader csvReader = createCsvReader(inputStream);
            List<Tovar> tovars = new ArrayList<>();
            String[] nextRecord;

            while ((nextRecord = csvReader.readNext()) != null) {
                Tovar tovar = mapCsvRecordToTovar(nextRecord);
                if (tovar != null) {
                    tovars.add(tovar);
                }
            }

            return tovars;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private CSVReader createCsvReader(InputStream inputStream) {
        return new CSVReaderBuilder(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .withCSVParser(new CSVParserBuilder()
                        .withSeparator(';')
                        .withQuoteChar('"')
                        .withEscapeChar('\\')
                        .build())
                .build();
    }

    private Tovar mapCsvRecordToTovar(String[] record) {
        if (record.length == 4) {
            Tovar tovar = new Tovar();
            tovar.setName(record[0]); // Наименование
            tovar.setPicture(record[1]); // Картинка
            tovar.setDescription(record[2]); // Описание
            tovar.setPrice(BigDecimal.valueOf(Double.parseDouble(record[3]))); // Цена
            return tovar;
        }
        return null;
    }

    private Mono<String> saveTovarsToDB(List<Tovar> tovars) {
        return tovarRepository.saveAll(tovars)
                .collectList()
                .map(savedTovars -> "Успешно загружено " + savedTovars.size() + " товаров.");
    }
}
