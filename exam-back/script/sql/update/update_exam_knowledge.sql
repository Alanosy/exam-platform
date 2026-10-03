-- -----------------------------------------------------------------------------
-- 知识点模块：知识点表 + 试题-知识点关联表
--
-- 背景：
--   question 表**没有**知识点字段，导致三处能力落不了地：
--     1) 错题归因（AI 诊断）拿不到 knowledge_points，只能让模型从题干瞎猜，
--        错题本 detail.vue 里至今写死 knowledgePoints: []（注释「题库还没打标」）；
--     2) AI 出题的「知识点」下拉没有数据源（knowledgeOptions 恒为空数组），只能手输；
--     3) 试卷审查的知识点覆盖率、考试统计的知识点薄弱分析都只能靠题干推断。
--   一期统计模块（ruoyi-exam-stat）曾用「题库分类树」顶替知识点，但分类粒度是
--   章节/目录级且一道题只能属于一个分类，扛不住「一道题涉及多个知识点」。
--
-- 设计：
--   exam_knowledge_point  两级：parent_id = 0 为章节（根节点），其下为知识点；
--                         全局共享（不挂题库），按租户隔离。
--   exam_question_knowledge  试题 ↔ 知识点 多对多，唯一约束避免重复关联。
--
-- 幂等：CREATE TABLE IF NOT EXISTS，重复执行不会重建也不会清数据。
-- 数据库：ry-exam
-- -----------------------------------------------------------------------------

-- 知识点（两级：章节 → 知识点）
CREATE TABLE IF NOT EXISTS `exam_knowledge_point` (
  `id`           bigint      NOT NULL COMMENT '主键ID',
  `parent_id`    bigint      NOT NULL DEFAULT 0 COMMENT '父级ID，0 表示章节（根节点）',
  `name`         varchar(100) NOT NULL COMMENT '知识点名称',
  `sort`         int         NOT NULL DEFAULT 0 COMMENT '排序',
  `del_flag`     tinyint     NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  `tenant_id`    bigint      DEFAULT NULL COMMENT '租户ID',
  `create_by`    varchar(64) DEFAULT '' COMMENT '创建者',
  `create_dept`  varchar(64) DEFAULT NULL COMMENT '创建部门',
  `create_time`  datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`    varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time`  datetime    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_knowledge_parent` (`parent_id`),
  KEY `idx_knowledge_tenant` (`tenant_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '知识点（两级：章节 / 知识点）';

-- 试题-知识点关联
CREATE TABLE IF NOT EXISTS `exam_question_knowledge` (
  `id`           bigint   NOT NULL COMMENT '主键ID',
  `question_id`  bigint   NOT NULL COMMENT '试题ID',
  `knowledge_id` bigint   NOT NULL COMMENT '知识点ID',
  `tenant_id`    bigint   DEFAULT NULL COMMENT '租户ID',
  `create_by`    varchar(64) DEFAULT '' COMMENT '创建者',
  `create_dept`  varchar(64) DEFAULT NULL COMMENT '创建部门',
  `create_time`  datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`    varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time`  datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_question_knowledge` (`question_id`, `knowledge_id`),
  KEY `idx_qk_knowledge` (`knowledge_id`),
  KEY `idx_qk_tenant` (`tenant_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '试题-知识点关联';
