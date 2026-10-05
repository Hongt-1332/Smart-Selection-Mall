package edu.fafu.config;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class StpInterfaceImpl implements StpInterface {

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 本系统暂未使用权限粒度控制，返回空列表
        return new ArrayList<>();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        List<String> list = new ArrayList<>();
        // 从 Sa-Token Session 中获取角色（登录时已存入）
        String role = StpUtil.getSession().getString("role");
        if (role != null) {
            list.add(role);
        }
        return list;
    }
}