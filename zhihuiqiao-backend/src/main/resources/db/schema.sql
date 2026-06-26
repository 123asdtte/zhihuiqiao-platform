-- 创建数据库
CREATE DATABASE IF NOT EXISTS zhihuiqiao
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE zhihuiqiao;

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码：student/teacher/enterprise/admin',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    description VARCHAR(200) DEFAULT NULL COMMENT '角色描述',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '登录账号',
    password VARCHAR(100) NOT NULL COMMENT '加密密码',
    real_name VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    avatar VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    role_type VARCHAR(50) NOT NULL COMMENT '角色类型：student/teacher/enterprise/admin',
    department VARCHAR(100) DEFAULT NULL COMMENT '院系/部门',
    major VARCHAR(100) DEFAULT NULL COMMENT '专业',
    grade VARCHAR(50) DEFAULT NULL COMMENT '年级',
    title VARCHAR(100) DEFAULT NULL COMMENT '职称/职务',
    company_name VARCHAR(200) DEFAULT NULL COMMENT '企业名称',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-正常 2-待审核',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_role_type (role_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 科研画像表
CREATE TABLE IF NOT EXISTS researcher_profile (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '画像ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    research_directions TEXT COMMENT '研究方向，多个用逗号分隔',
    skills TEXT COMMENT '技能标签，多个用逗号分隔',
    publications TEXT COMMENT '论文/专利/成果',
    project_experience TEXT COMMENT '项目经历',
    research_interests TEXT COMMENT '科研兴趣',
    availability VARCHAR(50) DEFAULT NULL COMMENT '可投入时间：全职/兼职/课余',
    cooperation_intention TEXT COMMENT '合作意向',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='科研画像表';

-- 科研项目表
DROP TABLE IF EXISTS research_project;
CREATE TABLE IF NOT EXISTS research_project (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '项目ID',
    project_name VARCHAR(200) NOT NULL COMMENT '项目名称',
    project_code VARCHAR(100) DEFAULT NULL COMMENT '项目编号',
    publisher_id BIGINT NOT NULL COMMENT '发布人ID',
    publisher_type VARCHAR(50) NOT NULL COMMENT '发布人类型：teacher/enterprise',
    publisher_name VARCHAR(50) DEFAULT NULL COMMENT '发布人姓名',
    project_type VARCHAR(50) DEFAULT NULL COMMENT '项目类型：基础研究/应用研究/技术开发/创新创业',
    research_fields TEXT COMMENT '研究领域',
    project_description TEXT COMMENT '项目简介',
    requirements TEXT COMMENT '成员要求',
    expected_outcomes TEXT COMMENT '预期成果',
    status VARCHAR(50) DEFAULT 'recruiting' COMMENT '状态：recruiting/ongoing/completed/closed',
    max_members INT DEFAULT 5 COMMENT '最大成员数',
    current_members INT DEFAULT 1 COMMENT '当前成员数',
    start_date DATE DEFAULT NULL COMMENT '开始日期',
    end_date DATE DEFAULT NULL COMMENT '结束日期',
    views INT DEFAULT 0 COMMENT '浏览量',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_publisher (publisher_id),
    KEY idx_status (status),
    FULLTEXT KEY ft_name_desc (project_name, project_description) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='科研项目表';

-- 项目申请表
CREATE TABLE IF NOT EXISTS project_application (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '申请ID',
    project_id BIGINT NOT NULL COMMENT '项目ID',
    applicant_id BIGINT NOT NULL COMMENT '申请人ID',
    apply_reason TEXT COMMENT '申请理由',
    status VARCHAR(50) DEFAULT 'pending' COMMENT '状态：pending/approved/rejected',
    reply_message TEXT COMMENT '回复消息',
    handle_time DATETIME DEFAULT NULL COMMENT '处理时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_project_applicant (project_id, applicant_id),
    KEY idx_applicant (applicant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目申请表';

-- 企业需求表
CREATE TABLE IF NOT EXISTS enterprise_demand (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '需求ID',
    enterprise_id BIGINT NOT NULL COMMENT '企业用户ID',
    demand_title VARCHAR(200) NOT NULL COMMENT '需求标题',
    demand_type VARCHAR(50) DEFAULT NULL COMMENT '需求类型：技术攻关/成果转化/人才招聘/联合研发',
    industry_field VARCHAR(100) DEFAULT NULL COMMENT '行业领域',
    demand_description TEXT COMMENT '需求描述',
    technical_requirements TEXT COMMENT '技术要求',
    budget_range VARCHAR(100) DEFAULT NULL COMMENT '预算范围',
    cooperation_mode VARCHAR(100) DEFAULT NULL COMMENT '合作方式',
    contact_info VARCHAR(200) DEFAULT NULL COMMENT '联系方式',
    status VARCHAR(50) DEFAULT 'open' COMMENT '状态：open/closed/completed',
    views INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_enterprise (enterprise_id),
    FULLTEXT KEY ft_title_desc (demand_title, demand_description) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='企业需求表';

-- 闲置资源表
CREATE TABLE IF NOT EXISTS idle_resource (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '资源ID',
    resource_name VARCHAR(200) NOT NULL COMMENT '资源名称',
    resource_type VARCHAR(50) NOT NULL COMMENT '资源类型：实验设备/图书资料/办公用品/电子数码/场地空间/其他',
    owner_id BIGINT NOT NULL COMMENT '所有者ID',
    description TEXT COMMENT '资源描述',
    images TEXT COMMENT '图片URL，多个用逗号分隔',
    location VARCHAR(200) DEFAULT NULL COMMENT '存放位置',
    original_price DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
    rental_price DECIMAL(10,2) DEFAULT NULL COMMENT '租赁价格/天',
    status VARCHAR(50) DEFAULT 'available' COMMENT '状态：available/rented/unavailable',
    borrow_rules TEXT COMMENT '借用规则',
    views INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_owner (owner_id),
    KEY idx_type_status (resource_type, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='闲置资源表';

-- 资源预约表
CREATE TABLE IF NOT EXISTS resource_booking (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '预约ID',
    resource_id BIGINT NOT NULL COMMENT '资源ID',
    borrower_id BIGINT NOT NULL COMMENT '借用人ID',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    purpose TEXT COMMENT '用途说明',
    status VARCHAR(50) DEFAULT 'pending' COMMENT '状态：pending/approved/rejected/ongoing/returned/cancelled',
    return_time DATETIME DEFAULT NULL COMMENT '实际归还时间',
    reply_message TEXT COMMENT '回复消息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_resource (resource_id),
    KEY idx_borrower (borrower_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资源预约表';

-- 资源流转记录表
CREATE TABLE IF NOT EXISTS resource_transfer_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    resource_id BIGINT NOT NULL COMMENT '资源ID',
    booking_id BIGINT DEFAULT NULL COMMENT '关联预约ID',
    from_user_id BIGINT NOT NULL COMMENT '转出方ID',
    to_user_id BIGINT NOT NULL COMMENT '转入方ID',
    transfer_type VARCHAR(50) NOT NULL COMMENT '流转类型：borrow/return/transfer',
    remark TEXT COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_resource (resource_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资源流转记录表';

-- 学习资源表
CREATE TABLE IF NOT EXISTS learning_resource (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '资源ID',
    resource_name VARCHAR(200) NOT NULL COMMENT '资源名称',
    resource_type VARCHAR(50) NOT NULL COMMENT '资源类型：course/video/paper/book/tool',
    subject VARCHAR(100) DEFAULT NULL COMMENT '学科领域',
    description TEXT COMMENT '资源描述',
    cover_url VARCHAR(500) DEFAULT NULL COMMENT '封面URL',
    content_url VARCHAR(500) DEFAULT NULL COMMENT '内容链接',
    difficulty_level VARCHAR(50) DEFAULT NULL COMMENT '难度：初级/中级/高级',
    publisher_id BIGINT NOT NULL COMMENT '发布人ID',
    status TINYINT DEFAULT 1 COMMENT '状态：0-下架 1-上架',
    views INT DEFAULT 0,
    likes INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_publisher (publisher_id),
    KEY idx_type_subject (resource_type, subject)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习资源表';

-- 学习记录表
CREATE TABLE IF NOT EXISTS learning_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    resource_id BIGINT NOT NULL COMMENT '学习资源ID',
    progress INT DEFAULT 0 COMMENT '学习进度：0-100',
    status VARCHAR(50) DEFAULT 'learning' COMMENT '状态：learning/completed/favorite',
    last_position INT DEFAULT 0 COMMENT '上次学习位置（秒/页码）',
    complete_time DATETIME DEFAULT NULL COMMENT '完成时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_resource (user_id, resource_id),
    KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习记录表';

-- 消息通知表
CREATE TABLE IF NOT EXISTS sys_notification (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    user_id BIGINT NOT NULL COMMENT '接收用户ID',
    title VARCHAR(200) NOT NULL COMMENT '通知标题',
    content TEXT COMMENT '通知内容',
    type VARCHAR(50) DEFAULT 'system' COMMENT '通知类型：system/project/application/resource/booking/learning',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读：0-未读 1-已读',
    related_id BIGINT DEFAULT NULL COMMENT '关联业务ID',
    related_type VARCHAR(50) DEFAULT NULL COMMENT '关联业务类型',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_user_read (user_id, is_read),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息通知表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT DEFAULT NULL COMMENT '操作用户ID',
    username VARCHAR(100) DEFAULT NULL COMMENT '操作用户名',
    role_type VARCHAR(50) DEFAULT NULL COMMENT '操作用户角色',
    module VARCHAR(100) NOT NULL COMMENT '操作模块',
    operation VARCHAR(200) NOT NULL COMMENT '操作描述',
    method VARCHAR(20) DEFAULT NULL COMMENT '请求方法 GET/POST/PUT/DELETE',
    request_url VARCHAR(500) DEFAULT NULL COMMENT '请求URL',
    request_params TEXT COMMENT '请求参数',
    response_code INT DEFAULT NULL COMMENT '响应状态码',
    ip_address VARCHAR(100) DEFAULT NULL COMMENT '操作IP地址',
    user_agent VARCHAR(500) DEFAULT NULL COMMENT '浏览器UA',
    execution_time INT DEFAULT 0 COMMENT '执行耗时(毫秒)',
    status TINYINT DEFAULT 1 COMMENT '操作状态：0-失败 1-成功',
    error_msg TEXT COMMENT '错误信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_module (module),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';
