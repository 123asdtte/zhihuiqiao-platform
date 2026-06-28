package com.zhihuiqiao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhihuiqiao.entity.NotificationSetting;

import java.util.List;

/**
 * 通知设置 Service 接口
 */
public interface NotificationSettingService extends IService<NotificationSetting> {

    /**
     * 获取用户通知设置列表
     * 如果不存在则初始化默认设置
     *
     * @param userId 用户ID
     * @return 设置列表
     */
    List<NotificationSetting> getSettings(Long userId);

    /**
     * 更新用户通知设置
     *
     * @param userId   用户ID
     * @param settings 设置列表
     * @return 是否成功
     */
    boolean updateSettings(Long userId, List<NotificationSetting> settings);

    /**
     * 判断用户是否开启了某类通知
     *
     * @param userId 用户ID
     * @param type   通知类型
     * @return true-开启 false-关闭
     */
    boolean isEnabled(Long userId, String type);
}
