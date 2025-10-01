package io.ryan.picspace.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import groovy.lang.Lazy;
import io.ryan.picspace.common.utils.Validator;
import io.ryan.picspace.exception.BusinessException;
import io.ryan.picspace.exception.ErrorCode;
import io.ryan.picspace.exception.ThrowUtils;
import io.ryan.picspace.mapper.mapper.SpaceMapper;
import io.ryan.picspace.model.dto.space.SpaceAddRequest;
import io.ryan.picspace.model.dto.space.SpaceQueryRequest;
import io.ryan.picspace.model.dto.space.SpaceUserAddRequest;
import io.ryan.picspace.model.entity.Space;
import io.ryan.picspace.model.entity.User;
import io.ryan.picspace.model.enums.SpaceLevelEnum;
import io.ryan.picspace.model.enums.SpaceRoleEnum;
import io.ryan.picspace.model.enums.SpaceTypeEnum;
import io.ryan.picspace.model.vo.SpaceVO;
import io.ryan.picspace.model.vo.UserVO;
import io.ryan.picspace.service.SpaceService;
import io.ryan.picspace.service.SpaceUserService;
import io.ryan.picspace.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author ryan
 * @description 针对表【space(空间)】的数据库操作Service实现
 * @createDate 2025-09-21 13:07:13
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceService {
    @Resource
    UserService userService;

    @Resource
    TransactionTemplate transactionTemplate;

    @Resource
    SpaceUserService spaceUserService;

    @Override
    public long addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        // 1. 填充参数默认值
        Space space = new Space();
        if (spaceAddRequest.getSpaceName() == null) {
            spaceAddRequest.setSpaceName("默认空间");
        }
        if (spaceAddRequest.getSpaceLevel() == null) {
            spaceAddRequest.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        if (spaceAddRequest.getSpaceType() == null) {
            spaceAddRequest.setSpaceType(SpaceTypeEnum.PRIVATE.getValue());
        }
        if (spaceAddRequest.getSpaceLevel() == null) {
            spaceAddRequest.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        } else {
            SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceAddRequest.getSpaceLevel());
            if (spaceLevelEnum == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "spaceLevel 参数错误");
            } else {
                spaceAddRequest.setSpaceLevel(spaceLevelEnum.getValue());
            }
        }
        BeanUtils.copyProperties(spaceAddRequest, space);
        space.setUserId(loginUser.getId());
        this.fillSpaceBySpaceLevel(space);
        // 2. 校验参数
        this.validSpace(space, true);
        // 3. 校验权限，非管理员只能创建普通级别的空间
        if (!userService.isAdmin(loginUser) && !space.getSpaceLevel().equals(SpaceLevelEnum.COMMON.getValue())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 4. 控制同一用户只能创建一个私有空间
        String lock = String.valueOf(loginUser.getId()).intern();
        synchronized (lock) {
            Long newSpaceId = transactionTemplate.execute(status -> {
                // 查询是否已经存在
                Long spaceCount = this.lambdaQuery()
                        .eq(Space::getUserId, loginUser.getId())
                        .eq(Space::getSpaceLevel, space.getSpaceLevel())
                        .count();
                if (space.getSpaceLevel().equals(SpaceLevelEnum.COMMON.getValue())) {
                    ThrowUtils.throwIf(spaceCount >= 5, ErrorCode.OPERATION_ERROR, "普通空间最多创建5个");
                }
                // 创建
                boolean result = this.save(space);
                ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "创建失败，请稍后重试");

                //将用户添加作为空间管理员
                SpaceUserAddRequest spaceUserAddRequest = new SpaceUserAddRequest();
                spaceUserAddRequest.setSpaceId(space.getId());
                spaceUserAddRequest.setUserId(space.getUserId());
                spaceUserAddRequest.setSpaceRole(SpaceRoleEnum.ADMIN.getValue());
                spaceUserService.addSpaceUser(spaceUserAddRequest);
                return space.getId();
            });
            return Optional.ofNullable(newSpaceId).orElse(-1L);
        }
    }

    @Override
    public void validSpace(Space space, boolean add) {
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        String spaceName = space.getSpaceName();
        // 修改数据时，id 不能为空，有参数则校验
        if (add) {
            ThrowUtils.throwIf(ObjUtil.isNull(spaceName), ErrorCode.PARAMS_ERROR, "spaceName 不能为空");
        } else {
            ThrowUtils.throwIf(spaceName != null && !Validator.validNaming(spaceName), ErrorCode.PARAMS_ERROR, "spaceName 不能为空且长度不能超过30");
        }

        if (space.getSpaceLevel() != null) {
            SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
            ThrowUtils.throwIf(ObjUtil.isNull(spaceLevelEnum), ErrorCode.PARAMS_ERROR, "spaceLevel 参数错误");
        }
        if (space.getSpaceType() != null) {
            SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(space.getSpaceType());
            ThrowUtils.throwIf(ObjUtil.isNull(spaceTypeEnum), ErrorCode.PARAMS_ERROR, "spaceType 参数错误");
        }
    }

    @Override
    public SpaceVO getSpaceVO(Space space, HttpServletRequest request) {
        // 对象转封装类
        SpaceVO spaceVO = SpaceVO.objToVo(space);
        // 关联查询用户信息
        Long userId = spaceVO.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            spaceVO.setUser(userVO);
        }
        return spaceVO;
    }

    @Override
    public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request) {
        List<Space> spaces = spacePage.getRecords();
        Page<SpaceVO> spaceVOPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
        if (CollUtil.isEmpty(spaces)) {
            return spaceVOPage;
        }
        // 对象列表 => 封装对象列表
        List<SpaceVO> spaceVOList = spaces.stream()
                .map(SpaceVO::objToVo)
                .toList();
        // 1. 关联查询用户信息
        // 1,2,3,4
        Set<Long> userIdSet = spaces.stream().map(Space::getUserId).collect(Collectors.toSet());
        // 1 => user1, 2 => user2
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息
        spaceVOList.forEach(spaceVO -> {
            Long userId = spaceVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            spaceVO.setUser(userService.getUserVO(user));
        });
        spaceVOPage.setRecords(spaceVOList);
        return spaceVOPage;
    }

    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {


        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        if (spaceQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = spaceQueryRequest.getId();
        Long userId = spaceQueryRequest.getUserId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        String sortField = spaceQueryRequest.getSortField();
        String sortOrder = spaceQueryRequest.getSortOrder();
        Integer spaceType = spaceQueryRequest.getSpaceType();

        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceType), "spaceType", spaceType);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceLevel), "spaceLevel", spaceLevel);
        queryWrapper.eq(StrUtil.isNotEmpty(spaceName), "spaceName", spaceName);

        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());

        if (spaceLevelEnum != null) {
            long maxCount = spaceLevelEnum.getMaxCount();
            long maxSize = spaceLevelEnum.getMaxSize();
            if (space.getMaxCount() == null) {
                space.setMaxCount(maxCount);
            }
            if (space.getMaxSize() == null) {
                space.setMaxSize(maxSize);
            }
        }

    }
}




