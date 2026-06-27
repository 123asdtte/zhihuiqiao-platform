package com.zhihuiqiao.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhihuiqiao.common.Result;
import com.zhihuiqiao.entity.ResearchProject;
import com.zhihuiqiao.entity.ResearcherProfile;
import com.zhihuiqiao.entity.SysUser;
import com.zhihuiqiao.service.DeepSeekService;
import com.zhihuiqiao.service.ResearchProjectService;
import com.zhihuiqiao.service.ResearcherProfileService;
import com.zhihuiqiao.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * AI 智能推荐 Controller
 * 基于 DeepSeek 提供科研项目智能推荐
 */
@Slf4j
@Tag(name = "AI智能推荐", description = "基于 DeepSeek 的科研项目智能推荐")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiRecommendController {

    private final DeepSeekService deepSeekService;
    private final ResearcherProfileService researcherProfileService;
    private final ResearchProjectService researchProjectService;
    private final SysUserService sysUserService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取当前登录用户 ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() instanceof Long userId) {
            return userId;
        }
        return null;
    }

    /**
     * 获取当前登录用户角色类型
     */
    private String getCurrentRoleType() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "teacher";
        }
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(auth -> auth.getAuthority().replace("ROLE_", "").toLowerCase())
                .orElse("teacher");
    }

    @Operation(summary = "科研项目智能推荐", description = "根据当前用户的科研画像，利用 DeepSeek AI 推荐合适的科研项目")
    @GetMapping("/recommend/projects")
    public Result<List<Map<String, Object>>> recommendProjects() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error("请先登录");
        }

        // 1. 查询用户科研画像
        ResearcherProfile profile = researcherProfileService.getByUserId(userId);

        // 2. 查询所有招募中的科研项目
        List<ResearchProject> projects = researchProjectService.listRecruitingProjects();
        if (projects.isEmpty()) {
            return Result.success(new ArrayList<>());
        }

        // 3. 构建 AI 提示词
        String userPrompt = buildPrompt(profile, projects);
        String systemPrompt = "你是一名科研匹配专家。请根据用户画像和项目列表，选出最匹配的 1-5 个项目，并以 JSON 数组格式返回，每个元素包含 projectId（项目ID，Long 类型）和 reason（推荐理由，简短 20 字以内）。只返回 JSON 数组，不要返回其他说明文字。";

        try {
            String aiResponse = deepSeekService.chat(systemPrompt, userPrompt);
            log.info("DeepSeek 推荐响应: {}", aiResponse);

            if (!StringUtils.hasText(aiResponse)) {
                return Result.success(buildFallbackResult(projects, profile));
            }

            // 4. 解析 AI 返回的 JSON
            JsonNode arrayNode = objectMapper.readTree(aiResponse);
            if (!arrayNode.isArray()) {
                // 兼容对象外层包裹的情况
                JsonNode dataNode = arrayNode.get("recommendations");
                if (dataNode != null && dataNode.isArray()) {
                    arrayNode = dataNode;
                } else {
                    arrayNode = arrayNode.path("data");
                }
            }

            List<Map<String, Object>> result = new ArrayList<>();
            for (JsonNode node : arrayNode) {
                Long projectId = node.has("projectId") ? node.get("projectId").asLong() : null;
                String reason = node.has("reason") ? node.get("reason").asText() : "与你匹配度较高";

                Optional<ResearchProject> projectOpt = projects.stream().filter(p -> p.getId().equals(projectId)).findFirst();
                if (projectOpt.isPresent()) {
                    ResearchProject project = projectOpt.get();
                    Map<String, Object> item = new HashMap<>();
                    item.put("project", project);
                    item.put("reason", reason);
                    item.put("score", 0);
                    result.add(item);
                }
            }

            // 如果 AI 没有返回任何有效推荐，返回兜底结果
            if (result.isEmpty()) {
                return Result.success(buildFallbackResult(projects, profile));
            }

            return Result.success(result);
        } catch (JsonProcessingException e) {
            log.error("解析 DeepSeek 推荐结果失败", e);
            return Result.success(buildFallbackResult(projects, profile));
        } catch (Exception e) {
            log.error("科研项目推荐失败", e);
            return Result.success(buildFallbackResult(projects, profile));
        }
    }

    /**
     * 构建推荐提示词
     */
    private String buildPrompt(ResearcherProfile profile, List<ResearchProject> projects) {
        StringBuilder sb = new StringBuilder();

        if (profile != null) {
            sb.append("用户科研画像：");
            sb.append("研究方向=").append(defaultIfEmpty(profile.getResearchDirections(), "未填写")).append(";");
            sb.append("技能=").append(defaultIfEmpty(profile.getSkills(), "未填写")).append(";");
            sb.append("科研兴趣=").append(defaultIfEmpty(profile.getResearchInterests(), "未填写")).append(";");
            sb.append("项目经历=").append(defaultIfEmpty(profile.getProjectExperience(), "未填写")).append(";");
            sb.append("合作意向=").append(defaultIfEmpty(profile.getCooperationIntention(), "未填写")).append(";");
            sb.append("可投入时间=").append(defaultIfEmpty(profile.getAvailability(), "未填写")).append(";");
        } else {
            sb.append("用户科研画像：未填写");
        }
        sb.append("\n\n可选科研项目列表：\n");

        for (ResearchProject project : projects) {
            sb.append("项目ID:").append(project.getId()).append(";");
            sb.append("名称:").append(project.getProjectName()).append(";");
            sb.append("类型:").append(defaultIfEmpty(project.getProjectType(), "未填写")).append(";");
            sb.append("领域:").append(defaultIfEmpty(project.getResearchFields(), "未填写")).append(";");
            sb.append("简介:").append(defaultIfEmpty(project.getProjectDescription(), "未填写")).append(";");
            sb.append("要求:").append(defaultIfEmpty(project.getRequirements(), "未填写")).append(";");
            sb.append("预期成果:").append(defaultIfEmpty(project.getExpectedOutcomes(), "未填写")).append("\n");
        }

        sb.append("\n请根据用户画像与项目信息的匹配程度，返回最匹配的 1-5 个项目ID及推荐理由。");
        return sb.toString();
    }

    /**
     * 兜底推荐：按关键词简单匹配
     */
    private List<Map<String, Object>> buildFallbackResult(List<ResearchProject> projects, ResearcherProfile profile) {
        List<ResearchProject> sorted = new ArrayList<>(projects);
        if (profile != null) {
            String keywords = combineProfileKeywords(profile).toLowerCase();
            Map<Long, Integer> scoreMap = new HashMap<>();
            for (ResearchProject project : sorted) {
                int score = 0;
                String text = combineProjectText(project).toLowerCase();
                for (String keyword : keywords.split("[，,；;\\s]+")) {
                    if (keyword.length() > 1 && text.contains(keyword)) {
                        score++;
                    }
                }
                scoreMap.put(project.getId(), score);
            }
            sorted.sort((a, b) -> scoreMap.getOrDefault(b.getId(), 0).compareTo(scoreMap.getOrDefault(a.getId(), 0)));
        }

        List<Map<String, Object>> result = new ArrayList<>();
        int limit = Math.min(5, sorted.size());
        for (int i = 0; i < limit; i++) {
            ResearchProject project = sorted.get(i);
            Map<String, Object> item = new HashMap<>();
            item.put("project", project);
            item.put("reason", "基于你的科研画像为你推荐");
            item.put("score", 0);
            result.add(item);
        }
        return result;
    }

    /**
     * 组合用户画像关键词
     */
    private String combineProfileKeywords(ResearcherProfile profile) {
        List<String> parts = new ArrayList<>();
        parts.add(defaultIfEmpty(profile.getResearchDirections(), ""));
        parts.add(defaultIfEmpty(profile.getSkills(), ""));
        parts.add(defaultIfEmpty(profile.getResearchInterests(), ""));
        parts.add(defaultIfEmpty(profile.getProjectExperience(), ""));
        parts.add(defaultIfEmpty(profile.getCooperationIntention(), ""));
        return parts.stream().filter(StringUtils::hasText).collect(Collectors.joining(","));
    }

    /**
     * 组合项目文本
     */
    private String combineProjectText(ResearchProject project) {
        List<String> parts = new ArrayList<>();
        parts.add(defaultIfEmpty(project.getProjectName(), ""));
        parts.add(defaultIfEmpty(project.getProjectType(), ""));
        parts.add(defaultIfEmpty(project.getResearchFields(), ""));
        parts.add(defaultIfEmpty(project.getProjectDescription(), ""));
        parts.add(defaultIfEmpty(project.getRequirements(), ""));
        parts.add(defaultIfEmpty(project.getExpectedOutcomes(), ""));
        return parts.stream().filter(StringUtils::hasText).collect(Collectors.joining(","));
    }

    /**
     * 默认字符串处理
     */
    private String defaultIfEmpty(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }
}
