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
('高精度示波器 TDS3054B', '实验设备', 2, '泰克数字荧光示波器，500MHz带宽，4通道，适合电子电路实验。', 'https://example.com/osc.jpg', '工科楼A301', 85000.00, 50.00, 'available', '需提前预约，使用前后需登记，损坏照价赔偿', 32),
('深度学习服务器 RTX4090', '实验设备', 3, '配备RTX4090显卡的高性能服务器，适合深度学习模型训练。', 'https://example.com/server.jpg', '计算机楼B205', 35000.00, 100.00, 'available', '仅限科研使用，禁止挖矿，使用需提前预约', 58),
('《人工智能：一种现代的方法》第4版', '图书资料', 2, '经典AI教材，9成新，适合人工智能入门学习。', 'https://example.com/book.jpg', '图书馆三楼', 128.00, 0.00, 'available', '借阅周期不超过30天，到期可申请续借', 12),
('便携式投影仪', '电子数码', 3, '小米便携式投影仪，适合小组讨论和项目答辩使用。', 'https://example.com/projector.jpg', '行政楼C102', 2999.00, 20.00, 'available', '借用不超过7天，需押学生证', 26),
('小型会议室（可容纳8人）', '场地空间', 2, '配备白板、投影仪、视频会议设备，适合小组讨论。', 'https://example.com/room.jpg', '创新楼D401', 0.00, 0.00, 'available', '需提前2天预约，单次使用不超过4小时', 41);

-- ==================== 知识点模拟数据（算法：拓扑排序学习路径） ====================

-- 大数据分析课程知识点（ID 1~12）
INSERT INTO knowledge_point (id, course_name, point_name, description, content, difficulty, prerequisite_ids, estimated_minutes, sort_order, status) VALUES
(1, '大数据分析', '数据科学导论', '了解数据科学的基本概念、发展历程和应用领域', '数据科学定义、数据科学工作流程、常见应用场景', 1, NULL, 30, 1, 1),
(2, '大数据分析', '统计学基础', '掌握描述性统计和推断性统计的基本方法', '均值、中位数、方差、标准差、假设检验、置信区间', 1, NULL, 45, 2, 1),
(3, '大数据分析', 'Linux系统基础', '熟悉Linux操作系统的基本操作和命令', '文件系统、Shell命令、权限管理、环境配置', 1, NULL, 40, 3, 1),
(4, '大数据分析', 'Python编程基础', '掌握Python编程语言的基本语法和数据结构', '变量与类型、列表/字典/集合、函数与类、文件操作', 2, '1', 60, 4, 1),
(5, '大数据分析', '数据采集', '学习从Web、API和数据库中采集数据的方法', '爬虫基础、API调用、数据库连接、数据导出', 2, '4', 50, 5, 1),
(6, '大数据分析', '数据预处理', '掌握数据清洗、缺失值处理、数据变换等预处理技术', '缺失值处理、异常值检测、标准化/归一化、特征编码', 2, '2,4', 50, 6, 1),
(7, '大数据分析', '数据存储与管理', '了解大数据存储技术，包括HDFS、NoSQL数据库等', 'HDFS原理、HBase、MongoDB、数据仓库概念', 3, '3', 45, 7, 1),
(8, '大数据分析', '数据可视化', '学习使用Matplotlib、Seaborn等工具进行数据可视化', '图表类型选择、Matplotlib基础、Seaborn统计图、交互式可视化', 3, '6,4', 40, 8, 1),
(9, '大数据分析', '数据分析与建模', '掌握使用Pandas和Scikit-learn进行数据分析建模', 'Pandas数据分析、特征工程、回归/分类模型、模型评估', 3, '6,2', 60, 9, 1),
(10, '大数据分析', '机器学习入门', '了解机器学习的基本概念和经典算法', '监督学习、无监督学习、过拟合与欠拟合、交叉验证', 4, '9,4', 55, 10, 1),
(11, '大数据分析', '大数据平台技术', '学习Hadoop、Spark等大数据处理平台的使用', 'Hadoop生态、MapReduce、Spark RDD/DataFrame、流处理', 4, '7,3', 60, 11, 1),
(12, '大数据分析', '深度学习基础', '了解神经网络和深度学习的基本原理', '感知机、多层神经网络、反向传播、CNN/RNN简介', 5, '10', 50, 12, 1)
ON DUPLICATE KEY UPDATE course_name = VALUES(course_name), point_name = VALUES(point_name);

-- 人工智能课程知识点（ID 13~23）
INSERT INTO knowledge_point (id, course_name, point_name, description, content, difficulty, prerequisite_ids, estimated_minutes, sort_order, status) VALUES
(13, '人工智能', '编程基础(Python)', '掌握Python编程语言的基本语法、数据结构与面向对象编程', '变量与类型、列表/字典/集合、函数、类与继承、异常处理', 1, NULL, 60, 1, 1),
(14, '人工智能', '高等数学基础', '复习高等数学的核心概念：极限、导数、积分与级数', '极限与连续、导数与微分、不定积分与定积分、泰勒级数', 1, NULL, 50, 2, 1),
(15, '人工智能', '概率论与数理统计', '掌握概率论基础与常用统计方法', '概率公理、随机变量、分布函数、大数定律、中心极限定理', 1, NULL, 50, 3, 1),
(16, '人工智能', '线性代数', '掌握矩阵运算、向量空间、特征值分解等线性代数核心', '矩阵运算、行列式、向量空间、特征值与特征向量、SVD分解', 2, '14', 60, 4, 1),
(17, '人工智能', '数据结构与算法', '学习常用数据结构和基本算法思想', '数组/链表/树/图、排序与搜索、动态规划、复杂度分析', 2, '13,14', 60, 5, 1),
(18, '人工智能', '机器学习基础', '系统学习经典机器学习算法的原理与实现', '线性回归、逻辑回归、决策树、SVM、聚类、集成学习', 3, '15,16,13', 70, 6, 1),
(19, '人工智能', '数据库与数据挖掘', '学习数据库操作与数据挖掘的常用技术', 'SQL语言、数据仓库、关联规则、聚类分析、异常检测', 3, '17', 50, 7, 1),
(20, '人工智能', '深度学习', '深入学习神经网络与深度学习技术', '多层感知机、CNN、RNN/LSTM、注意力机制、Transformer', 4, '18,16', 70, 8, 1),
(21, '人工智能', '自然语言处理', '学习文本处理与自然语言理解的核心技术', '分词/词性标注、词向量、Seq2Seq、BERT/GPT、文本分类', 4, '20,17', 60, 9, 1),
(22, '人工智能', '计算机视觉', '学习图像处理与计算机视觉的核心算法', '图像滤波、边缘检测、目标检测、图像分割、GAN', 5, '20,16', 60, 10, 1),
(23, '人工智能', '强化学习', '了解强化学习的基本原理与经典算法', 'MDP、价值迭代/策略迭代、Q-Learning、DQN、策略梯度', 5, '18,15', 55, 11, 1)
ON DUPLICATE KEY UPDATE course_name = VALUES(course_name), point_name = VALUES(point_name);

-- ==================== 学习资源模拟数据 ====================

INSERT INTO learning_resource (id, resource_name, resource_type, subject, description, content_url, difficulty_level, publisher_id, status, views, likes) VALUES
(1, 'Python数据科学手册', 'book', '大数据分析', 'Python在数据科学领域的经典入门教材，涵盖NumPy/Pandas/Matplotlib', 'https://example.com/books/python-data-science', '初级', 3, 1, 256, 42),
(2, '大数据技术原理与应用', 'course', '大数据分析', '系统讲解Hadoop、Spark等大数据处理技术', 'https://example.com/courses/bigdata-tech', '中级', 3, 1, 189, 35),
(3, '机器学习实战（基于Scikit-learn）', 'video', '人工智能', '从零开始学习机器学习算法和Scikit-learn框架', 'https://example.com/videos/ml-sklearn', '中级', 2, 1, 312, 58),
(4, '深度学习入门：基于PyTorch', 'video', '人工智能', 'PyTorch框架入门到实践，涵盖CNN/RNN/Transformer', 'https://example.com/videos/deeplearning-pytorch', '高级', 2, 1, 278, 49),
(5, '概率论与数理统计（第5版）', 'book', '人工智能', '经典概率论教材，系统讲解概率分布、统计推断等内容', 'https://example.com/books/probability', '初级', 3, 1, 145, 28)
ON DUPLICATE KEY UPDATE resource_name = VALUES(resource_name), subject = VALUES(subject);

-- ==================== 学习记录模拟数据 ====================

-- student01（user_id=2）已完成大数据分析前6个知识点中的4个
INSERT INTO learning_record (user_id, resource_id, progress, status, last_position, complete_time) VALUES
(2, 1, 100, 'completed', 350, '2026-06-10 14:30:00'),
(2, 2, 60, 'learning', 180, NULL)
ON DUPLICATE KEY UPDATE progress = VALUES(progress), status = VALUES(status);

-- ==================== 项目申请模拟数据 ====================

INSERT INTO project_application (project_id, applicant_id, apply_reason, status, handle_time, reply_message) VALUES
(1, 2, '我对深度学习和推荐系统很感兴趣，希望能参与这个项目积累经验', 'approved', '2026-06-15 10:00:00', '欢迎加入团队，请于本周五参加项目启动会'),
(2, 2, '我有Java和Vue开发经验，可以承担平台核心功能开发工作', 'pending', NULL, NULL)
ON DUPLICATE KEY UPDATE status = VALUES(status);
