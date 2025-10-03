package io.ryan.picspace.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.ryan.picspace.exception.BusinessException;
import io.ryan.picspace.exception.ErrorCode;
import io.ryan.picspace.exception.ThrowUtils;
import io.ryan.picspace.mapper.SpaceUserMapper;
import io.ryan.picspace.model.dto.space.SpaceUserAddRequest;
import io.ryan.picspace.model.dto.space.SpaceUserQueryRequest;
import io.ryan.picspace.model.entity.Space;
import io.ryan.picspace.model.entity.SpaceUser;
import io.ryan.picspace.model.entity.User;
import io.ryan.picspace.model.enums.SpaceRoleEnum;
import io.ryan.picspace.model.vo.SpaceUserVO;
import io.ryan.picspace.model.vo.SpaceVO;
import io.ryan.picspace.model.vo.UserVO;
import io.ryan.picspace.service.SpaceService;
import io.ryan.picspace.service.SpaceUserService;
import io.ryan.picspace.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author ryan
 * @description 针对表【space_user(空间用户关联)】的数据库操作Service实现
 * @createDate 2025-09-30 16:32:00
 */
@Service
public class SpaceUserServiceImpl extends ServiceImpl<SpaceUserMapper, SpaceUser>
        implements SpaceUserService {

    @Resource
    UserService userService;

    @Resource
    @Lazy
    SpaceService spaceService;

    @Override
    public long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest) {
        // 参数校验
        ThrowUtils.throwIf(spaceUserAddRequest == null, ErrorCode.PARAMS_ERROR);
        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(spaceUserAddRequest, spaceUser);
        validSpaceUser(spaceUser, true);
        // 数据库操作
        try {
            boolean result = this.save(spaceUser);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已在该空间中");
        }
        return spaceUser.getId();
    }

    @Override
    public void validSpaceUser(SpaceUser spaceUser, boolean add) {
        ThrowUtils.throwIf(spaceUser == null, ErrorCode.PARAMS_ERROR);
        // 创建时，空间 id 和用户 id 必填
        Long spaceId = spaceUser.getSpaceId();
        Long userId = spaceUser.getUserId();
        if (add) {
            ThrowUtils.throwIf(ObjectUtil.hasEmpty(spaceId, userId), ErrorCode.PARAMS_ERROR);
            User user = userService.getById(userId);
            ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        }
        // 校验空间角色
        String spaceRole = spaceUser.getSpaceRole();
        SpaceRoleEnum spaceRoleEnum = SpaceRoleEnum.getEnumByValue(spaceRole);
        if (spaceRole != null && spaceRoleEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间角色不存在");
        }
    }

    @Override
    public QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest) {
        QueryWrapper<SpaceUser> queryWrapper = new QueryWrapper<>();
        if (spaceUserQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = spaceUserQueryRequest.getId();
        Long spaceId = spaceUserQueryRequest.getSpaceId();
        Long userId = spaceUserQueryRequest.getUserId();
        String spaceRole = spaceUserQueryRequest.getSpaceRole();
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "spaceId", spaceId);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceRole), "spaceRole", spaceRole);
        return queryWrapper;
    }

    @Override
    public SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request) {
        // 对象转封装类
        SpaceUserVO spaceUserVO = SpaceUserVO.objToVo(spaceUser);
        // 关联查询用户信息
        Long userId = spaceUser.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            spaceUserVO.setUser(userVO);
        }
        // 关联查询空间信息
        Long spaceId = spaceUser.getSpaceId();
        if (spaceId != null && spaceId > 0) {
            Space space = spaceService.getById(spaceId);
            SpaceVO spaceVO = spaceService.getSpaceVO(space, request);
            spaceUserVO.setSpace(spaceVO);
        }
        return spaceUserVO;
    }

    @Override
    public List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList, List<Class<?>> fillClass) {
        // 判断输入列表是否为空
        if (CollUtil.isEmpty(spaceUserList)) {
            return Collections.emptyList();
        }
        // 对象列表 => 封装对象列表
        List<SpaceUserVO> spaceUserVOList = spaceUserList.stream().map(SpaceUserVO::objToVo).collect(Collectors.toList());

        if (CollUtil.contains(fillClass, User.class)) {
            // 1. 收集需要关联查询的用户 ID 和空间 ID
            Set<Long> userIdSet = spaceUserList.stream().map(SpaceUser::getUserId).collect(Collectors.toSet());
            // 2. 批量查询用户和空间
            Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                    .collect(Collectors.groupingBy(User::getId));
            // 3. 填充 SpaceUserVO 的用户和空间信息
            spaceUserVOList.forEach(spaceUserVO -> {
                Long userId = spaceUserVO.getUserId();
                User user = null;
                if (userIdUserListMap.containsKey(userId)) {
                    user = userIdUserListMap.get(userId).get(0);
                }
                spaceUserVO.setUser(userService.getUserVO(user));
            });
        }
        if (CollUtil.contains(fillClass, Space.class)) {
            // 1. 收集需要关联查询的用户 ID 和空间 ID
            Set<Long> spaceIdSet = spaceUserList.stream().map(SpaceUser::getSpaceId).collect(Collectors.toSet());
            // 2. 批量查询用户和空间
            Map<Long, List<Space>> spaceIdSpaceListMap = spaceService.listByIds(spaceIdSet).stream()
                    .collect(Collectors.groupingBy(Space::getId));
            // 3. 填充 SpaceUserVO 的用户和空间信息
            spaceUserVOList.forEach(spaceUserVO -> {
                Long spaceId = spaceUserVO.getSpaceId();
                Space space = null;
                if (spaceIdSpaceListMap.containsKey(spaceId)) {
                    space = spaceIdSpaceListMap.get(spaceId).get(0);
                }
                spaceUserVO.setSpace(SpaceVO.objToVo(space));
            });
        }
        return spaceUserVOList;
    }


}
