-- 修复 AI 助手创建的试题 tenant_id 异常问题
-- 根因：AI 工具端点走 Dubbo 调用没有登录上下文，tenant_id 未正确设置，
-- 导致题目 tenant_id 存成 0 或 NULL，用户在自己租户下看不到。
-- 此脚本将异常 tenant_id 的题目修正为默认租户 000000。

-- 1. 修复 tenant_id 为 0 的题目（应为字符串 '000000'）
UPDATE question SET tenant_id = '000000' WHERE tenant_id = 0 OR tenant_id = '0';

-- 2. 修复 tenant_id 为 NULL 的题目
UPDATE question SET tenant_id = '000000' WHERE tenant_id IS NULL;

-- 3. 验证修复结果
SELECT tenant_id, COUNT(*) AS cnt FROM question GROUP BY tenant_id;
