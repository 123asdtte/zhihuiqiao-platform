package com.zhihuiqiao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhihuiqiao.entity.NotificationSetting;
import com.zhihuiqiao.mapper.NotificationSettingMapper;
import com.zhihuiqiao.service.NotificationSettingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 通知设置 Service 实现类
 */
@Service
public class NotificationSettingServiceImpl extends ServiceImpl<NotificationSettingMapper, NotificationSetting>
        implements NotificationSettingService {

    /**
     * 支持配置的通知类型列表
     */
    private static final List<String> DEFAULT_TYPES = Arrays.asList(
            "system", "project", "application", "resource", "booking", "learning"
    );

    @Override
    public List<NotificationSetting> getSettings(Long userId) {
        // 查询用户已有设置
        LambdaQueryWrapper<NotificationSetting> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationSetting::getUserId, userId);
        List<NotificationSetting> existing = baseMapper.selectList(wrapper);

        // 如果没有任何设置，初始化默认设置（全部开启）
        if (existing.isEmpty()) {
            List<NotificationSetting> defaults = new ArrayList<>();
            for (String type : DEFAULT_TYPES) {
                NotificationSetting setting = new NotificationSetting();
                setting.setUserId(userId);
                setting.setType(type);
                setting.setEnabled(1);
                baseMapper.insert(setting);
                defaults.add(setting);
            }
            return defaults;
        }

        return existing;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSettings(Long userId, List<NotificationSetting> settings) {
        if (settings == null || settings.isEmpty()) {
            return false;
        }

        for (NotificationSetting setting : settings) {
            // 校验类型有效性
            if (!DEFAULT_TYPES.contains(setting.getType())) {
                continue;
            }

            // 查询是否已存在该类型设置
            LambdaQueryWrapper<NotificationSetting> query = new LambdaQueryWrapper<>();
            query.eq(NotificationSetting::getUserId, userId)
                    .eq(NotificationSetting::getType, setting.getType());
            NotificationSetting existing = baseMapper.selectOne(query);

            if (existing != null) {
                // 更新开关状态
                UpdateWrapper<NotificationSetting> update = new UpdateWrapper<>();
                update.eq("id", existing.getId())
                        .set("enabled", setting.getEnabled() != null && setting.getEnabled() == 1 ? 1 : 0);
                baseMapper.update(null, update);
            } else {
                // 新增设置记录
                setting.setUserId(userId);
                if (setting.getEnabled() == null) {
                    setting.setEnabled(0);
                }
                baseMapper.insert(setting);
            }
        }
        return true;
    }

    @Override
    public boolean isEnabled(Long userId, String type) {
        LambdaQueryWrapper<NotificationSetting> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationSetting::getUserId, userId)
                .eq(NotificationSetting::getType, type);
        NotificationSetting setting = baseMapper.selectOne(wrapper);

        // 未找到设置时默认开启
        if (setting == null) {
            return true;
        }
        return setting.getEnabled() != null && setting.getEnabled() == 1;
    }
}
