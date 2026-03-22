ALTER TABLE submissions ADD COLUMN language VARCHAR(50);
-- 기존 데이터는 JAVA로 초기화
UPDATE submissions SET language = 'JAVA' WHERE language IS NULL;
ALTER TABLE submissions MODIFY COLUMN language VARCHAR(50) NOT NULL;
