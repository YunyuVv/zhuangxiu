-- ============================================================
-- V3 阶段自定义 + 阶段子项 + 花销物品名
--  - expenses.name：物品/器材名称（采购清单用，新增记录必填）
--  - phase_tasks：每个阶段内用户自定义的子项清单（如「盯打压/验防水」）
--    与 expenses 区分：子项无金额，只用于阶段内待办/检查勾选
--  - 阶段本身不新增表（phases 已支持任意 name/数量/sort_order），仅放开增删接口
-- 不影响 V1/V2 已执行的数据。
-- ============================================================

-- 花销增加「物品/器材名称」（存量数据允许为空，新记录必填，由应用层校验）
ALTER TABLE expenses ADD COLUMN name VARCHAR(100);

-- 阶段子项清单
CREATE TABLE phase_tasks (
    id         BIGSERIAL PRIMARY KEY,
    project_id BIGINT       NOT NULL REFERENCES projects (id),
    phase_id   BIGINT       NOT NULL REFERENCES phases (id),
    name       VARCHAR(200) NOT NULL,
    done       BOOLEAN      NOT NULL DEFAULT FALSE,
    sort_order INT          NOT NULL,
    note       TEXT,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_phase_tasks_phase ON phase_tasks (phase_id);
CREATE INDEX idx_phase_tasks_project ON phase_tasks (project_id);
