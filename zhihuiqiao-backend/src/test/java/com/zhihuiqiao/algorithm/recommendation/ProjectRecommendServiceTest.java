package com.zhihuiqiao.algorithm.recommendation;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhihuiqiao.entity.EnterpriseDemand;
import com.zhihuiqiao.entity.ResearchProject;
import com.zhihuiqiao.entity.ResearcherProfile;
import com.zhihuiqiao.service.EnterpriseDemandService;
import com.zhihuiqiao.service.ResearchProjectService;
import com.zhihuiqiao.service.ResearcherProfileService;
import com.zhihuiqiao.service.SysUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 科研项目推荐服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class ProjectRecommendServiceTest {

    @Mock
    private ResearcherProfileService researcherProfileService;

    @Mock
    private ResearchProjectService researchProjectService;

    @Mock
    private EnterpriseDemandService enterpriseDemandService;

    @Mock
    private SysUserService sysUserService;

    @InjectMocks
    private ProjectRecommendService projectRecommendService;

    private ResearcherProfile mockProfile;
    private ResearchProject mockProject1;
    private ResearchProject mockProject2;
    private EnterpriseDemand mockDemand;

    @BeforeEach
    void setUp() {
        mockProfile = new ResearcherProfile();
        mockProfile.setId(1L);
        mockProfile.setUserId(100L);
        mockProfile.setResearchDirections("人工智能,机器学习");
        mockProfile.setSkills("Python,TensorFlow");
        mockProfile.setResearchInterests("深度学习,NLP");
        mockProfile.setProjectExperience("智能推荐系统");

        mockProject1 = new ResearchProject();
        mockProject1.setId(1L);
        mockProject1.setProjectName("AI驱动的推荐系统");
        mockProject1.setResearchFields("人工智能,推荐系统");
        mockProject1.setProjectDescription("基于深度学习的推荐算法研究");
        mockProject1.setRequirements("熟悉Python和TensorFlow");
        mockProject1.setStatus("recruiting");
        mockProject1.setViews(100);

        mockProject2 = new ResearchProject();
        mockProject2.setId(2L);
        mockProject2.setProjectName("云计算平台建设");
        mockProject2.setResearchFields("云计算,分布式系统");
        mockProject2.setProjectDescription("企业级云平台架构设计");
        mockProject2.setRequirements("熟悉分布式系统");
        mockProject2.setStatus("recruiting");
        mockProject2.setViews(50);

        mockDemand = new EnterpriseDemand();
        mockDemand.setId(1L);
        mockDemand.setDemandTitle("AI算法优化需求");
        mockDemand.setIndustryField("人工智能");
        mockDemand.setDemandDescription("需要优化深度学习模型");
        mockDemand.setTechnicalRequirements("熟悉Python和PyTorch");
        mockDemand.setStatus("open");
    }

    @Test
    void testRecommendProjectsForStudent_withProfile() {
        when(researcherProfileService.getOne(any(LambdaQueryWrapper.class)))
                .thenReturn(mockProfile);
        when(researchProjectService.list(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(mockProject1, mockProject2));

        List<ProjectRecommendService.RecommendedProject> recommendations =
                projectRecommendService.recommendProjectsForStudent(100L, 5);

        assertNotNull(recommendations);
        assertFalse(recommendations.isEmpty());
        // 项目1(人工智能相关)应比项目2(云计算)匹配度更高
        assertTrue(recommendations.get(0).getScore() >= 0);
        assertEquals("基于科研画像匹配", recommendations.get(0).getReason());
    }

    @Test
    void testRecommendProjectsForStudent_withoutProfile() {
        when(researcherProfileService.getOne(any(LambdaQueryWrapper.class)))
                .thenReturn(null);
        when(researchProjectService.list(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(mockProject1, mockProject2));

        List<ProjectRecommendService.RecommendedProject> recommendations =
                projectRecommendService.recommendProjectsForStudent(100L, 5);

        assertNotNull(recommendations);
        assertEquals(2, recommendations.size());
        // 无画像时默认推荐，分数为0，理由为"热门推荐"
        for (ProjectRecommendService.RecommendedProject rp : recommendations) {
            assertEquals(0.0, rp.getScore(), 1e-10);
            assertEquals("热门推荐", rp.getReason());
        }
    }

    @Test
    void testRecommendProjectsForStudent_noProjects() {
        when(researcherProfileService.getOne(any(LambdaQueryWrapper.class)))
                .thenReturn(mockProfile);
        when(researchProjectService.list(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        List<ProjectRecommendService.RecommendedProject> recommendations =
                projectRecommendService.recommendProjectsForStudent(100L, 5);

        assertNotNull(recommendations);
        assertTrue(recommendations.isEmpty());
    }

    @Test
    void testRecommendDemandsForResearcher_withProfile() {
        when(researcherProfileService.getOne(any(LambdaQueryWrapper.class)))
                .thenReturn(mockProfile);
        when(enterpriseDemandService.list(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(mockDemand));

        List<ProjectRecommendService.RecommendedDemand> recommendations =
                projectRecommendService.recommendDemandsForResearcher(100L, 5);

        assertNotNull(recommendations);
        assertFalse(recommendations.isEmpty());
        // 企业需求与教师画像匹配(人工智能方向)
        assertTrue(recommendations.get(0).getScore() > 0);
        assertEquals("基于科研能力匹配", recommendations.get(0).getReason());
    }

    @Test
    void testRecommendDemandsForResearcher_withoutProfile() {
        when(researcherProfileService.getOne(any(LambdaQueryWrapper.class)))
                .thenReturn(null);

        List<ProjectRecommendService.RecommendedDemand> recommendations =
                projectRecommendService.recommendDemandsForResearcher(100L, 5);

        assertNotNull(recommendations);
        assertTrue(recommendations.isEmpty());
    }

    @Test
    void testRecommendDemandsForResearcher_noDemands() {
        when(researcherProfileService.getOne(any(LambdaQueryWrapper.class)))
                .thenReturn(mockProfile);
        when(enterpriseDemandService.list(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        List<ProjectRecommendService.RecommendedDemand> recommendations =
                projectRecommendService.recommendDemandsForResearcher(100L, 5);

        assertNotNull(recommendations);
        assertTrue(recommendations.isEmpty());
    }

    @Test
    void testTopN_limit() {
        when(researcherProfileService.getOne(any(LambdaQueryWrapper.class)))
                .thenReturn(mockProfile);
        when(researchProjectService.list(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(mockProject1, mockProject2));

        // 只返回前1个
        List<ProjectRecommendService.RecommendedProject> recommendations =
                projectRecommendService.recommendProjectsForStudent(100L, 1);

        assertNotNull(recommendations);
        assertTrue(recommendations.size() <= 1);
    }
}
