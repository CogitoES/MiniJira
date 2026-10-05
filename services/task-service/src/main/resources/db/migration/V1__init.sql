CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL,
    priority VARCHAR(50) NOT NULL,
    assignee_id BIGINT,
    reporter_id BIGINT NOT NULL,
    deadline TIMESTAMP,
    story_points INT,
    labels VARCHAR(255),
    jira_key VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE task_labels (
    task_id BIGINT REFERENCES tasks(id),
    label VARCHAR(50) NOT NULL,
    PRIMARY KEY (task_id, label)
);
