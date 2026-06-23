package com.zhihuiqiao.algorithm.recommendation;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhihuiqiao.entity.EnterpriseDemand;
import com.zhihuiqiao.entity.ResearchProject;
import com.zhihuiqiao.entity.ResearcherProfile;
import com.zhihuiqiao.entity.SysUser;
import com.zhihuiqiao.service.EnterpriseDemandService;
import com.zhihuiqiao.service.ResearchProjectService;
import com.zhihuiqiao.service.ResearcherProfileService;
import com.zhihuiqiao.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 科研项目智能推荐服务
 * 
 * 基于 TF-IDF + Cosine Similarity 实现项目与学生画像的智能匹配
 * 核心流程：
 * 1. 构建学生科研画像的文本特征
 * 2. 计算学生画像与每个项目的文本相似度
 * 3. 按相似度排序，返回 Top-N 推荐结果
 */
@Service
@RequiredArgsConstructor
public class ProjectRecommendService {

    private final ResearcherProfileService researcherProfileService;
    private final ResearchProjectService researchProjectService;
    private final EnterpriseDemandService enterpriseDemandService;
    private final SysUserService sysUserService;

    /**
     * 为学生推荐最匹配的科研项目
     *
     * @param userId  学生用户ID
     * @param topN    返回前 N 个推荐结果
     * @return 带相似度分数的推荐项目列表
     */
    public List<RecommendedProject> recommendProjectsForStudent(Long userId, int topN) {
        // 1. 获取学生科研画像
        LambdaQueryWrapper<ResearcherProfile> profileQuery = new LambdaQueryWrapper<>();
        profileQuery.eq(ResearcherProfile::getUserId, userId);
        ResearcherProfile profile = researcherProfileService.getOne(profileQuery);

        if (profile == null) {
            // 没有科研画像，按默认排序返回项目
            return getDefaultProjects(topN);
        }

        // 2. 构建学生画像文本
        String studentProfileText = buildProfileText(profile);

        // 3. 获取所有招募中的科研项目
        LambdaQueryWrapper<ResearchProject> projectQuery = new LambdaQueryWrapper<>();
        projectQuery.eq(ResearchProject::getStatus, "recruiting");
        projectQuery.orderByDesc(ResearchProject::getCreateTime);
        List<ResearchProject> projects = researchProjectService.list(projectQuery);

        if (projects.isEmpty()) {
            return Collections.emptyList();
        }

        // 4. 构建项目文本列表
        List<String> projectTexts = projects.stream()
                .map(this::buildProjectText)
                .collect(Collectors.toList());

        // 5. 计算学生画像的 TF-IDF 向量（基于项目文本作为语料库）
        Map<String, Double> studentVector = TfIdfCalculator.computeTfIdfForDocument(
                studentProfileText, projectTexts);

        // 6. 计算所有项目的 TF-IDF 向量
        List<Map<String, Double>> projectVectors = TfIdfCalculator.computeTfIdf(projectTexts);

        // 7. 逐一计算相似度
        List<RecommendedProject> recommendations = new ArrayList<>();
        for (int i = 0; i < projects.size(); i++) {
            double similarity = CosineSimilarity.compute(studentVector, projectVectors.get(i));
            if (similarity > 0) {
                recommendations.add(new RecommendedProject(
                        projects.get(i), similarity, "基于科研画像匹配"));
            }
        }

        // 8. 按相似度降序排列
        recommendations.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        // 9. 返回 Top-N
        return recommendations.stream()
                .limit(topN)
                .collect(Collectors.toList());
    }

    /**
     * 为教师/企业推荐匹配的企业需求
     *
     * @param userId 教师/企业用户ID
     * @param topN   返回前 N 个
     * @return 推荐列表
     */
    public List<RecommendedDemand> recommendDemandsForResearcher(Long userId, int topN) {
        LambdaQueryWrapper<ResearcherProfile> profileQuery = new LambdaQueryWrapper<>();
        profileQuery.eq(ResearcherProfile::getUserId, userId);
        ResearcherProfile profile = researcherProfileService.getOne(profileQuery);

        if (profile == null) {
            return Collections.emptyList();
        }

        String profileText = buildProfileText(profile);

        LambdaQueryWrapper<EnterpriseDemand> demandQuery = new LambdaQueryWrapper<>();
        demandQuery.eq(EnterpriseDemand::getStatus, "open");
        List<EnterpriseDemand> demands = enterpriseDemandService.list(demandQuery);

        if (demands.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> demandTexts = demands.stream()
                .map(this::buildDemandText)
                .collect(Collectors.toList());

        Map<String, Double> profileVector = TfIdfCalculator.computeTfIdfForDocument(
                profileText, demandTexts);
        List<Map<String, Double>> demandVectors = TfIdfCalculator.computeTfIdf(demandTexts);

        List<RecommendedDemand> recommendations = new ArrayList<>();
        for (int i = 0; i < demands.size(); i++) {
            double similarity = CosineSimilarity.compute(profileVector, demandVectors.get(i));
            if (similarity > 0) {
                recommendations.add(new RecommendedDemand(
                        demands.get(i), similarity, "基于科研能力匹配"));
            }
        }

        recommendations.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        return recommendations.stream().limit(topN).collect(Collectors.toList());
    }

    /**
     * 构建教师科研画像的文本特征
     * 合并研究方向、技能、科研兴趣等多个维度
     */
    private String buildProfileText(ResearcherProfile profile) {
        StringBuilder sb = new StringBuilder();
        if (profile.getResearchDirections() != null) {
            sb.append(profile.getResearchDirections()).append(" ");
        }
        if (profile.getSkills() != null) {
            sb.append(profile.getSkills()).append(" ");
        }
        if (profile.getResearchInterests() != null) {
            sb.append(profile.getResearchInterests()).append(" ");
        }
        if (profile.getProjectExperience() != null) {
            sb.append(profile.getProjectExperience()).append(" ");
        }
        return sb.toString().trim();
    }

    /**
     * 构建科研项目的文本特征
     */
    private String buildProjectText(ResearchProject project) {
        StringBuilder sb = new StringBuilder();
        if (project.getProjectName() != null) {
            sb.append(project.getProjectName()).append(" ");
        }
        if (project.getResearchFields() != null) {
            sb.append(project.getResearchFields()).append(" ");
        }
        if (project.getProjectDescription() != null) {
            sb.append(project.getProjectDescription()).append(" ");
        }
        if (project.getRequirements() != null) {
            sb.append(project.getRequirements()).append(" ");
        }
        return sb.toString().trim();
    }

    /**
     * 构建企业需求的文本特征
     */
    private String buildDemandText(EnterpriseDemand demand) {
        StringBuilder sb = new StringBuilder();
        if (demand.getDemandTitle() != null) {
            sb.append(demand.getDemandTitle()).append(" ");
        }
        if (demand.getIndustryField() != null) {
            sb.append(demand.getIndustryField()).append(" ");
        }
        if (demand.getDemandDescription() != null) {
            sb.append(demand.getDemandDescription()).append(" ");
        }
        if (demand.getTechnicalRequirements() != null) {
            sb.append(demand.getTechnicalRequirements()).append(" ");
        }
        return sb.toString().trim();
    }

    /**
     * 无科研画像时的默认推荐（按浏览量排序）
     */
    private List<RecommendedProject> getDefaultProjects(int topN) {
        LambdaQueryWrapper<ResearchProject> query = new LambdaQueryWrapper<>();
        query.eq(ResearchProject::getStatus, "recruiting");
        query.orderByDesc(ResearchProject::getViews);
        query.last("LIMIT " + topN);
        return researchProjectService.list(query).stream()
                .map(p -> new RecommendedProject(p, 0.0, "热门推荐"))
                .collect(Collectors.toList());
    }

    // ==================== 内部 VO 类 ====================

    /**
     * 推荐结果 VO：项目 + 匹配分数 + 推荐理由
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class RecommendedProject {
        private ResearchProject project;
        private double score;
        private String reason;
    }

    /**
     * 推荐结果 VO：企业需求 + 匹配分数 + 推荐理由
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class RecommendedDemand {
        private EnterpriseDemand demand;
        private double score;
        private String reason;
    }
}
