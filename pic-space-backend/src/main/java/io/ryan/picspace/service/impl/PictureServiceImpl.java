package io.ryan.picspace.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.ryan.picspace.common.DeleteRequest;
import io.ryan.picspace.exception.BusinessException;
import io.ryan.picspace.exception.ErrorCode;
import io.ryan.picspace.exception.ThrowUtils;
import io.ryan.picspace.manager.PictureManager;
import io.ryan.picspace.mapper.PictureMapper;
import io.ryan.picspace.model.dto.picture.*;
import io.ryan.picspace.model.entity.Picture;
import io.ryan.picspace.model.entity.Space;
import io.ryan.picspace.model.entity.User;
import io.ryan.picspace.model.enums.PictureReviewStatusEnum;
import io.ryan.picspace.model.vo.PictureVO;
import io.ryan.picspace.model.vo.UserVO;
import io.ryan.picspace.service.PictureService;
import io.ryan.picspace.service.SpaceService;
import io.ryan.picspace.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author ryan
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2025-09-16 18:33:32
 */
@Service
@Slf4j
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureService {

    @Resource
    private PictureManager pictureManager;

    @Resource
    private UserService userService;

    @Resource
    private SpaceService spaceService;

    @Resource
    TransactionTemplate transactionTemplate;

    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        // 修改数据时，id 不能为空，有参数则校验
        ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id 不能为空");
        // 如果传递了 url，才校验
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url 过长");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }

    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) throws IOException {
        // 空校验
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        ThrowUtils.throwIf(pictureUploadRequest == null, ErrorCode.PARAMS_ERROR);

        //如果上传到私有空间，校验空间是否存在且有权限上传
        Long spaceId = pictureUploadRequest.getSpaceId();
        if (spaceId != null) {
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
            //校验是否有权限上传到此空间
//            if (!loginUser.getId().equals(space.getUserId())) {
//                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限上传到此空间");
//            }
            if (space.getTotalCount() > space.getMaxCount() || space.getTotalSize() > space.getMaxSize()) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "空间容量已满，请联系管理员");
            }
        }


        // 判断是新增还是删除
        Long pictureId = pictureUploadRequest.getId();
        // 如果是更新，判断图片是否存在
        if (pictureId != null) {
            Picture oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限操作");
            }
            // 防止公共空间与私人空间的图片互相更新
            if (spaceId == null) {
                if (oldPicture.getSpaceId() != null) {
                    spaceId = oldPicture.getSpaceId();
                }
            } else {
                // 如果携带了 spaceId，必须和原来图片的 spaceId 一致, 否则是伪造请求
                if (!oldPicture.getSpaceId().equals(spaceId))
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "非法操作");
            }
        }

        //根据输入源类型，调用不同的上传方法
        UploadPictureResult uploadPictureResult;
        if (inputSource instanceof MultipartFile) {
            // 上传图片，得到图片信息
            uploadPictureResult = pictureManager.upload((MultipartFile) inputSource, loginUser.getId());
        } else if (inputSource instanceof String) {
            ThrowUtils.throwIf(pictureUploadRequest.getFileUrl() == null, ErrorCode.NO_AUTH_ERROR);
            uploadPictureResult = pictureManager.uploadByUrl((String) inputSource, loginUser.getId());
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的上传类型");
        }

        // 构造要入库的图片信息
        Picture picture = new Picture();

        picture.setUrl(uploadPictureResult.getUrl());
        picture.setName(pictureUploadRequest.getPicName());
        if (uploadPictureResult.getPicName() == null)
            picture.setName("image-" + System.currentTimeMillis());
        else
            picture.setName(uploadPictureResult.getPicName());
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setHash(uploadPictureResult.getHash());
        picture.setUserId(loginUser.getId());
        picture.setSpaceId(spaceId);
        // 操作数据库
        // 如果 pictureId 不为空，表示更新，否则是新增
        if (pictureId != null) {
            // 如果是更新，需要补充 id 和编辑时间
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        // 补全审核信息
        this.fillReviewParams(picture, loginUser);


        Long finalSpaceId = spaceId;
        transactionTemplate.execute((transactionStatus) -> {
            boolean result = this.saveOrUpdate(picture);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败，数据库操作失败");
            if (finalSpaceId != null) {
                // 更新空间的使用额度
                boolean update = spaceService.lambdaUpdate()
                        .eq(Space::getId, finalSpaceId)
                        .setSql("totalSize = totalSize + " + picture.getPicSize())
                        .setSql("totalCount = totalCount + 1")
                        .update();
                ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "额度更新失败");
            }
            return picture;
        });
        return PictureVO.objToVo(picture);
    }

    @Override
    public void fillReviewParams(Picture picture, User user) {
        // 如果是管理员或者上传到私人空间，直接通过审核
        if (userService.isAdmin(user) || picture.getSpaceId() != null) {
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewerId(user.getId());
            picture.setReviewTime(new Date());
        } else {
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }

    }

    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
        // 对象转封装类
        PictureVO pictureVO = PictureVO.objToVo(picture);
        // 关联查询用户信息
        Long userId = picture.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            pictureVO.setUserVO(userVO);
        }
        return pictureVO;
    }

    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }
        // 对象列表 => 封装对象列表
        List<PictureVO> pictureVOList = pictureList.stream()
                .map(PictureVO::objToVo)
                .collect(Collectors.toList());
        // 1. 关联查询用户信息
        // 1,2,3,4
        Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        // 1 => user1, 2 => user2
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息
        pictureVOList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureVO.setUserVO(userService.getUserVO(user));
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }

    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (pictureQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewerId = pictureQueryRequest.getReviewerId();
        Long userId = pictureQueryRequest.getUserId();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();
        Long spaceId = pictureQueryRequest.getSpaceId();
        Boolean publicFlag = pictureQueryRequest.getPublicFlag();

        // 从多字段中搜索
        if (StrUtil.isNotBlank(searchText)) {
            // 需要拼接查询条件
            // and (name like "%xxx%" or introduction like "%xxx%")
            queryWrapper.and(
                    qw -> qw.like("name", searchText)
                            .or()
                            .like("introduction", searchText)
            );
        }
        // 是否是公共图片
        queryWrapper.isNull(publicFlag, "spaceId");
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "spaceId", spaceId);

        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);

        queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.eq(StrUtil.isNotEmpty(reviewMessage), "reviewMessage", reviewMessage);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), "reviewerId", reviewerId);

        // JSON 数组查询
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public Boolean editPicture(PictureUpdateRequest pictureEditRequest, User loginUser) {
        // 在此处将实体类和 DTO 进行转换
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureEditRequest, picture);
        // 注意将 list 转为 string
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        // 设置编辑时间
        picture.setEditTime(new Date());
        // 数据校验
        this.validPicture(picture);

        // 补全审核参数
        this.fillReviewParams(picture, loginUser);

        // 判断是否存在
        long id = pictureEditRequest.getId();
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 校验权限, 本和和管理员可修改以及空间管理员可修改
        this.checkPictureAuth(loginUser, oldPicture);
        // 操作数据库
        return this.updateById(picture);

    }

    @Override
    public Boolean deletePicture(DeleteRequest deleteRequest, User loginUser) {
        Long id = deleteRequest.getId();
        // 判断是否存在
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 权限校验
        this.checkPictureAuth(loginUser, oldPicture);
        // 操作数据库

        Long finalSpaceId = oldPicture.getSpaceId();
        transactionTemplate.execute((transactionStatus) -> {
            boolean result = this.removeById(id);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败，数据库操作失败");
            if (finalSpaceId != null) {
                boolean update = spaceService.lambdaUpdate()
                        .eq(Space::getId, finalSpaceId)
                        .setSql("totalSize = totalSize - " + oldPicture.getPicSize())
                        .setSql("totalCount = totalCount-1")
                        .update();
                ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "图片上传失败，空间数据更新失败");
            }
            return true;
        });

        return true;
    }

    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        Long pictureId = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        String reviewMessage = pictureReviewRequest.getReviewMessage();

        PictureReviewStatusEnum enumByValue = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
        if (pictureId == null || enumByValue == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Picture oldPicture = this.getById(pictureId);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.PARAMS_ERROR);
        if (oldPicture.getReviewStatus().equals(reviewStatus))
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请勿重复审核");
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureReviewRequest, picture);
        picture.setReviewerId(loginUser.getId());
        picture.setReviewTime(new Date());
        boolean b = this.updateById(picture);
        ThrowUtils.throwIf(!b, ErrorCode.OPERATION_ERROR);
    }


    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
        // 校验参数
        String searchText = pictureUploadByBatchRequest.getSearchText();
        Integer count = pictureUploadByBatchRequest.getCount();
        ThrowUtils.throwIf(count > 30, ErrorCode.PARAMS_ERROR, "最多 30 条");
        // 名称前缀默认等于搜索关键词
        String namePrefix = pictureUploadByBatchRequest.getNamePrefix();
        if (StrUtil.isBlank(namePrefix)) {
            namePrefix = searchText;
        }
        // 抓取内容
        String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1", searchText);
        Document document;
        try {
            document = Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            log.error("获取页面失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取页面失败");
        }
        // 解析内容
        Element div = document.getElementsByClass("dgControl").first();
        if (ObjUtil.isEmpty(div)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取元素失败");
        }
        assert div != null;
        Elements imgElementList = div.select("img.mimg");
        // 遍历元素，依次处理上传图片
        int uploadCount = 0;
        for (Element imgElement : imgElementList) {
            String fileUrl = imgElement.attr("src");
            if (StrUtil.isBlank(fileUrl)) {
                log.info("当前链接为空，已跳过：{}", fileUrl);
                continue;
            }
            // 处理图片的地址，防止转义或者和对象存储冲突的问题
            int questionMarkIndex = fileUrl.indexOf("?");
            if (questionMarkIndex > -1) {
                fileUrl = fileUrl.substring(0, questionMarkIndex);
            }
            // 上传图片
            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
            pictureUploadRequest.setFileUrl(fileUrl);
            pictureUploadRequest.setPicName(namePrefix + (uploadCount + 1));
            try {
                PictureVO pictureVO = this.uploadPicture(pictureUploadRequest.getFileUrl(), pictureUploadRequest, loginUser);
                log.info("图片上传成功，id = {}", pictureVO.getId());
                uploadCount++;
            } catch (Exception e) {
                log.error("图片上传失败", e);
                continue;
            }
            if (uploadCount >= count) {
                break;
            }
        }
        return uploadCount;
    }

    @Override
    public void checkPictureAuth(User user, Picture picture) {
        Long spaceId = picture.getSpaceId();
        Long loginUserId = user.getId();
        if (spaceId == null) {
            // 公共图库仅本人和管理员可操作
            if (!picture.getUserId().equals(loginUserId) && !userService.isAdmin(user)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        } else {
            // 私人空间，仅本人可操作
            if (!picture.getUserId().equals(loginUserId)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }
    }


}




