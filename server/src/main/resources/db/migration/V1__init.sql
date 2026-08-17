-- ============================================================
-- V1 初始化建表：装修记录核心数据模型
-- 金额统一 NUMERIC(14,2)；阶段状态三态枚举（未开始/进行中/已完成）
-- 图片走 imgbb，数据库仅存 URL（attachments 多态关联）
-- ============================================================

-- 用户
CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    nickname      VARCHAR(50),
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 装修项目（一套房 = 一个项目）
CREATE TABLE projects (
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT       NOT NULL REFERENCES users (id),
    name              VARCHAR(100) NOT NULL,
    address           VARCHAR(255),
    area              NUMERIC(10, 2),
    total_budget      NUMERIC(14, 2) NOT NULL DEFAULT 0,
    start_date        DATE,
    expected_end_date DATE,
    status            VARCHAR(20)  NOT NULL DEFAULT '进行中',
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_projects_user ON projects (user_id);

-- 阶段（标准 8 工序，创建项目时由后端初始化；budget 支持阶段预算对比）
CREATE TABLE phases (
    id         BIGSERIAL PRIMARY KEY,
    project_id BIGINT       NOT NULL REFERENCES projects (id),
    name       VARCHAR(50)  NOT NULL,
    sort_order INT          NOT NULL,
    start_date DATE,
    end_date   DATE,
    status     VARCHAR(20)  NOT NULL DEFAULT '未开始',
    budget     NUMERIC(14, 2) NOT NULL DEFAULT 0,
    note       TEXT,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_phases_project ON phases (project_id);

-- 花销
CREATE TABLE expenses (
    id           BIGSERIAL PRIMARY KEY,
    project_id   BIGINT       NOT NULL REFERENCES projects (id),
    phase_id     BIGINT       REFERENCES phases (id),
    category     VARCHAR(30)  NOT NULL,
    amount       NUMERIC(14, 2) NOT NULL,
    pay_method   VARCHAR(30),
    expense_date DATE         NOT NULL,
    note         TEXT,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_expenses_project ON expenses (project_id);
CREATE INDEX idx_expenses_phase ON expenses (phase_id);

-- 附件（图片，imgbb URL）
CREATE TABLE attachments (
    id         BIGSERIAL PRIMARY KEY,
    project_id BIGINT       NOT NULL REFERENCES projects (id),
    ref_type   VARCHAR(20)  NOT NULL,   -- PROJECT / PHASE / EXPENSE
    ref_id     BIGINT,
    url        TEXT         NOT NULL,
    thumb_url  TEXT,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_attachments_ref ON attachments (ref_type, ref_id);
CREATE INDEX idx_attachments_project ON attachments (project_id);
