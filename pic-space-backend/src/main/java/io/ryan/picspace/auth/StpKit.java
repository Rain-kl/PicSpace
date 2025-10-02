package io.ryan.picspace.auth;

import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Component;

/**
 * StpLogic 门面类，管理项目中所有的 StpLogic 账号体系
 * 添加 @Component 注解的目的是确保静态属性 DEFAULT 和 SPACE 被初始化
 */
@Component
public class StpKit {

    public static final String ADMIN_TYPE = "sys_admin";
    public static final String USER_TYPE = "sys_user";

    /**
     * 默认原生会话对象，项目中目前没使用到
     */
    public static final StpLogic DEFAULT = StpUtil.stpLogic;

    /**
     * Space 会话对象，管理 Space 表所有账号的登录、权限认证
     */
    public static final StpLogic ADMIN = new StpLogic(ADMIN_TYPE) {
        @Override
        public String splicingKeyTokenName() {
            return "admin-token";
        }
    };

    public static final StpLogic USER = new StpLogic(USER_TYPE) {
        @Override
        public String splicingKeyTokenName() {
            return "user-token";
        }
    };

    public static void logout() {
        StpKit.ADMIN.logout();
        StpKit.USER.logout();
    }
}