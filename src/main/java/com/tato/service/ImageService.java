package com.tato.service;

import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class ImageService {

    // 관광지 이미지 수동으로 다 연결했습니다..
    private static final Map<Long, String> IMAGE_URLS = Map.of(
            1L, "/images/attractions/architecture_img01.jpg",
            2L, "/images/attractions/architecture_img02.jpg",
            3L, "/images/attractions/architecture_img03.jpg",
            4L, "/images/attractions/architecture_img04.jpg",
            5L, "/images/attractions/architecture_img05.jpg"
    );

    // 이미지 없을 때
    private static final String DEFAULT_IMAGE_URL =
            "https://images.unsplash.com/photo-1513475382585-d06e58bcb0e0?w=300&h=200&fit=crop";

    /*관광지 ID로 이미지 URL 반환*/
    public String getImageUrl(Long attractionId) {
        return IMAGE_URLS.getOrDefault(attractionId, DEFAULT_IMAGE_URL);
    }

    /* 이미지가 있는지 확인*/
    public boolean hasImage(Long attractionId) {
        return IMAGE_URLS.containsKey(attractionId);
    }
}