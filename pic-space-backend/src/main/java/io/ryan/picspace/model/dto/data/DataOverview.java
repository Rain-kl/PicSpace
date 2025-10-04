package io.ryan.picspace.model.dto.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;


@Data
@AllArgsConstructor
public class DataOverview implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 图片总数
     */
    private Long pictureCount;
    /**
     * 用户总数
     */
    private Long userCount;
    /**
     * 空间总数
     */
    private Long spaceCount;
}
