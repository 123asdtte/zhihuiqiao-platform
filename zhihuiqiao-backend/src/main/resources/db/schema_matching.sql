-- 科研撮合平台：需求承接、合同、里程碑交付相关数据表

USE zhihuiqiao;

-- 需求承接记录表（揭榜）
CREATE TABLE IF NOT EXISTS demand_bid (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '承接ID',
    demand_id BIGINT NOT NULL COMMENT '关联企业需求ID',
    bidder_id BIGINT NOT NULL COMMENT '承接人ID',
    bidder_type VARCHAR(50) NOT NULL COMMENT '承接人类型：student/teacher',
    bid_content TEXT NOT NULL COMMENT '承接方案',
    expected_duration VARCHAR(100) DEFAULT NULL COMMENT '预计周期',
    expected_budget VARCHAR(100) DEFAULT NULL COMMENT '预计报价',
    status VARCHAR(50) DEFAULT 'pending' COMMENT '状态：pending/approved/rejected/cancelled',
    enterprise_remark TEXT COMMENT '企业备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_demand (demand_id),
    KEY idx_bidder (bidder_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='需求承接记录表';

-- 合作合同表
CREATE TABLE IF NOT EXISTS cooperation_contract (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '合同ID',
    demand_id BIGINT NOT NULL COMMENT '关联需求ID',
    bid_id BIGINT NOT NULL COMMENT '关联承接方案ID',
    party_a_id BIGINT NOT NULL COMMENT '企业方用户ID',
    party_b_id BIGINT NOT NULL COMMENT '承接方用户ID',
    contract_title VARCHAR(200) NOT NULL COMMENT '合同标题',
    contract_content TEXT COMMENT '合同内容',
    amount DECIMAL(12,2) DEFAULT NULL COMMENT '合同金额',
    status VARCHAR(50) DEFAULT 'unsigned' COMMENT '状态：unsigned/signed/completed/terminated',
    sign_time DATETIME DEFAULT NULL COMMENT '签订时间',
    file_url VARCHAR(500) DEFAULT NULL COMMENT '合同附件URL',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_demand (demand_id),
    KEY idx_bid (bid_id),
    KEY idx_party_a (party_a_id),
    KEY idx_party_b (party_b_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='合作合同表';

-- 交付里程碑表
CREATE TABLE IF NOT EXISTS delivery_milestone (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '里程碑ID',
    contract_id BIGINT NOT NULL COMMENT '关联合同ID',
    demand_id BIGINT NOT NULL COMMENT '关联需求ID',
    milestone_name VARCHAR(200) NOT NULL COMMENT '里程碑名称',
    description TEXT COMMENT '交付内容描述',
    due_date DATE DEFAULT NULL COMMENT '计划截止日期',
    deliverable_url VARCHAR(500) DEFAULT NULL COMMENT '交付物附件URL',
    status VARCHAR(50) DEFAULT 'pending' COMMENT '状态：pending/submitted/approved/rejected',
    submit_time DATETIME DEFAULT NULL COMMENT '提交时间',
    approve_time DATETIME DEFAULT NULL COMMENT '验收时间',
    enterprise_remark TEXT COMMENT '企业验收备注',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_contract (contract_id),
    KEY idx_demand (demand_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交付里程碑表';
