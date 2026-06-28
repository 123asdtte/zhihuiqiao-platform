package com.zhihuiqiao.service;

import com.zhihuiqiao.entity.NotificationSetting;
import com.zhihuiqiao.mapper.NotificationSettingMapper;
import com.zhihuiqiao.service.impl.NotificationSettingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 通知设置服务单元测试
 * 验证设置初始化、更新、开关判断逻辑
 */
@ExtendWith(MockitoExtension.class)
class NotificationSettingServiceImplTest {

    @Mock
    private NotificationSettingMapper notificationSettingMapper;

    private NotificationSettingServiceImpl settingService;

    @BeforeEach
    void setUp() throws Exception {
        settingService = new NotificationSettingServiceImpl();
        Field baseMapperField = settingService.getClass().getSuperclass().getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(settingService, notificationSettingMapper);
    }

    /**
     * 测试：用户无设置时自动初始化默认设置
     */
    @Test
    void getSettings_shouldInitializeDefaults_whenEmpty() {
        when(notificationSettingMapper.selectList(any())).thenReturn(new ArrayList<>());

        List<NotificationSetting> settings = settingService.getSettings(1L);

        assertEquals(6, settings.size());
        settings.forEach(s -> assertEquals(1, s.getEnabled()));
        verify(notificationSettingMapper, times(6)).insert(any(NotificationSetting.class));
    }

    /**
     * 测试：isEnabled 对未找到设置返回 true（默认开启）
     */
    @Test
    void isEnabled_shouldReturnTrue_whenNotFound() {
        when(notificationSettingMapper.selectOne(any())).thenReturn(null);

        boolean enabled = settingService.isEnabled(1L, "system");

        assertTrue(enabled);
    }

    /**
     * 测试：isEnabled 正确返回设置状态
     */
    @Test
    void isEnabled_shouldReturnFalse_whenDisabled() {
        NotificationSetting setting = new NotificationSetting();
        setting.setUserId(1L);
        setting.setType("booking");
        setting.setEnabled(0);
        when(notificationSettingMapper.selectOne(any())).thenReturn(setting);

        boolean enabled = settingService.isEnabled(1L, "booking");

        assertFalse(enabled);
    }
}
