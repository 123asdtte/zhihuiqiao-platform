USE zhihuiqiao;
SET NAMES utf8mb4;

-- 初始化角色数据
INSERT INTO sys_role (role_code, role_name, description, sort_order) VALUES
('student', '学生', '在校学生用户', 1),
('teacher', '教师', '教师/科研人员', 2),
('enterprise', '企业', '企业/合作方用户', 3),
('admin', '管理员', '平台管理员', 4)
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);

-- 初始化管理员账号：admin / 123456
INSERT INTO sys_user (username, password, real_name, email, role_type, department, status)
VALUES ('admin', '$2a$10$.KK4BHzkNzVdwobL61hMruSAB/SiUzEcKfA5c9X.ONSLGYP.aPyim', '系统管理员', 'admin@zhihuiqiao.com', 'admin', '信息化办公室', 1)
ON DUPLICATE KEY UPDATE password = VALUES(password), role_type = VALUES(role_type);

-- 初始化示例用户（密码均为 123456）
INSERT INTO sys_user (username, password, real_name, email, role_type, department, major, grade, status) VALUES
('student01', '$2a$10$.KK4BHzkNzVdwobL61hMruSAB/SiUzEcKfA5c9X.ONSLGYP.aPyim', '张同学', 'student01@zhihuiqiao.com', 'student', '计算机学院', '软件工程', '2022级', 1),
('teacher01', '$2a$10$.KK4BHzkNzVdwobL61hMruSAB/SiUzEcKfA5c9X.ONSLGYP.aPyim', '李教授', 'teacher01@zhihuiqiao.com', 'teacher', '计算机学院', '人工智能', NULL, 1),
('enterprise01', '$2a$10$.KK4BHzkNzVdwobL61hMruSAB/SiUzEcKfA5c9X.ONSLGYP.aPyim', '王经理', 'enterprise01@zhihuiqiao.com', 'enterprise', NULL, NULL, NULL, 1)
ON DUPLICATE KEY UPDATE password = VALUES(password), role_type = VALUES(role_type);

-- 科研画像模拟数据
INSERT INTO researcher_profile (user_id, research_directions, skills, publications, project_experience, research_interests, availability, cooperation_intention) VALUES
(2, '人工智能,机器学习,计算机视觉', 'Python,TensorFlow,PyTorch,深度学习', '发表SCI论文5篇，申请专利2项', '主持省级科研项目1项，参与国家自然科学基金项目1项', '智能推荐系统、医学影像分析', '全职', '欢迎对AI感兴趣的学生加入课题组'),
(3, '大数据,云计算,分布式系统', 'Java,Spring Cloud,Hadoop,Spark', '发表EI论文8篇，软著3项', '主持校企合作项目2项，指导学生竞赛获奖多次', '大数据平台、云原生架构', '兼职', '寻找大数据方向的企业合作伙伴')
ON DUPLICATE KEY UPDATE research_directions = VALUES(research_directions);

-- 科研项目模拟数据
INSERT INTO research_project (project_name, project_code, publisher_id, publisher_type, project_type, research_fields, project_description, requirements, expected_outcomes, status, max_members, current_members, start_date, end_date, views) VALUES
('基于深度学习的校园智能推荐系统', 'PRJ2026001', 2, 'teacher', '应用研究', '人工智能,推荐系统', '利用深度学习技术构建面向学生的课程、资源、项目推荐系统，提升学习效率。', '熟悉Python和深度学习框架，有推荐系统相关经验优先', '完成推荐算法原型系统，发表学术论文1篇', 'recruiting', 5, 1, '2026-07-01', '2026-12-31', 128),
('高校科研成果转化智能撮合平台', 'PRJ2026002', 2, 'teacher', '技术开发', '大数据,自然语言处理', '开发一个连接科研人员与企业的成果转化平台，实现需求与成果的自动匹配。', '具备Java或Vue开发能力，了解NLP基础算法', '完成平台核心功能开发，申请软件著作权1项', 'recruiting', 6, 1, '2026-08-01', '2027-02-28', 96),
('校园闲置实验设备共享调度系统', 'PRJ2026003', 3, 'teacher', '技术开发', '物联网,调度算法', '基于物联网技术实现实验设备的在线预约、状态监控和智能调度。', '熟悉嵌入式开发或Web开发，有物联网项目经验优先', '完成设备共享平台开发，接入10台以上实验设备', 'recruiting', 4, 1, '2026-07-15', '2026-11-30', 75),
('面向产教融合的个性化学习路径推荐研究', 'PRJ2026004', 2, 'teacher', '基础研究', '教育大数据,知识图谱', '研究如何基于学生画像和课程知识图谱生成个性化学习路径。', '有教育技术或数据挖掘背景，熟悉知识图谱者优先', '构建学习路径推荐模型，发表核心期刊论文1篇', 'recruiting', 5, 1, '2026-09-01', '2027-06-30', 52),
('企业技术难题众包攻关平台', 'PRJ2026005', 3, 'teacher', '创新创业', '众包,协同创新', '搭建企业技术难题发布、师生揭榜攻关、成果评价的众包平台。', '有创业意愿，具备全栈开发能力或项目管理经验', '完成平台MVP开发，对接3家以上企业', 'recruiting', 8, 1, '2026-08-15', '2027-03-31', 43);

-- 企业需求模拟数据
INSERT INTO enterprise_demand (enterprise_id, demand_title, demand_type, industry_field, demand_description, technical_requirements, budget_range, cooperation_mode, contact_info, status, views) VALUES
(4, '智能制造场景下的缺陷检测算法研发', '技术攻关', '智能制造', '需要开发一套基于计算机视觉的工业产品表面缺陷检测算法，准确率达到95%以上。', '熟悉目标检测、图像分割算法，有工业视觉项目经验', '20-50万', '联合研发', '王经理 13800138000', 'open', 67),
(4, '面向高校学生的AI实训课程开发', '联合研发', '教育培训', '希望与高校合作开发一套面向计算机专业学生的AI实训课程，包含案例和实验平台。', '具备课程设计能力，熟悉PyTorch/TensorFlow', '10-30万', '课程共建', '王经理 13800138000', 'open', 34),
(4, '企业知识图谱构建与智能问答系统', '技术攻关', '知识管理', '需要基于企业内部文档构建知识图谱，并实现智能问答系统。', '熟悉NLP、知识图谱、RAG检索增强技术', '30-80万', '项目外包', '王经理 13800138000', 'open', 28);

-- 闲置资源模拟数据
INSERT INTO idle_resource (resource_name, resource_type, owner_id, description, images, location, original_price, rental_price, status, borrow_rules, views) VALUES
('高精度示波器 TDS3054B', '实验设备', 2, '泰克数字荧光示波器，500MHz带宽，4通道，适合电子电路实验。', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=A%20professional%20digital%20oscilloscope%20on%20a%20clean%20lab%20bench%2C%20electronic%20test%20equipment%2C%20modern%20scientific%20instrument%2C%20product%20photography%20style%2C%20neutral%20background&image_size=landscape_4_3', '工科楼A301', 85000.00, 50.00, 'available', '需提前预约，使用前后需登记，损坏照价赔偿', 32),
('深度学习服务器 RTX4090', '实验设备', 3, '配备RTX4090显卡的高性能服务器，适合深度学习模型训练。', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=A%20high%20performance%20deep%20learning%20server%20workstation%20with%20NVIDIA%20RTX%204090%20GPU%2C%20computer%20hardware%20in%20server%20rack%2C%20blue%20LED%20lights%2C%20tech%20style%20photography&image_size=landscape_4_3', '计算机楼B205', 35000.00, 100.00, 'available', '仅限科研使用，禁止挖矿，使用需提前预约', 58),
('《人工智能：一种现代的方法》第4版', '图书资料', 2, '经典AI教材，9成新，适合人工智能入门学习。', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=A%20stack%20of%20artificial%20intelligence%20textbooks%20on%20a%20wooden%20desk%2C%20modern%20AI%20book%20cover%20design%2C%20clean%20academic%20style%2C%20soft%20natural%20lighting&image_size=portrait_4_3', '图书馆三楼', 128.00, 0.00, 'available', '借阅周期不超过30天，到期可申请续借', 12),
('便携式投影仪', '电子数码', 3, '小米便携式投影仪，适合小组讨论和项目答辩使用。', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=A%20portable%20mini%20projector%20on%20a%20white%20table%2C%20modern%20compact%20digital%20device%2C%20sleek%20design%2C%20product%20photography%20with%20clean%20background&image_size=landscape_4_3', '行政楼C102', 2999.00, 20.00, 'available', '借用不超过7天，需押学生证', 26),
('小型会议室（可容纳8人）', '场地空间', 2, '配备白板、投影仪、视频会议设备，适合小组讨论。', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=A%20modern%20small%20meeting%20room%20for%208%20people%2C%20with%20whiteboard%2C%20conference%20table%20and%20video%20equipment%2C%20bright%20interior%20design%2C%20clean%20office%20space&image_size=landscape_4_3', '创新楼D401', 0.00, 0.00, 'available', '需提前2天预约，单次使用不超过4小时', 41);

-- 学习资源模拟数据
INSERT INTO learning_resource (resource_name, resource_type, subject, description, cover_url, content_url, difficulty_level, publisher_id, status, views, likes) VALUES
('Python 数据分析入门', 'course', '计算机科学', '从零开始学习 Python 数据分析，涵盖 NumPy、Pandas、Matplotlib 等核心库。', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=A%20modern%20online%20course%20cover%20about%20Python%20data%20analysis%2C%20clean%20minimalist%20design%2C%20code%20and%20charts%20elements%2C%20blue%20gradient%20background&image_size=landscape_4_3', 'https://example.com/python-data', '初级', 2, 1, 156, 23),
('深度学习与计算机视觉', 'video', '人工智能', '系统讲解卷积神经网络、目标检测、图像分割等计算机视觉核心技术。', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=A%20modern%20AI%20video%20course%20cover%20about%20deep%20learning%20computer%20vision%2C%20neural%20network%20and%20camera%20elements%2C%20dark%20tech%20style&image_size=landscape_4_3', 'https://example.com/dl-cv', '高级', 2, 1, 89, 15),
('Transformer 架构论文精读', 'paper', '人工智能', '对《Attention Is All You Need》论文进行逐段精读，深入理解 Transformer 架构。', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=Academic%20paper%20cover%20design%20about%20Transformer%20neural%20network%20architecture%2C%20clean%20white%20background%20with%20abstract%20attention%20mechanism%20visualization&image_size=portrait_4_3', 'https://example.com/transformer-paper', '中级', 3, 1, 67, 8),
('机器学习实战指南', 'book', '计算机科学', '面向工程实践的机器学习参考书，包含大量可运行代码示例与项目案例。', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=A%20machine%20learning%20textbook%20cover%20with%20code%20snippets%20and%20algorithm%20diagrams%2C%20modern%20academic%20book%20design%2C%20soft%20gradient&image_size=portrait_4_3', 'https://example.com/ml-book', '中级', 2, 1, 134, 31),
('LaTeX 论文写作工具包', 'tool', '计算机科学', '收集常用 LaTeX 模板、参考文献格式与写作技巧，提升科研论文写作效率。', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=A%20LaTeX%20academic%20writing%20toolkit%20cover%20with%20document%20and%20formula%20elements%2C%20clean%20professional%20design%2C%20academic%20style&image_size=landscape_4_3', 'https://example.com/latex-toolkit', '初级', 3, 1, 45, 5);
