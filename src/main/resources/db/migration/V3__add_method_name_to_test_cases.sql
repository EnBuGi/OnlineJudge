ALTER TABLE project_test_cases ADD COLUMN method_name VARCHAR(255);
-- 기존 데이터는 name을 method_name으로 복사 (초기값 설정)
UPDATE project_test_cases SET method_name = name WHERE method_name IS NULL;
ALTER TABLE project_test_cases MODIFY COLUMN method_name VARCHAR(255) NOT NULL;
