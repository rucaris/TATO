
INSERT INTO attraction (id, name, name_ko, category, address, address_full_ko, latitude, longitude, description, description_ko)
VALUES (1, 'Ueno Park Nature Study', '우에노 자연학습원', '자연·산·공원', 'Tokyo', '도쿄', 35.7148, 139.7745,
        'Sample attraction for demo', '데모용 샘플 관광지입니다.'); 
-- 리뷰 샘플
INSERT INTO review (attraction_id, author, rating, content, created_at)
VALUES (1, '김우성', 5, '도쿄 여행에서 꼭 들를만해요!', NOW());
