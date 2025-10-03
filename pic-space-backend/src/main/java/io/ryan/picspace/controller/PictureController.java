package io.ryan.picspace.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.ryan.picspace.auth.StpKit;
import io.ryan.picspace.auth.annotation.AuthSysUser;
import io.ryan.picspace.auth.contant.SpaceUserPermissionConstant;
import io.ryan.picspace.common.BaseResponse;
import io.ryan.picspace.common.DeleteRequest;
import io.ryan.picspace.common.ResultUtils;
import io.ryan.picspace.exception.BusinessException;
import io.ryan.picspace.exception.ErrorCode;
import io.ryan.picspace.exception.ThrowUtils;
import io.ryan.picspace.model.dto.picture.PictureQueryRequest;
import io.ryan.picspace.model.dto.picture.PictureUpdateRequest;
import io.ryan.picspace.model.dto.picture.PictureUploadRequest;
import io.ryan.picspace.model.entity.Picture;
import io.ryan.picspace.model.entity.Space;
import io.ryan.picspace.model.entity.User;
import io.ryan.picspace.model.enums.PictureReviewStatusEnum;
import io.ryan.picspace.model.vo.PictureTagCategory;
import io.ryan.picspace.model.vo.PictureVO;
import io.ryan.picspace.service.PictureService;
import io.ryan.picspace.service.SpaceService;
import io.ryan.picspace.service.SpaceUserService;
import io.ryan.picspace.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/picture")
public class PictureController {

    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    @Resource
    private SpaceService spaceService;
    @Autowired
    private SpaceUserService spaceUserService;

    /**
     * 上传图片（可重新上传）
     */
    @PostMapping("/upload")
    @AuthSysUser
    @SaCheckPermission(type = StpKit.USER_TYPE, value = SpaceUserPermissionConstant.PICTURE_UPLOAD)
    public BaseResponse<PictureVO> uploadPicture(
            @RequestPart("file") MultipartFile multipartFile,
            PictureUploadRequest pictureUploadRequest) throws IOException {
        System.out.println("权限测试");
        User loginUser = userService.getLoginUser();
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 通过 URL 上传图片（可重新上传）
     */
    @PostMapping("/upload/url")
    @AuthSysUser
    @SaCheckPermission(type = StpKit.USER_TYPE, value = SpaceUserPermissionConstant.PICTURE_UPLOAD)
    public BaseResponse<PictureVO> uploadPictureByUrl(
            @RequestBody PictureUploadRequest pictureUploadRequest) throws IOException {
        User loginUser = userService.getLoginUser();
        String fileUrl = pictureUploadRequest.getFileUrl();
        PictureVO pictureVO = pictureService.uploadPicture(pictureUploadRequest.getFileUrl(), pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    @PostMapping("/delete")
    @AuthSysUser
    @SaCheckPermission(type = StpKit.USER_TYPE, value = SpaceUserPermissionConstant.PICTURE_DELETE)
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser();
        Boolean b = pictureService.deletePicture(deleteRequest, loginUser);
        ThrowUtils.throwIf(!b, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(b);
    }


    /**
     * 根据 id 获取图片（封装类）
     */
    @GetMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        if (picture.getSpaceId() != null) {
            User loginUser = userService.getLoginUser();
            pictureService.checkPictureAuth(loginUser, picture);
        }

        if (picture.getReviewStatus().equals(PictureReviewStatusEnum.PASS.getValue()) ||
                picture.getUserId().equals(userService.getLoginUser().getId())) {
            // 获取封装类
            return ResultUtils.success(pictureService.getPictureVO(picture, request));
        } else {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

    }

    /**
     * 分页获取图片列表（封装类）
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                             HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 50, ErrorCode.PARAMS_ERROR);
        // 仅获取审核通过的图片
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        if (pictureQueryRequest.getSpaceId() != null) {
            //私有空间
            User loginUser = userService.getLoginUser();
            Space space = spaceService.getById(pictureQueryRequest.getSpaceId());
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
            boolean exists = spaceUserService.query()
                    .eq("spaceId", space.getId())
                    .eq("userId", loginUser.getId())
                    .exists();
            ThrowUtils.throwIf(!exists, ErrorCode.NO_AUTH_ERROR);
        } else {
            // 仅获取公开的图片
            pictureQueryRequest.setPublicFlag(true);
        }
        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        // 获取封装类
        return ResultUtils.success(pictureService.getPictureVOPage(picturePage, request));
    }

    /**
     * 编辑图片（给用户使用）
     */
    @PostMapping("/edit")
    @AuthSysUser
    @SaCheckPermission(type = StpKit.USER_TYPE, value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<Boolean> editPicture(@RequestBody PictureUpdateRequest pictureEditRequest) {
        if (pictureEditRequest == null || pictureEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User loginUser = userService.getLoginUser();
        // 操作数据库
        boolean result = pictureService.editPicture(pictureEditRequest, loginUser);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
        List<String> categoryList = Arrays.asList("现实", "二次元", "漫画", "海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }

}