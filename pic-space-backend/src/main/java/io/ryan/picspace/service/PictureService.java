package io.ryan.picspace.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.ryan.picspace.model.dto.picture.PictureQueryRequest;
import io.ryan.picspace.model.dto.picture.PictureReviewRequest;
import io.ryan.picspace.model.dto.picture.PictureUploadByBatchRequest;
import io.ryan.picspace.model.dto.picture.PictureUploadRequest;
import io.ryan.picspace.model.entity.Picture;
import io.ryan.picspace.model.entity.User;
import io.ryan.picspace.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author ryan
 * @description 针对表【picture(图片)】的数据库操作Service
 * @createDate 2025-09-16 18:33:32
 */
public interface PictureService extends IService<Picture> {

    /**
     * 校验图片
     *
     * @param picture
     */
    void validPicture(Picture picture);

    /**
     * 上传图片
     *
     * @param multipartFile
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(MultipartFile multipartFile,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser) throws IOException;

    /**
     * 通过 URL上传图片
     *
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(PictureUploadRequest pictureUploadRequest,
                            User loginUser) throws IOException;

    /**
     * 获取图片包装类（单条）
     *
     * @param picture
     * @param request
     * @return
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * 获取图片包装类（分页）
     *
     * @param picturePage
     * @param request
     * @return
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 获取查询对象
     *
     * @param pictureQueryRequest
     * @return
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);


    /**
     * 图片审核
     *
     * @param pictureReviewRequest
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    void fillReviewParams(Picture picture, User loginUser);

    /**
     * 批量抓取和创建图片
     *
     * @param pictureUploadByBatchRequest
     * @param loginUser
     * @return 成功创建的图片数
     */
    Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest,
                                 User loginUser);
}
