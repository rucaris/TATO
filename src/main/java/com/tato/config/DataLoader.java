package com.tato.config;

import com.tato.model.Attraction;
import com.tato.repository.AttractionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final AttractionRepository attractionRepository;

    @Override
    public void run(String... args) throws Exception {
        if (attractionRepository.count() > 0) {
            log.info("이미 관광지 데이터가 있습니다. 기존 데이터를 삭제하고 다시 로드합니다.");
            attractionRepository.deleteAll(); // 기존 데이터 삭제
        }

        log.info("CSV 파일에서 관광지 데이터를 로드합니다...");
        loadFromCSV();
    }

    private void loadFromCSV() {
        try {
            ClassPathResource resource = new ClassPathResource("static/data/tokyo_attractions.csv");
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)
            );

            String line;
            boolean isFirstLine = true;
            int count = 0;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                try {
                    // CSV 파싱 개선
                    String[] fields = parseCSVLine(line);

                    if (fields.length >= 7) {
                        String spotId = fields[0].trim();
                        String name = fields[1].trim();
                        String category = fields[2].trim();
                        String ward = fields[3].trim();
                        String address = fields[4].trim();
                        String latStr = fields[5].trim();
                        String lngStr = fields[6].trim();
                        String description = fields.length > 7 ? fields[7].trim() : "";

                        // 디버깅 로그
                        log.debug("라인 {}: ID={}, 이름={}, 위도={}, 경도={}", lineNumber, spotId, name, latStr, lngStr);

                        if (name.isEmpty()) continue;

                        Double lat = null, lng = null;
                        try {
                            if (!latStr.isEmpty()) lat = Double.parseDouble(latStr);
                            if (!lngStr.isEmpty()) lng = Double.parseDouble(lngStr);
                        } catch (NumberFormatException e) {
                            log.warn("좌표 파싱 실패 (라인 {}): 위도={}, 경도={}", lineNumber, latStr, lngStr);
                        }

                        // 좌표가 있는 것만 저장
                        if (lat != null && lng != null) {
                            Attraction attraction = Attraction.builder()
                                    .name(name)
                                    .category(category.isEmpty() ? "기타" : category)
                                    .address(address.isEmpty() ? null : address)
                                    .latitude(lat)
                                    .longitude(lng)
                                    .description(description.isEmpty() ? (name + "에 대한 정보입니다.") : description)
                                    .build();

                            attractionRepository.save(attraction);
                            count++;

                            // 처음 5개는 상세 로그
                            if (count <= 5) {
                                log.info("저장됨: {} (위도: {}, 경도: {})", name, lat, lng);
                            }
                        } else {
                            log.warn("좌표가 없어 건너뜀: {}", name);
                        }
                    }
                } catch (Exception e) {
                    log.warn("라인 {} 처리 실패: {} - {}", lineNumber, line, e.getMessage());
                }
            }

            reader.close();
            log.info("총 {}개의 관광지가 로드되었습니다!", count);

        } catch (Exception e) {
            log.error("CSV 파일 로드 실패", e);
        }
    }

    // CSV 파싱
    private String[] parseCSVLine(String line) {
        return line.split(",", -1); // 빈필드 포함
    }
}