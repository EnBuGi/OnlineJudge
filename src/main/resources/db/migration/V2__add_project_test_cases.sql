CREATE TABLE IF NOT EXISTS project_test_cases (
    id VARCHAR(36) NOT NULL,
    project_id VARCHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    score INT NOT NULL,
    is_hidden TINYINT(1) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_project_test_case_project FOREIGN KEY (project_id) REFERENCES projects (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
