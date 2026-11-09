-- ============================================================
-- V2 装修指南（公开内容模块，P0 轻量 + P1 攻略）
-- 与「阶段时间线」8 标准阶段共用 stage_key 关联口径。
-- guides：阶段全景（轻量说明，P0）；guide_details：单阶段攻略（P1，运营维护）。
-- 内容初稿，待运营完善。
-- ============================================================

-- 装修指南（阶段全景）
CREATE TABLE guides (
    id          BIGSERIAL PRIMARY KEY,
    stage_key   VARCHAR(30)  NOT NULL UNIQUE,  -- 阶段标识：demolition/hydropower/...
    title       VARCHAR(50)  NOT NULL,         -- 显示名：拆改/水电/...
    summary     VARCHAR(500),                  -- 轻量说明（P0）
    duration    VARCHAR(50),                   -- 周期参考
    cost_ratio  NUMERIC(5, 4) NOT NULL DEFAULT 0,  -- 花费占比 0~1
    sort_order  INT          NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_guides_sort ON guides (sort_order);

-- 单阶段攻略明细（P1：买什么/找谁/避坑点/预算区间）
CREATE TABLE guide_details (
    id         BIGSERIAL PRIMARY KEY,
    guide_id   BIGINT       NOT NULL REFERENCES guides (id),
    section    VARCHAR(30)  NOT NULL,          -- 买什么 / 找谁 / 避坑点 / 预算区间
    content    TEXT,
    sort_order INT          NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_guide_details_guide ON guide_details (guide_id);

-- ---------- 种子数据：8 标准阶段（轻量说明 + P1 攻略初稿） ----------
INSERT INTO guides (stage_key, title, summary, duration, cost_ratio, sort_order) VALUES
('demolition',   '拆改', '拆除原有墙体/地面/吊顶，重新规划空间格局，是装修的第一步。', '约 3–7 天',   0.05, 1),
('hydropower',   '水电', '隐蔽工程核心：重排给排水与强弱电，定位开关插座与出水口，直接决定日后使用便利与安全。', '约 7–15 天', 0.20, 2),
('masonry',      '泥瓦', '厨卫防水、墙地砖铺贴、包立管与地面找平，防水闭水试验在此验收。', '约 15–30 天', 0.18, 3),
('carpentry',    '木工', '吊顶、背景墙、现场柜体与门窗套制作，部分造型在油漆前完成。', '约 10–20 天', 0.15, 4),
('painting',     '油漆', '墙面刮腻子、打磨、刷漆/贴壁布，决定最终观感与环保，需通风散味。', '约 10–20 天（含晾干）', 0.10, 5),
('installation', '安装', '橱柜、木门、地板、洁具、开关灯具、烟机灶具等成品进场安装。', '约 7–15 天',  0.12, 6),
('soft',         '软装', '家具、家电、窗帘、灯具与装饰陈设进场，营造最终居住氛围。', '约 7–30 天',  0.15, 7),
('acceptance',   '验收', '逐项验收施工质量、核对工程量与质保，保洁后择吉日入住。', '约 3–7 天',   0.05, 8);

INSERT INTO guide_details (guide_id, section, content, sort_order) VALUES
((SELECT id FROM guides WHERE stage_key='demolition'), '买什么', '基本无需采购，主要是拆除与垃圾清运；如需保护保留项准备防护膜。', 1),
((SELECT id FROM guides WHERE stage_key='demolition'), '找谁', '拆改/拆除工队或装修公司；涉及墙体改动需确认是否为承重墙（物业/结构确认）。', 2),
((SELECT id FROM guides WHERE stage_key='demolition'), '避坑点', '严禁拆承重墙/剪力墙；拆前断水断电，做好地漏与下水管保护防堵。', 3),
((SELECT id FROM guides WHERE stage_key='demolition'), '预算区间', '约占总预算 3%–6%，以人工与垃圾清运为主。', 4),

((SELECT id FROM guides WHERE stage_key='hydropower'), '买什么', '电线、PVC/PE 穿线管、PPR 给水管、底盒暗盒、强弱电箱；点位密集建议留余量。', 1),
((SELECT id FROM guides WHERE stage_key='hydropower'), '找谁', '持证专业水电工；设计阶段与设计师/水电师傅共同做点位交底。', 2),
((SELECT id FROM guides WHERE stage_key='hydropower'), '避坑点', '水电走顶不走地便于检修；强弱电分管分槽防干扰；完工拍照留底管线走向。', 3),
((SELECT id FROM guides WHERE stage_key='hydropower'), '预算区间', '约占总预算 15%–22%，隐蔽工程不建议省。', 4),

((SELECT id FROM guides WHERE stage_key='masonry'), '买什么', '瓷砖/石材、防水涂料、水泥沙子、地漏、过门石；瓷砖按 5% 损耗备货。', 1),
((SELECT id FROM guides WHERE stage_key='masonry'), '找谁', '泥瓦工；防水建议由专业防水师傅施工并做闭水试验。', 2),
((SELECT id FROM guides WHERE stage_key='masonry'), '避坑点', '卫生间防水刷到 1.8m 并做 48h 闭水；墙砖泡水、留缝防空鼓。', 3),
((SELECT id FROM guides WHERE stage_key='masonry'), '预算区间', '约占总预算 15%–20%（含瓷砖，品牌差异大）。', 4),

((SELECT id FROM guides WHERE stage_key='carpentry'), '买什么', '石膏板/龙骨、生态板、线条、现场柜体板材；与定制柜衔接预留尺寸。', 1),
((SELECT id FROM guides WHERE stage_key='carpentry'), '找谁', '木工师傅；吊顶/造型与油漆工序衔接。', 2),
((SELECT id FROM guides WHERE stage_key='carpentry'), '避坑点', '吊顶转角用整板防裂；预留中央空调/新风检修口；与定制柜尺寸对齐。', 3),
((SELECT id FROM guides WHERE stage_key='carpentry'), '预算区间', '约占总预算 10%–16%（含定制柜则更高）。', 4),

((SELECT id FROM guides WHERE stage_key='painting'), '买什么', '腻子、底漆、面漆（乳胶漆）、阴阳角条；优先选低 VOC 环保漆。', 1),
((SELECT id FROM guides WHERE stage_key='painting'), '找谁', '油漆工；调色可交由品牌门店电脑调色。', 2),
((SELECT id FROM guides WHERE stage_key='painting'), '避坑点', '一遍底漆两遍面漆；阴干勿暴晒；完工通风散味再进软装。', 3),
((SELECT id FROM guides WHERE stage_key='painting'), '预算区间', '约占总预算 8%–12%。', 4),

((SELECT id FROM guides WHERE stage_key='installation'), '买什么', '橱柜、木门、地板、洁具、开关插座、灯具、烟机灶具、五金。', 1),
((SELECT id FROM guides WHERE stage_key='installation'), '找谁', '各主材商家提供上门安装；协调同一周集中进场。', 2),
((SELECT id FROM guides WHERE stage_key='installation'), '避坑点', '顺序先柜后门再地板；洁具/龙头查漏点；保留保修卡与发票。', 3),
((SELECT id FROM guides WHERE stage_key='installation'), '预算区间', '约占总预算 10%–18%（主材占比弹性大）。', 4),

((SELECT id FROM guides WHERE stage_key='soft'), '买什么', '家具、床垫、窗帘、灯具、家电、装饰画与绿植。', 1),
((SELECT id FROM guides WHERE stage_key='soft'), '找谁', '家居/家电卖场或设计师代购；可分批入场。', 2),
((SELECT id FROM guides WHERE stage_key='soft'), '避坑点', '先量尺寸再买避免进门难；甲醛治理以通风为主，慎信速效产品。', 3),
((SELECT id FROM guides WHERE stage_key='soft'), '预算区间', '约占总预算 12%–20%（丰俭由人）。', 4),

((SELECT id FROM guides WHERE stage_key='acceptance'), '买什么', '基本无需采购；可备验房工具或请第三方验房。', 1),
((SELECT id FROM guides WHERE stage_key='acceptance'), '找谁', '装修公司/工长逐项验收；争议项可请第三方监理。', 2),
((SELECT id FROM guides WHERE stage_key='acceptance'), '避坑点', '对照合同核工程量与质保；水电点位、防水、瓷砖空鼓重点查；留 5%–10% 尾款。', 3),
((SELECT id FROM guides WHERE stage_key='acceptance'), '预算区间', '验收本身成本低，但尾款与质保条款要写清。', 4);
