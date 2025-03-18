package ru.yandex.practicum.yaShop.service;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.yaShop.entities.Tovar;
import ru.yandex.practicum.yaShop.repositories.TovarRepository;
import com.opencsv.CSVReader;
import java.io.BufferedReader;
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


    public Mono<String> uploadCsv(FilePart file) {
        if (file==null) {
            return Mono.just("Файл пуст!");
        }

        return DataBufferUtils.join(file.content())
                .map(dataBuffer -> {
                    InputStream inputStream = dataBuffer.asInputStream();
                    return new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                })
                .flatMap(reader -> Mono.fromCallable(() -> {
                    // Инициализируем CSVReader
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

                    // Читаем CSV-файл построчно
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

                    // Сохраняем товары в базу данных
                    return tovarRepository.saveAll(tovars).then(Mono.just("Успешно загружено " + count + " товаров."));
                }))
                .flatMap(m -> m)
                .onErrorResume(error -> Mono.just("Ошибка при обработке файла: " + error.getMessage()));
    }
}
