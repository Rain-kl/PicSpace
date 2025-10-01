package io.ryan.picspace.model.dto.space;

import lombok.Data;

import java.io.Serializable;

@Data
public class SpaceUserAddRequest implements Serializable {

    /**
     * 空间 ID
     */
    private Long spaceId;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 空间角色：user/editor/admin
     */
    private String spaceRole;

    private static final long serialVersionUID = 1L;
}