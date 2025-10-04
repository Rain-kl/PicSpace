package io.ryan.picspace.model.dto.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;


@Data
@AllArgsConstructor
public class DataTrend implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 日期
     */
    private String date;
    /**
     * 新增用户数
     */
    private Long newUserCount;
    /**
     * 新增图片数
     */
    private Long newPictureCount;
    /**
     * 新增空间数
     */
    private Long newSpaceCount;
    private List<BaseDataGraph> userGraph;
    private List<BaseDataGraph> pictureGraph;
    private List<BaseDataGraph> spaceGraph;
}
