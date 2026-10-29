-- 开发/演示用：每次启动重建表（会清空历史任务，仅演示环境如此）
DROP TABLE IF EXISTS task;
DROP TABLE IF EXISTS template;

CREATE TABLE template (
    id                    VARCHAR(64)  PRIMARY KEY,
    title                 VARCHAR(128) NOT NULL,
    category              VARCHAR(64)  NOT NULL,
    cover_url             VARCHAR(255),
    description           VARCHAR(512),
    required_image_count  INT          NOT NULL DEFAULT 1,
    accepted_mime_types   VARCHAR(255),
    max_file_size_bytes   BIGINT       NOT NULL DEFAULT 5242880,
    created_at            DATETIME     DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE task (
    id                    VARCHAR(64)  PRIMARY KEY,
    user_id               VARCHAR(64)  NOT NULL,
    template_id           VARCHAR(64)  NOT NULL,
    template_title        VARCHAR(128),
    required_image_count  INT          NOT NULL DEFAULT 1,
    status                VARCHAR(32)  NOT NULL,
    stage                 VARCHAR(32)  NOT NULL,
    stage_detail          VARCHAR(128),
    result_url            VARCHAR(255),
    fail_reason           VARCHAR(255),
    created_at            DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at            DATETIME     DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_task_user ON task (user_id, created_at DESC);
