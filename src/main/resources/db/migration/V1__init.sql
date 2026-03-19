CREATE TABLE users (
    id VARCHAR(36) NOT NULL,
    github_id VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(20) NOT NULL,
    generation INT NOT NULL,
    role VARCHAR(255),
    profile_image_url VARCHAR(500) NOT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE projects (
    id VARCHAR(36) NOT NULL,
    generation INT NOT NULL,
    type VARCHAR(20) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    start_date DATETIME(6) NOT NULL,
    due_date DATETIME(6) NOT NULL,
    skeleton_url VARCHAR(255),
    test_code_url VARCHAR(255),
    time_limit INT,
    memory_limit INT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE submissions (
    id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    project_id VARCHAR(36) NOT NULL,
    repo_url VARCHAR(255) NOT NULL,
    status VARCHAR(20),
    score INT,
    submit_at DATETIME(6) NOT NULL,
    time_usage INT,
    memory_usage INT,
    total_tests INT,
    passed_tests INT,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE submission_result_details (
    id VARCHAR(36) NOT NULL,
    submission_id VARCHAR(36) NOT NULL,
    method_name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    duration_ms INT,
    message VARCHAR(2000),
    is_hidden TINYINT(1) NOT NULL,
    score INT NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
