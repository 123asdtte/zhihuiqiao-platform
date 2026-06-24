/*
 Navicat Premium Data Transfer

 Source Server         : root@localhost
 Source Server Type    : MySQL
 Source Server Version : 80017
 Source Host           : localhost:3306
 Source Schema         : zhihuiqiao

 Target Server Type    : MySQL
 Target Server Version : 80017
 File Encoding         : 65001

 Date: 22/06/2026 08:41:30
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for enterprise_demand
-- ----------------------------
DROP TABLE IF EXISTS `enterprise_demand`;
CREATE TABLE `enterprise_demand`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '需求ID',
  `enterprise_id` bigint(20) NOT NULL COMMENT '企业用户ID',
  `demand_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '需求标题',
  `demand_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '需求类型：技术攻关/成果转化/人才招聘/联合研发',
  `industry_field` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '行业领域',
  `demand_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '需求描述',
  `technical_requirements` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '技术要求',
  `budget_range` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '预算范围',
  `cooperation_mode` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '合作方式',
  `contact_info` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系方式',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'open' COMMENT '状态：open/closed/completed',
  `views` int(11) NULL DEFAULT 0,
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  `deleted` tinyint(4) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_enterprise`(`enterprise_id`) USING BTREE,
  FULLTEXT INDEX `ft_title_desc`(`demand_title`, `demand_description`) WITH PARSER `ngram`
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '企业需求表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for idle_resource
-- ----------------------------
DROP TABLE IF EXISTS `idle_resource`;
CREATE TABLE `idle_resource`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '资源ID',
  `resource_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资源名称',
  `resource_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资源类型：实验设备/图书资料/办公用品/电子数码/场地空间/其他',
  `owner_id` bigint(20) NOT NULL COMMENT '所有者ID',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '资源描述',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '图片URL，多个用逗号分隔',
  `location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '存放位置',
  `original_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '原价',
  `rental_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '租赁价格/天',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'available' COMMENT '状态：available/rented/unavailable',
  `borrow_rules` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '借用规则',
  `views` int(11) NULL DEFAULT 0,
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  `deleted` tinyint(4) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_owner`(`owner_id`) USING BTREE,
  INDEX `idx_type_status`(`resource_type`, `status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '闲置资源表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for learning_record
-- ----------------------------
DROP TABLE IF EXISTS `learning_record`;
CREATE TABLE `learning_record`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `resource_id` bigint(20) NOT NULL COMMENT '学习资源ID',
  `progress` int(11) NULL DEFAULT 0 COMMENT '学习进度：0-100',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'learning' COMMENT '状态：learning/completed/favorite',
  `last_position` int(11) NULL DEFAULT 0 COMMENT '上次学习位置（秒/页码）',
  `complete_time` datetime(0) NULL DEFAULT NULL COMMENT '完成时间',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  `deleted` tinyint(4) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_resource`(`user_id`, `resource_id`) USING BTREE,
  INDEX `idx_user`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学习记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for learning_resource
-- ----------------------------
DROP TABLE IF EXISTS `learning_resource`;
CREATE TABLE `learning_resource`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '资源ID',
  `resource_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资源名称',
  `resource_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资源类型：course/video/paper/book/tool',
  `subject` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学科领域',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '资源描述',
  `cover_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面URL',
  `content_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '内容链接',
  `difficulty_level` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '难度：初级/中级/高级',
  `publisher_id` bigint(20) NOT NULL COMMENT '发布人ID',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：0-下架 1-上架',
  `views` int(11) NULL DEFAULT 0,
  `likes` int(11) NULL DEFAULT 0,
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  `deleted` tinyint(4) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_publisher`(`publisher_id`) USING BTREE,
  INDEX `idx_type_subject`(`resource_type`, `subject`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学习资源表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for project_application
-- ----------------------------
DROP TABLE IF EXISTS `project_application`;
CREATE TABLE `project_application`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `applicant_id` bigint(20) NOT NULL COMMENT '申请人ID',
  `apply_reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '申请理由',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'pending' COMMENT '状态：pending/approved/rejected',
  `reply_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '回复消息',
  `handle_time` datetime(0) NULL DEFAULT NULL COMMENT '处理时间',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  `deleted` tinyint(4) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_project_applicant`(`project_id`, `applicant_id`) USING BTREE,
  INDEX `idx_applicant`(`applicant_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '项目申请表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for research_project
-- ----------------------------
DROP TABLE IF EXISTS `research_project`;
CREATE TABLE `research_project`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '项目ID',
  `project_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '项目名称',
  `project_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '项目编号',
  `publisher_id` bigint(20) NOT NULL COMMENT '发布人ID',
  `publisher_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发布人类型：teacher/enterprise',
  `project_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '项目类型：基础研究/应用研究/技术开发/创新创业',
  `research_fields` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '研究领域',
  `project_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '项目简介',
  `requirements` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '成员要求',
  `expected_outcomes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '预期成果',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'recruiting' COMMENT '状态：recruiting/ongoing/completed/closed',
  `max_members` int(11) NULL DEFAULT 5 COMMENT '最大成员数',
  `start_date` date NULL DEFAULT NULL COMMENT '开始日期',
  `end_date` date NULL DEFAULT NULL COMMENT '结束日期',
  `views` int(11) NULL DEFAULT 0 COMMENT '浏览量',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  `deleted` tinyint(4) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_publisher`(`publisher_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  FULLTEXT INDEX `ft_name_desc`(`project_name`, `project_description`) WITH PARSER `ngram`
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '科研项目表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for researcher_profile
-- ----------------------------
DROP TABLE IF EXISTS `researcher_profile`;
CREATE TABLE `researcher_profile`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '画像ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `research_directions` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '研究方向，多个用逗号分隔',
  `skills` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '技能标签，多个用逗号分隔',
  `publications` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '论文/专利/成果',
  `project_experience` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '项目经历',
  `research_interests` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '科研兴趣',
  `availability` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '可投入时间：全职/兼职/课余',
  `cooperation_intention` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '合作意向',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  `deleted` tinyint(4) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '科研画像表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for resource_booking
-- ----------------------------
DROP TABLE IF EXISTS `resource_booking`;
CREATE TABLE `resource_booking`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '预约ID',
  `resource_id` bigint(20) NOT NULL COMMENT '资源ID',
  `borrower_id` bigint(20) NOT NULL COMMENT '借用人ID',
  `start_time` datetime(0) NOT NULL COMMENT '开始时间',
  `end_time` datetime(0) NOT NULL COMMENT '结束时间',
  `purpose` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '用途说明',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'pending' COMMENT '状态：pending/approved/rejected/ongoing/returned/cancelled',
  `return_time` datetime(0) NULL DEFAULT NULL COMMENT '实际归还时间',
  `reply_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '回复消息',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  `deleted` tinyint(4) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_resource`(`resource_id`) USING BTREE,
  INDEX `idx_borrower`(`borrower_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '资源预约表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for resource_transfer_log
-- ----------------------------
DROP TABLE IF EXISTS `resource_transfer_log`;
CREATE TABLE `resource_transfer_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `resource_id` bigint(20) NOT NULL COMMENT '资源ID',
  `booking_id` bigint(20) NULL DEFAULT NULL COMMENT '关联预约ID',
  `from_user_id` bigint(20) NOT NULL COMMENT '转出方ID',
  `to_user_id` bigint(20) NOT NULL COMMENT '转入方ID',
  `transfer_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '流转类型：borrow/return/transfer',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '备注',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_resource`(`resource_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '资源流转记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色编码：student/teacher/enterprise/admin',
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名称',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '角色描述',
  `sort_order` int(11) NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `deleted` tinyint(4) NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_code`(`role_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, 'student', '学生', '在校学生用户', 1, '2026-06-18 15:40:08', '2026-06-18 15:40:08', 0);
INSERT INTO `sys_role` VALUES (2, 'teacher', '教师', '教师/科研人员', 2, '2026-06-18 15:40:08', '2026-06-18 15:40:08', 0);
INSERT INTO `sys_role` VALUES (3, 'enterprise', '企业', '企业/合作方用户', 3, '2026-06-18 15:40:08', '2026-06-18 15:40:08', 0);
INSERT INTO `sys_role` VALUES (4, 'admin', '管理员', '平台管理员', 4, '2026-06-18 15:40:08', '2026-06-18 15:40:08', 0);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录账号',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '加密密码',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `role_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色类型：student/teacher/enterprise/admin',
  `department` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '院系/部门',
  `major` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '专业',
  `grade` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '年级',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '职称/职务',
  `company_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '企业名称',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：0-禁用 1-正常 2-待审核',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `deleted` tinyint(4) NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username`) USING BTREE,
  INDEX `idx_role_type`(`role_type`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', '$2a$10$.KK4BHzkNzVdwobL61hMruSAB/SiUzEcKfA5c9X.ONSLGYP.aPyim', '系统管理员', 'admin@zhihuiqiao.com', NULL, NULL, 'admin', '信息化办公室', NULL, NULL, NULL, NULL, 1, '2026-06-18 15:40:08', '2026-06-18 16:59:34', 0);
INSERT INTO `sys_user` VALUES (2, 'student01', '$2a$10$.KK4BHzkNzVdwobL61hMruSAB/SiUzEcKfA5c9X.ONSLGYP.aPyim', '张同学', 'student01@zhihuiqiao.com', NULL, NULL, 'student', '计算机学院', '软件工程', '2022级', NULL, NULL, 1, '2026-06-18 15:40:08', '2026-06-18 16:59:34', 0);
INSERT INTO `sys_user` VALUES (3, 'teacher01', '$2a$10$.KK4BHzkNzVdwobL61hMruSAB/SiUzEcKfA5c9X.ONSLGYP.aPyim', '李教授', 'teacher01@zhihuiqiao.com', NULL, NULL, 'teacher', '计算机学院', '人工智能', NULL, NULL, NULL, 1, '2026-06-18 15:40:08', '2026-06-18 16:59:34', 0);
INSERT INTO `sys_user` VALUES (4, 'enterprise01', '$2a$10$.KK4BHzkNzVdwobL61hMruSAB/SiUzEcKfA5c9X.ONSLGYP.aPyim', '王经理', 'enterprise01@zhihuiqiao.com', NULL, NULL, 'enterprise', NULL, NULL, NULL, NULL, NULL, 1, '2026-06-18 15:40:08', '2026-06-18 16:59:34', 0);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_role`(`user_id`, `role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户角色关联表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
