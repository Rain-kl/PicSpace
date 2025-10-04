package io.ryan.picspace.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.ryan.picspace.auth.annotation.AuthSysUser;
import io.ryan.picspace.common.BaseResponse;
import io.ryan.picspace.common.ResultUtils;
import io.ryan.picspace.exception.ErrorCode;
import io.ryan.picspace.exception.ThrowUtils;
import io.ryan.picspace.model.dto.user.UserLoginRequest;
import io.ryan.picspace.model.dto.user.UserQueryRequest;
import io.ryan.picspace.model.dto.user.UserRegisterRequest;
import io.ryan.picspace.model.entity.User;
import io.ryan.picspace.model.vo.LoginUserVO;
import io.ryan.picspace.model.vo.UserVO;
import io.ryan.picspace.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 获取当前登录用户
     */
    @GetMapping("/get/login")
    @AuthSysUser
    public BaseResponse<LoginUserVO> getLoginUser() {
        User loginUser = userService.getLoginUser();
        return ResultUtils.success(userService.getLoginUserVO(loginUser));
    }

    /**
     * 用户注销
     */
    @PostMapping("/logout")
    @AuthSysUser
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }


    /**
     * 根据 id 获取包装类
     */
    @GetMapping("/get/vo")
    @AuthSysUser
    public BaseResponse<UserVO> getUserVOById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 查询用户列表, 只允许通过 userAccount, id 查询
     *
     * @param userQueryRequest 查询请求参数
     */
    @PostMapping("/get/list/page")
    public BaseResponse<Page<UserVO>> getUserListPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);

        UserQueryRequest newUserQueryRequest = new UserQueryRequest();
        newUserQueryRequest.setUserAccount(userQueryRequest.getUserAccount());
        newUserQueryRequest.setId(userQueryRequest.getId());

        long current = userQueryRequest.getCurrent();
        long pageSize = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, pageSize),
                userService.getQueryWrapper(newUserQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, pageSize, userPage.getTotal());
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }

}
