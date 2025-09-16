package io.ryan.picspace.manager;

import cn.hutool.core.codec.Base62;
import cn.hutool.core.util.HashUtil;
import io.ryan.picspace.common.utils.ImageUtils;
import io.ryan.picspace.model.dto.picture.UploadPictureResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.IOException;

@Component // 或 @Component，默认就是单例
@Slf4j
public class PictureManager {

    @Autowired
    FileManager fileManager;

    public UploadPictureResult upload(MultipartFile file, Long userId) throws IOException {
        long l = HashUtil.murmur64(file.getBytes());
        String fileName = "images/" + userId + "/" + Base62.encode(Long.toString(l)) + "-" + file.getOriginalFilename();
        String upload = fileManager.upload(file, fileName);
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        uploadPictureResult.setUrl(upload);
        uploadPictureResult.setPicName(fileName);
        uploadPictureResult.setPicSize(file.getSize());
        BufferedImage imageDimension = ImageUtils.getImageDimension(file.getInputStream());
        uploadPictureResult.setPicWidth(imageDimension.getWidth());
        uploadPictureResult.setPicHeight(imageDimension.getHeight());
        uploadPictureResult.setPicScale((double) (imageDimension.getWidth() / imageDimension.getHeight()));
        uploadPictureResult.setPicFormat(file.getContentType());

        return uploadPictureResult;

    }

}