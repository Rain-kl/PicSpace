package io.ryan.picspace.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import io.ryan.picspace.model.dto.space.SpaceUserAddRequest;
import io.ryan.picspace.model.dto.space.SpaceUserQueryRequest;
import io.ryan.picspace.model.entity.SpaceUser;
import io.ryan.picspace.model.vo.SpaceUserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
 * @author ryan
 * @description 针对表【space_user(空间用户关联)】的数据库操作Service
 * @createDate 2025-09-30 16:32:00
 */
public interface SpaceUserService extends IService<SpaceUser> {

    long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);

    void validSpaceUser(SpaceUser spaceUser, boolean add);

    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);

    SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request);

    List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList);
}
