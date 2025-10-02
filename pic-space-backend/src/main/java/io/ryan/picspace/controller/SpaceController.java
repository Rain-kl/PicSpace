package io.ryan.picspace.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.ryan.picspace.auth.annotation.AuthSysUser;
import io.ryan.picspace.common.BaseResponse;
import io.ryan.picspace.common.DeleteRequest;
import io.ryan.picspace.common.ResultUtils;
import io.ryan.picspace.common.utils.Validator;
import io.ryan.picspace.exception.BusinessException;
import io.ryan.picspace.exception.ErrorCode;
import io.ryan.picspace.exception.ThrowUtils;
import io.ryan.picspace.auth.StpKit;
import io.ryan.picspace.auth.contant.SpaceUserPermissionConstant;
import io.ryan.picspace.model.dto.space.SpaceAddRequest;
import io.ryan.picspace.model.dto.space.SpaceEditRequest;
import io.ryan.picspace.model.dto.space.SpaceLevel;
import io.ryan.picspace.model.entity.Space;
import io.ryan.picspace.model.entity.User;
import io.ryan.picspace.model.enums.SpaceLevelEnum;
import io.ryan.picspace.model.vo.SpaceVO;
import io.ryan.picspace.service.SpaceService;
import io.ryan.picspace.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/space")
@AuthSysUser
public class SpaceController {

    @Resource
    private UserService userService;

    @Resource
    private SpaceService spaceService;


    @PostMapping("/add")
    public BaseResponse<Long> addSpace(@RequestBody SpaceAddRequest spaceAddRequest) {
        ThrowUtils.throwIf(spaceAddRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(spaceAddRequest.getSpaceLevel() == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(spaceAddRequest.getSpaceName() == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser();
        long newId = spaceService.addSpace(spaceAddRequest, loginUser);
        return ResultUtils.success(newId);
    }

    /**
     * 根据 id 获取空间（仅管理员可用）
     */
    @GetMapping("/get/vo")
    public BaseResponse<SpaceVO> getSpaceById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(SpaceVO.objToVo(space));
    }


    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteSpace(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser();
        Long id = deleteRequest.getId();
        // 判断是否存在
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或者管理员可删除
        if (!oldSpace.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = spaceService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }


    /**
     * 分页获取空间列表（封装类）
     */
    @PostMapping("/list")
    public BaseResponse<List<SpaceVO>> listSpace() {
        User loginUser = userService.getLoginUser();
        Long userId = loginUser.getId();
        // 查询数据库
        List<Space> spaceList = spaceService.query().eq("userId", userId).list();
        List<SpaceVO> spaceVOList = spaceList.stream().map(SpaceVO::objToVo).toList();
        // 获取封装类
        return ResultUtils.success(spaceVOList);
    }


    /**
     * 编辑空间（给用户使用）
     */
    @PostMapping("/edit")
    @SaCheckPermission(type = StpKit.USER_TYPE, value = SpaceUserPermissionConstant.PICTURE_UPLOAD)
    public BaseResponse<Boolean> editSpace(@RequestBody SpaceEditRequest spaceEditRequest) {
        // 数据校验
        if (spaceEditRequest == null || spaceEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ThrowUtils.throwIf(!Validator.validNaming(spaceEditRequest.getSpaceName()), ErrorCode.PARAMS_ERROR, "空间名称不合法");
        // 判断是否存在
        long id = spaceEditRequest.getId();
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);


        // 在此处将实体类和 DTO 进行转换
        Space space = new Space();
        BeanUtils.copyProperties(spaceEditRequest, space);
        // 设置编辑时间
        space.setEditTime(new Date());

        User loginUser = userService.getLoginUser();
        // 仅本人或管理员可编辑
        if (!oldSpace.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        // 操作数据库
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 获取空间等级列表
     */
    @GetMapping("/list/level")
    public BaseResponse<List<SpaceLevel>> listSpaceLevel() {
        List<SpaceLevel> list = Arrays.stream(SpaceLevelEnum.values())
                .map(spaceLevelEnum -> new SpaceLevel(
                        spaceLevelEnum.getValue(),
                        spaceLevelEnum.getText(),
                        spaceLevelEnum.getMaxCount(),
                        spaceLevelEnum.getMaxSize())
                ).toList();
        return ResultUtils.success(list);
    }


}
