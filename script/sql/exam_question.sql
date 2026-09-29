/*
 Navicat Premium Data Transfer

 Source Server         : 8.137.151.232
 Source Server Type    : MySQL
 Source Server Version : 80042
 Source Host           : 8.137.151.232:3306
 Source Schema         : ry-cloud

 Target Server Type    : MySQL
 Target Server Version : 80042
 File Encoding         : 65001

 Date: 28/09/2026 16:38:51
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for exam_question
-- ----------------------------
DROP TABLE IF EXISTS `exam_question`;
CREATE TABLE `exam_question`  (
                                  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                  `exam_id` bigint(0) NOT NULL COMMENT '试卷ID',
                                  `question_id` bigint(0) NOT NULL COMMENT '题库试题ID',
                                  `exam_question_score` decimal(5, 2) NOT NULL COMMENT '该题在本试卷的分值，覆盖原题默认分值',
                                  `sort` int(0) NOT NULL DEFAULT 0 COMMENT '题目在试卷内排序',
                                  `is_question_snapshot` tinyint(0) NOT NULL DEFAULT 0 COMMENT '0引用原题 1开启快照，独立存储题目',
                                  `snapshot_content` json NULL COMMENT '快照内容，is_question_snapshot=1时生效，存储题干、选项、答案',
                                  `tenant_id` bigint(0) NULL DEFAULT NULL COMMENT '租户ID',
                                  `del_flag` tinyint(0) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
                                  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
                                  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
                                  PRIMARY KEY (`id`) USING BTREE,
                                  INDEX `idx_exam_id`(`exam_id`) USING BTREE,
                                  INDEX `idx_question_id`(`question_id`) USING BTREE,
                                  INDEX `idx_del_flag`(`del_flag`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '试卷试题关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for question
-- ----------------------------
DROP TABLE IF EXISTS `question`;
CREATE TABLE `question`  (
                             `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                             `bank_id` bigint(0) NOT NULL COMMENT '所属题库ID',
                             `title` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '题干富文本',
                             `question_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '题型 SINGLE单选,MULTIPLE多选,JUDGE判断,BLANK填空,SHORT_ANSWER简答,ESSAY论述,CODE代码题,UPLOAD_FILE文件上传,MATCH匹配题',
                             `difficulty` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'medium' COMMENT '难度 easy简单 medium中等 hard困难',
                             `score` decimal(5, 2) NULL COMMENT '题目默认分值',
                             `analysis` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '试题解析富文本',
                             `answer` json NULL COMMENT '参考答案JSON，不同题型结构不同',
                             `create_user` bigint(0) NOT NULL COMMENT '题目创建人ID',
                             `status` tinyint(0) NOT NULL DEFAULT 1 COMMENT '0草稿 1启用 2废弃',
                             `tenant_id` bigint(0) NULL DEFAULT NULL COMMENT '租户ID',
                             `del_flag` tinyint(0) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
                             `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
                             `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
                             `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
                             `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
                             PRIMARY KEY (`id`) USING BTREE,
                             INDEX `idx_bank_id`(`bank_id`) USING BTREE,
                             INDEX `idx_question_type`(`question_type`) USING BTREE,
                             INDEX `idx_create_user`(`create_user`) USING BTREE,
                             INDEX `idx_del_flag`(`del_flag`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '试题主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for question_bank
-- ----------------------------
DROP TABLE IF EXISTS `question_bank`;
CREATE TABLE `question_bank`  (
                                  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                  `bank_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '题库名称',
                                  `bank_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '题库描述',
                                  `creator_id` bigint(0) NOT NULL COMMENT '创建人用户ID',
                                  `visibility` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'private' COMMENT '可见性 private私有 / public公开',
                                  `status` tinyint(0) NOT NULL DEFAULT 1 COMMENT '状态 0草稿 1正常 2归档',
                                  `tenant_id` bigint(0) NULL DEFAULT NULL COMMENT '租户ID，单租户可不用',
                                  `del_flag` tinyint(0) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
                                  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
                                  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
                                  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
                                  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
                                  PRIMARY KEY (`id`) USING BTREE,
                                  INDEX `idx_creator_id`(`creator_id`) USING BTREE,
                                  INDEX `idx_visibility`(`visibility`) USING BTREE,
                                  INDEX `idx_del_flag`(`del_flag`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '题库表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for question_media
-- ----------------------------
DROP TABLE IF EXISTS `question_media`;
CREATE TABLE `question_media`  (
                                   `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                   `question_id` bigint(0) NOT NULL COMMENT '试题ID',
                                   `media_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'media_type:image图片,audio音频,video视频',
                                   `media_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '资源访问地址MinIO',
                                   `media_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '原始文件名',
                                   `sort` int(0) NOT NULL DEFAULT 0 COMMENT '展示顺序',
                                   `tenant_id` bigint(0) NULL DEFAULT NULL COMMENT '租户ID',
                                   `del_flag` tinyint(0) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
                                   `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
                                   `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
                                   PRIMARY KEY (`id`) USING BTREE,
                                   INDEX `idx_question_id`(`question_id`) USING BTREE,
                                   INDEX `idx_del_flag`(`del_flag`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '试题多媒体附件表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for question_option
-- ----------------------------
DROP TABLE IF EXISTS `question_option`;
CREATE TABLE `question_option`  (
                                    `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                    `question_id` bigint(0) NOT NULL COMMENT '试题ID',
                                    `option_key` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '选项标识 A/B/C/D',
                                    `option_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '选项内容富文本',
                                    `sort` int(0) NOT NULL DEFAULT 0 COMMENT '排序号',
                                    `tenant_id` bigint(0) NULL DEFAULT NULL COMMENT '租户ID',
                                    `del_flag` tinyint(0) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
                                    `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
                                    `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
                                    PRIMARY KEY (`id`) USING BTREE,
                                    INDEX `idx_question_id`(`question_id`) USING BTREE,
                                    INDEX `idx_del_flag`(`del_flag`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '试题选项表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for question_tag
-- ----------------------------
DROP TABLE IF EXISTS `question_tag`;
CREATE TABLE `question_tag`  (
                                 `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                 `tag_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标签名称',
                                 `creator_id` bigint(0) NOT NULL COMMENT '创建人ID',
                                 `tenant_id` bigint(0) NULL DEFAULT NULL COMMENT '租户ID',
                                 `del_flag` tinyint(0) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
                                 `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
                                 `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
                                 PRIMARY KEY (`id`) USING BTREE,
                                 UNIQUE INDEX `uk_tag_name`(`tag_name`, `tenant_id`, `del_flag`) USING BTREE,
                                 INDEX `idx_creator_id`(`creator_id`) USING BTREE,
                                 INDEX `idx_del_flag`(`del_flag`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '试题标签表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for question_tag_rel
-- ----------------------------
DROP TABLE IF EXISTS `question_tag_rel`;
CREATE TABLE `question_tag_rel`  (
                                     `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                     `question_id` bigint(0) NOT NULL COMMENT '试题ID',
                                     `tag_id` bigint(0) NOT NULL COMMENT '标签ID',
                                     `tenant_id` bigint(0) NULL DEFAULT NULL COMMENT '租户ID',
                                     `del_flag` tinyint(0) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
                                     `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
                                     PRIMARY KEY (`id`) USING BTREE,
                                     UNIQUE INDEX `uk_question_tag`(`question_id`, `tag_id`, `tenant_id`, `del_flag`) USING BTREE,
                                     INDEX `idx_question_id`(`question_id`) USING BTREE,
                                     INDEX `idx_tag_id`(`tag_id`) USING BTREE,
                                     INDEX `idx_del_flag`(`del_flag`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '试题标签关联表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
