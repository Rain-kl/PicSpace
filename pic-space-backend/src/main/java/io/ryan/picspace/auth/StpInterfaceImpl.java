package io.ryan.picspace.auth;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.json.JSONUtil;
import io.ryan.picspace.auth.model.SpaceUserAuthContext;
import io.ryan.picspace.exception.BusinessException;
import io.ryan.picspace.exception.ErrorCode;
import io.ryan.picspace.model.entity.Picture;
import io.ryan.picspace.model.entity.SpaceUser;
import io.ryan.picspace.model.entity.User;
import io.ryan.picspace.model.enums.SpaceRoleEnum;
import io.ryan.picspace.service.PictureService;
import io.ryan.picspace.service.SpaceService;
import io.ryan.picspace.service.SpaceUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static io.ryan.picspace.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 自定义权限加载接口实现类
 */
@Component    // 保证此类被 SpringBoot 扫描，完成 Sa-Token 的自定义权限验证扩展
public class StpInterfaceImpl implements StpInterface {
    // 默认是 /api
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;

    @Resource
    private SpaceUserService spaceUserService;

    @Resource
    private PictureService pictureService;
    @Autowired
    private SpaceService spaceService;

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        List<String> ADMIN_PERMISSIONS = spaceUserAuthManager.getPermissionsByRole(SpaceRoleEnum.ADMIN.getValue());
        if (!StpKit.USER_TYPE.equals(loginType)) {
            return new ArrayList<>();
        }
        // 获取 userId
        User loginUser = (User) StpKit.USER.getSessionByLoginId(loginId).get(USER_LOGIN_STATE);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "用户未登录");
        }
        // 获取上下文对象
        SpaceUserAuthContext authContext = getAuthContextByRequest();

        if (isAllFieldsNull(authContext)) {
            return ADMIN_PERMISSIONS;
        }

        if (authContext.getSpaceId() != null) {
            SpaceUser spaceUser = spaceUserService.query()
                    .eq("spaceId", authContext.getSpaceId())
                    .eq("userId", loginUser.getId()).one();
            if (spaceUser == null) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限操作");
            }
            return spaceUserAuthManager.getPermissionsByRole(spaceUser.getSpaceRole());
        }
        if (authContext.getPictureId() != null) {
            Picture picture = pictureService.getById(authContext.getPictureId());
            if (picture == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            }
            if (picture.getSpaceId() == null) {
                return ADMIN_PERMISSIONS;
            }
            SpaceUser spaceUser = spaceUserService.query()
                    .eq("spaceId", picture.getSpaceId())
                    .eq("userId", loginUser.getId()).one();
            if (spaceUser == null) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限操作");
            }
            return spaceUserAuthManager.getPermissionsByRole(spaceUser.getSpaceRole());
        }
        if (authContext.getSpaceUserId() != null) {
            SpaceUser targetSpaceUser = spaceUserService.getById(authContext.getSpaceUserId());
            if (targetSpaceUser == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "空间用户不存在");
            }
            if (targetSpaceUser.getUserId().equals(loginUser.getId())) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "无法操作自己");
            }
            // 查询当前登录用户在该空间的角色
            SpaceUser operator = spaceUserService.query()
                    .eq("spaceId", targetSpaceUser.getSpaceId())
                    .eq("userId", loginUser.getId()).one();
            return spaceUserAuthManager.getPermissionsByRole(operator.getSpaceRole());
        }
        return ADMIN_PERMISSIONS;
    }


    /**
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return new ArrayList<>();
    }

    /**
     * 从请求中获取上下文对象
     */
    private SpaceUserAuthContext getAuthContextByRequest() {
        System.out.println("StpInterfaceImpl.getAuthContextByRequest");
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String contentType = request.getHeader(Header.CONTENT_TYPE.getValue());
        SpaceUserAuthContext authRequest;
        // 获取请求参数
        if (ContentType.JSON.getValue().equals(contentType)) {
            String body = ServletUtil.getBody(request);
            authRequest = JSONUtil.toBean(body, SpaceUserAuthContext.class);
        } else {
            Map<String, String> paramMap = ServletUtil.getParamMap(request);
            authRequest = BeanUtil.toBean(paramMap, SpaceUserAuthContext.class);
        }

        Long id = authRequest.getId();
        if (ObjUtil.isNotNull(id)) {
            // 获取到请求路径的业务前缀，/api/picture/aaa?a=1
            String requestURI = request.getRequestURI();
            // 先替换掉上下文，剩下的就是前缀
            String partURI = requestURI.replace(contextPath + "/", "");
            // 获取前缀的第一个斜杠前的字符串
            String moduleName = StrUtil.subBefore(partURI, "/", false);
            switch (moduleName) {
                case "picture":
                    authRequest.setPictureId(id);
                    break;
                case "spaceUser":
                    authRequest.setSpaceUserId(id);
                    break;
                case "space":
                    authRequest.setSpaceId(id);
                    break;
                default:
            }
        }
        return authRequest;
    }

    /**
     * 判断对象的所有字段是否为空
     */
    private boolean isAllFieldsNull(Object object) {
        if (object == null) {
            return true; // 对象本身为空
        }
        // 获取所有字段并判断是否所有字段都为空
        return Arrays.stream(ReflectUtil.getFields(object.getClass()))
                // 获取字段值
                .map(field -> ReflectUtil.getFieldValue(object, field))
                // 检查是否所有字段都为空
                .allMatch(ObjectUtil::isEmpty);
    }
}
