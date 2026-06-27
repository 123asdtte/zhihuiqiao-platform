package com.zhihuiqiao.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhihuiqiao.common.Result;
import com.zhihuiqiao.dto.AiChatRequest;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @Operation(summary = "AI 通用对话", description = "基于 DeepSeek AI 的通用问答助手")
    @PostMapping("/chat")
    public Result<String> chat(@RequestBody AiChatRequest request) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error("请先登录");
        }

        if (!StringUtils.hasText(request.getQuestion())) {
            return Result.success("你好，有什么可以帮你的吗？");
        }

        String question = request.getQuestion();

        // 如果用户咨询推荐科研项目，走本地推荐接口生成结果
        if (isRecommendQuestion(question)) {
            return recommendByLocal(userId);
        }

        String systemPrompt = "你是\"智汇桥\"产学研平台的智能助手。平台主要功能包括：科研项目发布与组队、企业需求对接、学习资源分享、闲置资源预约等。请用简洁、友好、专业的中文回答用户问题。如果用户询问平台操作，请尽量引导其使用对应功能模块。";

        try {
            String answer = deepSeekService.chat(systemPrompt, question);
            return Result.success(answer);
        } catch (Exception e) {
            log.error("AI 对话失败", e);
            return Result.success("抱歉，AI 助手暂时无法回答，请稍后再试。");
        }
    }

    /**
     * 判断用户问题是否为科研项目推荐意图
     */
    private boolean isRecommendQuestion(String question) {
        String lower = question.toLowerCase();
        String[] keywords = {"推荐", "科研项目", "推荐项目", "适合我的项目", "项目推荐"};
        for (String keyword : keywords) {
            if (lower.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 本地生成项目推荐文本
     */
    private Result<String> recommendByLocal(Long userId) {
        ResearcherProfile profile = researcherProfileService.getByUserId(userId);
        List<ResearchProject> projects = researchProjectService.listRecruitingProjects();
        if (projects.isEmpty()) {
            return Result.success("当前没有正在招募中的科研项目，你可以稍后再来问我，或者先去浏览企业需求和学习资源。");
        }

        List<Map<String, Object>> fallback = buildFallbackResult(projects, profile);
        if (fallback.isEmpty()) {
            return Result.success("当前没有合适的推荐项目，建议你完善科研画像后再来获取个性化推荐。");
        }

        StringBuilder sb = new StringBuilder("根据你的科研画像，为你推荐以下招募中的科研项目：\n\n");
        int index = 1;
        for (Map<String, Object> item : fallback) {
            ResearchProject project = (ResearchProject) item.get("project");
            String reason = (String) item.get("reason");
            sb.append(index).append(". ").append(project.getProjectName()).append("\n");
            sb.append("   ").append(reason).append("\n");
            sb.append("   简介：").append(defaultIfEmpty(project.getProjectDescription(), "暂无简介")).append("\n");
            sb.append("   类型：").append(defaultIfEmpty(project.getProjectType(), "未分类")).append(" | 领域：")
              .append(defaultIfEmpty(project.getResearchFields(), "未填写")).append("\n\n");
            index++;
            if (index > 5) {
                break;
            }
        }
        sb.append("你可以点击卡片查看详情，或直接在导航中进入“科研项目”模块浏览全部项目。");
        return Result.success(sb.toString());
    }
}
