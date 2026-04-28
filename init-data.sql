-- 초기 상품 데이터 (Railway MySQL 연결 후 실행)
-- kairos DB 생성 후 실행하세요

CREATE DATABASE IF NOT EXISTS kairos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE kairos;

-- 앱 실행 시 JPA가 테이블 자동 생성합니다 (ddl-auto: update)
-- 아래는 샘플 상품 데이터입니다

INSERT INTO products (name, description, price, stock, category, image_url, active, created_at) VALUES
('필라테스 매직 서클', '고강도 필라테스 링. 허벅지, 팔, 코어 강화에 최적화된 프리미엄 소재 사용.', 38000, 50, 'PILATES', NULL, true, NOW()),
('소프트 볼 세트 (2개)', '부드러운 촉감의 필라테스 소프트 볼. 균형 훈련 및 코어 안정화에 활용.', 24000, 80, 'PILATES', NULL, true, NOW()),
('저항 밴드 세트', '3단계 강도의 라텍스 저항 밴드. 스트레칭, 재활, 근력 강화 다목적 활용.', 19000, 100, 'FITNESS', NULL, true, NOW()),
('요가 블록 코르크', '천연 코르크 소재 요가 블록. 초보자부터 중급자까지 자세 교정에 필수품.', 22000, 60, 'YOGA', NULL, true, NOW()),
('논슬립 요가 매트 6mm', '미끄럼 방지 TPE 소재 요가 매트 6mm. 관절 보호 및 안정적인 그립감.', 55000, 40, 'YOGA', NULL, true, NOW()),
('폼롤러 고강도', '깊은 근막 이완을 위한 고밀도 폼롤러. 운동 후 회복 및 통증 완화.', 35000, 45, 'RECOVERY', NULL, true, NOW()),
('스트레칭 스트랩', '부드러운 면 소재의 다중 루프 스트레칭 스트랩. 유연성 향상 트레이닝.', 16000, 90, 'FITNESS', NULL, true, NOW()),
('필라테스 슬라이드 디스크', '바닥 운동 강도를 높여주는 슬라이드 디스크. 코어 및 전신 근력 강화.', 28000, 35, 'PILATES', NULL, true, NOW());
