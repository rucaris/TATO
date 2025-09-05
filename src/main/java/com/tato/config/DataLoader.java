package com.tato.config;

import com.tato.model.Attraction;
import com.tato.repository.AttractionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "app.data.auto-load",
        havingValue = "true",
        matchIfMissing = true
)
public class DataLoader implements CommandLineRunner {

    private final AttractionRepository attractionRepository;

    @Override
    public void run(String... args) throws Exception {
        if (attractionRepository.count() > 0) {
            log.info("관광지 데이터가 이미 존재합니다. 로딩을 건너뜁니다.");
            return;
        }

        log.info("CSV 파일에서 관광지 데이터를 로드합니다...");
        loadFromCSV();
    }

    private void loadFromCSV() {
        try {
            ClassPathResource resource = new ClassPathResource("static/data/tokyo_attractions.csv");

            if (!resource.exists()) {
                log.error("CSV 파일을 찾을 수 없습니다: {}", resource.getFilename());
                return;
            }

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
                    String[] fields = parseCSVLine(line);

                    if (fields.length >= 7) {
                        String spotId = fields[0].trim();  // CSV의 spot_id
                        String name = fields[1].trim();
                        String category = fields[2].trim();
                        String ward = fields[3].trim();
                        String address = fields[4].trim();
                        String latStr = fields[5].trim();
                        String lngStr = fields[6].trim();
                        String description = fields.length > 7 ? fields[7].trim() : "";

                        log.debug("라인 {}: spotId={}, 이름={}, 위도={}, 경도={}",
                                lineNumber, spotId, name, latStr, lngStr);

                        if (name.isEmpty() || spotId.isEmpty()) continue;

                        // 중복 체크
                        if (attractionRepository.existsBySpotId(spotId)) {
                            log.debug("이미 존재하는 spotId: {}", spotId);
                            continue;
                        }

                        Double lat = null, lng = null;
                        try {
                            if (!latStr.isEmpty()) lat = Double.parseDouble(latStr);
                            if (!lngStr.isEmpty()) lng = Double.parseDouble(lngStr);
                        } catch (NumberFormatException e) {
                            log.warn("좌표 파싱 실패 (라인 {}): 위도={}, 경도={}", lineNumber, latStr, lngStr);
                        }

                        if (lat != null && lng != null) {
                            Attraction attraction = Attraction.builder()
                                    .spotId(spotId)  // ✅ CSV의 spot_id 저장
                                    .name(name)
                                    .category(category.isEmpty() ? "기타" : category)
                                    .address(address.isEmpty() ? null : address)
                                    .latitude(lat)
                                    .longitude(lng)
                                    .description(description.isEmpty() ? (name + "에 대한 정보입니다.") : description)
                                    .build();

                            attractionRepository.save(attraction);
                            count++;

                            if (count <= 5) {
                                log.info("저장됨: spotId={}, 이름={} (위도: {}, 경도: {})",
                                        spotId, name, lat, lng);
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

    private String[] parseCSVLine(String line) {
        return line.split(",", -1);
    }
}