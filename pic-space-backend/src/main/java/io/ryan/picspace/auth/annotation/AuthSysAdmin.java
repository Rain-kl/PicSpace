package io.ryan.picspace.auth.annotation;

import cn.dev33.satoken.annotation.SaCheckLogin;
import io.ryan.picspace.auth.StpKit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@SaCheckLogin(type = StpKit.ADMIN_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface AuthSysAdmin {

}
