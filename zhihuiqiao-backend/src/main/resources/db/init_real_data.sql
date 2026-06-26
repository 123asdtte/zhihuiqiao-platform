USE zhihuiqiao;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ==================== 清理测试与重复数据 ====================

-- 清空业务表数据
TRUNCATE TABLE research_project;
TRUNCATE TABLE enterprise_demand;
TRUNCATE TABLE idle_resource;
TRUNCATE TABLE learning_resource;
TRUNCATE TABLE project_application;
TRUNCATE TABLE resource_booking;
TRUNCATE TABLE resource_transfer_log;
TRUNCATE TABLE researcher_profile;
TRUNCATE TABLE sys_notification;
TRUNCATE TABLE operation_log;

-- 删除测试账号（保留 admin、student01、teacher01、enterprise01 四个正式账号）
DELETE FROM sys_user_role WHERE user_id IN (SELECT id FROM sys_user WHERE username LIKE '%test%' OR username LIKE '%demo%');
DELETE FROM sys_user WHERE username LIKE '%test%' OR username LIKE '%demo%';

SET FOREIGN_KEY_CHECKS = 1;

-- ==================== 科研画像数据 ====================

INSERT INTO researcher_profile (user_id, research_directions, skills, publications, project_experience, research_interests, availability, cooperation_intention) VALUES
(2, '人工智能,机器学习,计算机视觉,推荐系统', 'Python,TensorFlow,PyTorch,OpenCV,数据分析', '本科期间参与省级大创项目1项，发表会议论文1篇', '参与“基于深度学习的校园智能推荐系统”项目，负责数据预处理与模型训练', '智能推荐系统、医学影像分析、自然语言处理', '课余', '希望加入人工智能相关课题组，承担模型开发与实验工作'),
(3, '大数据,云计算,自然语言处理,知识图谱', 'Java,Spring Cloud,Hadoop,Spark,MySQL,Elasticsearch', '发表SCI/EI论文10余篇，主持省级科研项目2项，申请发明专利3项', '主持校企合作项目3项，指导学生获国家级竞赛奖项2项', '大数据平台、智能信息检索、产学研合作', '全职', '欢迎对大数据与人工智能感兴趣的学生加入课题组，同时寻找企业合作伙伴');

-- ==================== 科研项目数据 ====================
-- 发布人均为 teacher01（id=3），符合教师/管理员才可发布科研项目的权限规则

INSERT INTO research_project (project_name, project_code, publisher_id, publisher_type, publisher_name, project_type, research_fields, project_description, requirements, expected_outcomes, status, max_members, current_members, start_date, end_date, views) VALUES
('基于深度学习的校园智能推荐系统', 'PRJ2026001', 3, 'teacher', '李教授', '应用研究', '人工智能,推荐系统,教育大数据', '利用深度学习技术构建面向高校学生的课程、资源、科研项目推荐系统，提升学习与研究效率。项目将采集学生行为数据，构建用户画像，并采用协同过滤与深度学习混合模型实现精准推荐。', '熟悉Python编程，了解机器学习基础，有深度学习框架（PyTorch/TensorFlow）使用经验者优先', '完成推荐算法原型系统，发表核心期刊或会议论文1篇，申请软件著作权1项', 'recruiting', 5, 1, '2026-07-01', '2026-12-31', 128),
('高校科研成果转化智能撮合平台', 'PRJ2026002', 3, 'teacher', '李教授', '技术开发', '大数据,自然语言处理,知识图谱', '开发一个连接高校科研人员与企业技术需求的成果转化平台，实现科研成果与企业需求的自动匹配、智能推荐与在线撮合。', '具备Java或Vue全栈开发能力，了解NLP基础算法，有推荐系统或搜索引擎开发经验者优先', '完成平台核心功能开发与上线运行，申请软件著作权1项，对接3家以上企业用户', 'recruiting', 6, 1, '2026-08-01', '2027-02-28', 96),
('校园闲置实验设备共享调度系统', 'PRJ2026003', 3, 'teacher', '李教授', '技术开发', '物联网,调度算法,Web开发', '基于物联网技术实现实验设备的在线预约、状态监控和智能调度，提高设备利用率，降低重复购置成本。', '熟悉嵌入式开发或Web开发，了解MQTT、HTTP等通信协议，有物联网项目经验优先', '完成设备共享平台开发，接入10台以上实验设备，形成可推广的校园应用案例', 'recruiting', 4, 1, '2026-07-15', '2026-11-30', 75),
('面向产教融合的个性化学习路径推荐研究', 'PRJ2026004', 3, 'teacher', '李教授', '基础研究', '教育大数据,知识图谱,学习分析', '研究如何基于学生画像、课程知识图谱与职业能力模型，生成面向产教融合的个性化学习路径。', '有教育技术、数据挖掘或知识图谱背景，熟悉Python数据处理与图数据库者优先', '构建学习路径推荐模型，发表核心期刊论文1篇，形成可验证的教学改进方案', 'recruiting', 5, 1, '2026-09-01', '2027-06-30', 52),
('企业技术难题众包攻关平台', 'PRJ2026005', 3, 'teacher', '李教授', '创新创业', '众包,协同创新,平台运营', '搭建企业技术难题发布、师生揭榜攻关、成果评价与利益分配的众包平台，促进校企协同创新。', '有创业意愿，具备全栈开发能力或项目管理经验，对众包与开源社区有了解者优先', '完成平台MVP开发与试运营，对接3家以上企业，形成可持续的商业模式', 'recruiting', 8, 1, '2026-08-15', '2027-03-31', 43),
('基于多模态大模型的智慧教学助手', 'PRJ2026006', 3, 'teacher', '李教授', '应用研究', '大模型,多模态学习,智慧教育', '研究基于多模态大模型（文本、图像、语音）构建智慧教学助手，实现自动答疑、作业批改与学习资源推荐。', '熟悉大模型API调用与Prompt工程，了解多模态数据处理，有相关项目经验者优先', '完成智慧教学助手原型系统，开展课堂应用实验，发表高水平论文1篇', 'recruiting', 5, 1, '2026-10-01', '2027-04-30', 38),
('面向智能制造的工业视觉检测系统', 'PRJ2026007', 3, 'teacher', '李教授', '技术开发', '计算机视觉,智能制造,缺陷检测', '与企业合作开发面向智能制造场景的工业视觉检测系统，实现产品表面缺陷的自动识别与分类。', '熟悉目标检测、图像分割算法，有工业视觉项目经验或竞赛经历者优先', '完成工业视觉检测系统部署，准确率≥95%，形成校企合作项目案例', 'ongoing', 4, 3, '2026-04-01', '2026-12-31', 87),
('新能源电池管理系统安全评估研究', 'PRJ2026008', 3, 'teacher', '李教授', '基础研究', '新能源,电池安全,故障诊断', '针对新能源电池管理系统开展安全评估与故障诊断研究，建立电池状态估计与热失控预警模型。', '有电气工程、控制理论或数据挖掘背景，熟悉Python/MATLAB仿真工具者优先', '构建电池安全评估模型，发表SCI/EI论文1篇，申请发明专利1项', 'recruiting', 4, 1, '2026-09-15', '2027-09-14', 29);

-- ==================== 企业需求数据 ====================
-- 发布人均为 enterprise01（id=4），符合企业/管理员才可发布企业需求的权限规则

INSERT INTO enterprise_demand (enterprise_id, demand_title, demand_type, industry_field, demand_description, technical_requirements, budget_range, cooperation_mode, contact_info, status, views) VALUES
(4, '智能制造场景下的缺陷检测算法研发', '技术攻关', '智能制造', '我司主要从事精密零部件加工，希望开发一套基于计算机视觉的工业产品表面缺陷检测算法，实现对划痕、凹坑、裂纹等缺陷的自动识别，准确率达到95%以上，并支持产线实时检测。', '熟悉目标检测、图像分割算法，有工业视觉项目经验；了解YOLO、ResNet、U-Net等模型；具备模型部署与优化能力', '20-50万', '联合研发', '王经理 13800138000', 'open', 67),
(4, '面向高校学生的AI实训课程开发', '联合研发', '教育培训', '希望与高校合作开发一套面向计算机、人工智能专业学生的AI实训课程体系，包含理论讲解、案例实战与在线实验平台，提升学生的工程实践能力。', '具备课程设计能力，熟悉PyTorch/TensorFlow；有高校教学经验或企业培训经验者优先', '10-30万', '课程共建', '王经理 13800138000', 'open', 34),
(4, '企业知识图谱构建与智能问答系统', '技术攻关', '知识管理', '需要基于企业内部技术文档、产品手册与项目报告构建知识图谱，并开发智能问答系统，支持自然语言检索与知识推理。', '熟悉NLP、知识图谱、RAG检索增强技术；了解Neo4j、Elasticsearch等工具；有大模型应用开发经验者优先', '30-80万', '项目外包', '王经理 13800138000', 'open', 28),
(4, '基于数字孪生的生产线仿真优化平台', '技术攻关', '数字孪生', '计划建设基于数字孪生技术的生产线仿真优化平台，实现生产过程可视化、瓶颈分析与工艺参数优化，提升生产效率与柔性。', '熟悉数字孪生、离散事件仿真；了解Unity/Unreal或WebGL可视化；有工业互联网项目经验者优先', '50-100万', '联合研发', '王经理 13800138000', 'open', 21),
(4, '跨境电商智能客服系统研发', '技术开发', '电子商务', '需要开发一套面向跨境电商业务的智能客服系统，支持多语言自动回复、订单查询、售后处理与情感分析，降低人工客服成本。', '熟悉自然语言处理、对话系统、多语言模型；有客服系统或聊天机器人开发经验者优先', '15-40万', '项目外包', '王经理 13800138000', 'open', 19),
(4, '智慧农业大棚环境监控系统', '联合研发', '智慧农业', '希望联合开发智慧农业大棚环境监控系统，实时采集温湿度、光照、土壤墒情等数据，实现环境调控与预警，助力农业数字化转型。', '熟悉物联网传感器、嵌入式开发、云平台；有农业信息化或环境监测项目经验者优先', '10-25万', '联合研发', '王经理 13800138000', 'open', 15),
(4, '医疗器械图像处理与辅助诊断算法', '技术攻关', '医疗健康', '需要研发面向医学影像的图像处理与辅助诊断算法，支持X光、CT等影像的病灶检测与分类，辅助医生提升诊断效率。', '熟悉医学图像处理、深度学习；了解DICOM标准与医学影像数据规范；有相关项目或竞赛经验者优先', '40-100万', '联合研发', '王经理 13800138000', 'open', 12);

-- ==================== 闲置资源数据 ====================

INSERT INTO idle_resource (resource_name, resource_type, owner_id, description, images, location, original_price, rental_price, status, borrow_rules, views) VALUES
('高精度示波器 TDS3054B', '实验设备', 3, '泰克数字荧光示波器，500MHz带宽，4通道，适合电子电路、信号处理等实验使用。设备状态良好，配件齐全。', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=A%20professional%20digital%20oscilloscope%20on%20a%20clean%20lab%20bench%2C%20electronic%20test%20equipment%2C%20modern%20scientific%20instrument%2C%20product%20photography%20style%2C%20neutral%20background&image_size=landscape_4_3', '工科楼A301', 85000.00, 50.00, 'available', '需提前预约，使用前后需登记，损坏照价赔偿，单次借用不超过7天', 32),
('深度学习服务器 RTX4090', '实验设备', 3, '配备NVIDIA RTX 4090显卡的高性能服务器，适合深度学习模型训练、大模型微调与科学计算。', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=A%20high%20performance%20deep%20learning%20server%20workstation%20with%20NVIDIA%20RTX%204090%20GPU%2C%20computer%20hardware%20in%20server%20rack%2C%20blue%20LED%20lights%2C%20tech%20style%20photography&image_size=landscape_4_3', '计算机楼B205', 35000.00, 100.00, 'available', '仅限科研使用，禁止挖矿，使用需提前预约，按小时计费', 58),
('《人工智能：一种现代的方法》第4版', '图书资料', 3, '经典AI教材，9成新，内容涵盖搜索、知识表示、机器学习、自然语言处理等，适合人工智能入门与复习。', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=A%20stack%20of%20artificial%20intelligence%20textbooks%20on%20a%20wooden%20desk%2C%20modern%20AI%20book%20cover%20design%2C%20clean%20academic%20style%2C%20soft%20natural%20lighting&image_size=portrait_4_3', '图书馆三楼', 128.00, 0.00, 'available', '借阅周期不超过30天，到期可申请续借，需爱护书籍', 12),
('便携式投影仪', '电子数码', 2, '小米便携式投影仪，画质清晰，适合小组讨论、项目答辩与小型会议使用。', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=A%20portable%20mini%20projector%20on%20a%20white%20table%2C%20modern%20compact%20digital%20device%2C%20sleek%20design%2C%20product%20photography%20with%20clean%20background&image_size=landscape_4_3', '行政楼C102', 2999.00, 20.00, 'available', '借用不超过7天，需押学生证，归还时确认设备完好', 26),
('小型会议室（可容纳8人）', '场地空间', 3, '配备白板、投影仪、视频会议设备，环境安静，适合小组讨论、课题研讨与项目汇报。', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=A%20modern%20small%20meeting%20room%20for%208%20people%2C%20with%20whiteboard%2C%20conference%20table%20and%20video%20equipment%2C%20bright%20interior%20design%2C%20clean%20office%20space&image_size=landscape_4_3', '创新楼D401', 0.00, 0.00, 'available', '需提前2天预约，单次使用不超过4小时，使用完毕请清理现场', 41),
('3D 打印机 Ultimaker S5', '实验设备', 3, '工业级FDM 3D打印机，双喷头，支持多种材料，适合机械结构设计验证、创意原型制作。', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=A%20professional%20industrial%203D%20printer%20on%20a%20lab%20workbench%2C%20dual%20extrusion%20FDM%20printer%2C%20modern%20maker%20space%20environment%2C%20clean%20product%20photography&image_size=landscape_4_3', '工程训练中心E102', 28000.00, 80.00, 'available', '需提前3天预约，提供STL文件，按打印时长计费，耗材另计', 19),
('无人机测绘套装 DJI Mavic 3', '电子数码', 2, '大疆Mavic 3无人机套装，含遥控器、备用电池与收纳包，适合航拍、测绘与户外数据采集。', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=A%20DJI%20Mavic%203%20drone%20with%20remote%20controller%20and%20batteries%20on%20a%20clean%20surface%2C%20professional%20aerial%20photography%20equipment%2C%20product%20photography%20style&image_size=landscape_4_3', '地理信息实验室F305', 13888.00, 150.00, 'available', '借用需持无人机操作证，仅限校内合规空域使用，损坏按市场价赔偿', 33),
('统计学经典教材合集', '图书资料', 2, '包含《统计学导论》《应用回归分析》《多元统计分析》等经典教材，适合统计学专业学习与考研复习。', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=A%20stack%20of%20statistics%20textbooks%20on%20a%20wooden%20desk%2C%20academic%20book%20cover%20design%20with%20charts%20and%20graphs%2C%20clean%20study%20environment&image_size=portrait_4_3', '图书馆五楼', 245.00, 0.00, 'available', '借阅周期不超过30天，可续借一次', 8),
('高性能计算工作站', '实验设备', 3, '配备64核CPU与128GB内存的高性能计算工作站，适合大规模数据处理、仿真计算与编译构建。', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=A%20high%20performance%20computing%20workstation%20with%20multiple%20monitors%20in%20a%20modern%20lab%2C%20powerful%20computer%20hardware%2C%20tech%20style%20photography&image_size=landscape_4_3', '计算机楼B208', 42000.00, 60.00, 'available', '需提前预约，禁止用于非科研用途，按小时计费', 22);

-- ==================== 学习资源数据 ====================

INSERT INTO learning_resource (resource_name, resource_type, subject, description, cover_url, content_url, difficulty_level, publisher_id, status, views, likes) VALUES
('Python 数据分析入门', 'course', '计算机科学', '从零开始学习 Python 数据分析，涵盖 NumPy、Pandas、Matplotlib 等核心库，配合真实案例进行实战演练。', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=A%20modern%20online%20course%20cover%20about%20Python%20data%20analysis%2C%20clean%20minimalist%20design%2C%20code%20and%20charts%20elements%2C%20blue%20gradient%20background&image_size=landscape_4_3', 'https://example.com/python-data', '初级', 3, 1, 156, 23),
('深度学习与计算机视觉', 'video', '人工智能', '系统讲解卷积神经网络、目标检测、图像分割等计算机视觉核心技术，适合有一定机器学习基础的学习者。', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=A%20modern%20AI%20video%20course%20cover%20about%20deep%20learning%20computer%20vision%2C%20neural%20network%20and%20camera%20elements%2C%20dark%20tech%20style&image_size=landscape_4_3', 'https://example.com/dl-cv', '高级', 3, 1, 89, 15),
('Transformer 架构论文精读', 'paper', '人工智能', '对《Attention Is All You Need》论文进行逐段精读，深入理解 Transformer 架构、自注意力机制与位置编码。', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=Academic%20paper%20cover%20design%20about%20Transformer%20neural%20network%20architecture%2C%20clean%20white%20background%20with%20abstract%20attention%20mechanism%20visualization&image_size=portrait_4_3', 'https://example.com/transformer-paper', '中级', 3, 1, 67, 8),
('机器学习实战指南', 'book', '计算机科学', '面向工程实践的机器学习参考书，包含大量可运行代码示例与项目案例，覆盖分类、回归、聚类、降维等经典任务。', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=A%20machine%20learning%20textbook%20cover%20with%20code%20snippets%20and%20algorithm%20diagrams%2C%20modern%20academic%20book%20design%2C%20soft%20gradient&image_size=portrait_4_3', 'https://example.com/ml-book', '中级', 3, 1, 134, 31),
('LaTeX 论文写作工具包', 'tool', '计算机科学', '收集常用 LaTeX 模板、参考文献格式与写作技巧，帮助科研人员提升论文写作效率与排版质量。', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=A%20LaTeX%20academic%20writing%20toolkit%20cover%20with%20document%20and%20formula%20elements%2C%20clean%20professional%20design%2C%20academic%20style&image_size=landscape_4_3', 'https://example.com/latex-toolkit', '初级', 3, 1, 45, 5),
('线性代数及其应用', 'video', '数学', '系统讲解向量、矩阵、特征值分解、奇异值分解等线性代数核心内容，并结合机器学习中的应用案例。', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=A%20modern%20mathematics%20video%20course%20cover%20about%20linear%20algebra%2C%20matrices%20vectors%20and%20geometric%20shapes%2C%20clean%20academic%20design%2C%20blue%20gradient&image_size=landscape_4_3', 'https://example.com/linear-algebra', '中级', 3, 1, 78, 12),
('科研论文写作与发表', 'course', '科研方法', '从选题、文献综述、实验设计到论文撰写与投稿，系统讲解科研论文写作与发表的全流程方法与技巧。', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=A%20scientific%20writing%20course%20cover%20with%20research%20paper%2C%20pen%20and%20laptop%20on%20academic%20desk%2C%20clean%20professional%20design%2C%20warm%20lighting&image_size=landscape_4_3', 'https://example.com/paper-writing', '初级', 3, 1, 112, 18),
('创新创业实战案例', 'video', '创新创业', '精选大学生创新创业典型案例，分析商业模式、团队组建、融资路演与产品迭代的关键要点。', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=A%20startup%20entrepreneurship%20video%20course%20cover%20with%20business%20team%20collaboration%2C%20modern%20office%20scene%2C%20bright%20and%20energetic%20design&image_size=landscape_4_3', 'https://example.com/innovation-case', '初级', 3, 1, 65, 9),
('英语口语与学术报告', 'course', '语言学习', '提升学术英语口语表达能力，涵盖学术会议报告、论文答辩、国际交流等场景的常用表达与技巧。', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=An%20English%20speaking%20and%20academic%20presentation%20course%20cover%2C%20speaker%20at%20podium%20with%20audience%2C%20clean%20modern%20design%2C%20global%20communication%20theme&image_size=landscape_4_3', 'https://example.com/english-presentation', '中级', 3, 1, 54, 7);

-- ==================== 项目申请数据 ====================

INSERT INTO project_application (project_id, applicant_id, apply_reason, status, reply_message, handle_time, create_time) VALUES
(1, 2, '我对推荐系统非常感兴趣，熟悉Python和PyTorch，曾参与过Kaggle推荐竞赛。希望加入项目组负责模型训练与评估工作。', 'approved', '欢迎加入，请尽快联系导师确认分工。', '2026-06-20 10:30:00', '2026-06-18 09:00:00'),
(2, 2, '我对自然语言处理和知识图谱很感兴趣，具备一定的Java后端开发经验，希望参与成果转化平台的研发。', 'pending', NULL, NULL, '2026-06-25 14:20:00'),
(6, 2, '我对大模型应用开发有浓厚兴趣，了解Prompt工程和LangChain框架，希望能参与智慧教学助手的研发。', 'pending', NULL, NULL, '2026-06-26 11:00:00'),
(7, 2, '我熟悉YOLO系列目标检测算法，曾在企业实习参与过视觉检测项目，希望加入项目组。', 'approved', '你的背景很匹配，欢迎加入工业视觉检测项目组。', '2026-05-10 16:00:00', '2026-05-08 10:00:00');

-- ==================== 资源预约数据 ====================

INSERT INTO resource_booking (resource_id, borrower_id, start_time, end_time, purpose, status, return_time, reply_message, create_time) VALUES
(1, 2, '2026-06-28 09:00:00', '2026-07-02 18:00:00', '用于电子电路课程实验中的信号采集与分析', 'approved', NULL, '已批准，请按时到工科楼A301领取设备。', '2026-06-20 10:00:00'),
(2, 2, '2026-06-30 08:00:00', '2026-07-05 20:00:00', '用于深度学习课程大作业的模型训练', 'pending', NULL, NULL, '2026-06-26 15:30:00'),
(5, 2, '2026-07-01 14:00:00', '2026-07-01 18:00:00', '用于课题组项目进度汇报与讨论', 'approved', NULL, '已批准，请提前10分钟到场准备。', '2026-06-22 09:00:00');
