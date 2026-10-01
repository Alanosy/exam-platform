-- -----------------------------------------------------------------------------
-- 证书模块：考试表增加「及格证书」字段
--
-- 背景：
--   exam 表原本只管考试规则，没有「过了发什么证书」这一项。
--   证书模板由 ruoyi-exam-cert 服务维护（exam_certificate 表），
--   这里只在 exam 上挂一个 cert_id 指向模板，为空表示这场考试不发证书。
--
-- 字段：
--   cert_id bigint  NULL —— 证书模板ID，NULL / 0 表示不发证书
--
-- 幂等：先查 information_schema 判断列是否已存在，存在则跳过。
--       重复执行不会产生重复列，也不会覆盖已有数据。
-- -----------------------------------------------------------------------------

-- exam.cert_id
SET @exist_cert_id := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'exam'
    AND COLUMN_NAME = 'cert_id'
);
SET @sql_cert_id := IF(@exist_cert_id = 0,
  'ALTER TABLE `exam` ADD COLUMN `cert_id` bigint DEFAULT NULL COMMENT ''及格证书模板ID，为空表示本场考试不颁发证书'' AFTER `anti_cheat_config`',
  'SELECT 1');
PREPARE stmt FROM @sql_cert_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
