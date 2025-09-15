package io.ryan.picspace.aop;

import io.ryan.picspace.annotation.AuthCheck;
import io.ryan.picspace.exception.BusinessException;
import io.ryan.picspace.exception.ErrorCode;
import io.ryan.picspace.model.entity.User;
import io.ryan.picspace.model.enums.UserRoleEnum;
import io.ryan.picspace.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Slf4j
@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        RequestAttributes currentRequestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) currentRequestAttributes).getRequest();
        User loginUser = userService.getLoginUser(request);
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        if (mustRoleEnum == null) return joinPoint.proceed();

        UserRoleEnum loginUserEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        if (loginUserEnum == null) throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "用户无权限");

        if (UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(loginUserEnum)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "用户无权限");
        }
        return joinPoint.proceed();
    }
}
