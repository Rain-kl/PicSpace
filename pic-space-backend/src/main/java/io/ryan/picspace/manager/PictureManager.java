package io.ryan.picspace.manager;

import cn.hutool.core.codec.Base62;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.HashUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import io.ryan.picspace.common.utils.ImageUtils;
import io.ryan.picspace.exception.BusinessException;
import io.ryan.picspace.exception.ErrorCode;
import io.ryan.picspace.exception.ThrowUtils;
import io.ryan.picspace.model.dto.picture.UploadPictureResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

@Component // 或 @Component，默认就是单例
@Slf4j
public class PictureManager {

    @Autowired
    FileManager fileManager;

    public UploadPictureResult upload(MultipartFile file, Long userId) throws IOException {
        this.validPictureFile(file);
        //转换为 webp 格式存储
//        byte[] webpBytes = ImageUtils.toWebp(file.getBytes());

        long l = HashUtil.murmur64(file.getBytes());
        String hash = Base62.encode(Long.toString(l));
        String fileName = "images/" + userId + "/" + hash + "-" + file.getOriginalFilename();
        String upload = fileManager.upload(file, fileName);
        // 保存 webp 副本
//        fileManager.upload(webpBytes, fileName+".webp");
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        // 默认启用 webp 链接
        uploadPictureResult.setUrl(upload);
        uploadPictureResult.setPicName(fileName);
        uploadPictureResult.setPicSize(file.getSize());
        BufferedImage imageDimension = ImageUtils.readImage(file.getInputStream());
        uploadPictureResult.setPicWidth(imageDimension.getWidth());
        uploadPictureResult.setPicHeight(imageDimension.getHeight());
        uploadPictureResult.setPicScale((double) (imageDimension.getWidth() / imageDimension.getHeight()));
        uploadPictureResult.setPicFormat(file.getContentType());
        uploadPictureResult.setHash(hash);

        return uploadPictureResult;

    }

    public UploadPictureResult uploadByUrl(String fileUrl, Long userId) {
        // 验证 URL 有效性
        try (HttpResponse headResponse = fileManager.doHeadRequest(fileUrl)) {

            this.validUrl(fileUrl);
            this.validPictureByHeadResponse(headResponse);

            // 下载图片数据
            byte[] imageBytes = HttpUtil.downloadBytes(fileUrl);

            // 生成哈希值
            long hashValue = HashUtil.murmur64(imageBytes);
            String hash = Base62.encode(Long.toString(hashValue));

            // 从 URL 中提取文件名
            String originalFileName = extractFileNameFromUrl(fileUrl);
            if (StrUtil.isBlank(originalFileName)) {
                originalFileName = "image.jpg"; // 默认文件名
            }

            // 构建文件路径
            String fileName = "images/" + userId + "/" + hash + "-" + originalFileName;

            // 使用 FileManager 的 uploadByUrl 方法上传文件
            String uploadUrl = fileManager.uploadByUrl(fileUrl, fileName);

            // 构建返回结果
            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            uploadPictureResult.setUrl(uploadUrl);
            uploadPictureResult.setPicName(fileName);
            uploadPictureResult.setPicSize((long) imageBytes.length);
            uploadPictureResult.setHash(hash);

            // 获取图片尺寸信息
            try (InputStream inputStream = new ByteArrayInputStream(imageBytes)) {
                BufferedImage imageDimension = ImageUtils.readImage(inputStream);
                uploadPictureResult.setPicWidth(imageDimension.getWidth());
                uploadPictureResult.setPicHeight(imageDimension.getHeight());
                uploadPictureResult.setPicScale((double) (imageDimension.getWidth() / imageDimension.getHeight()));
            }

            // 通过HEAD请求获取Content-Type
            String contentType = headResponse.header("Content-Type");
            uploadPictureResult.setPicFormat(contentType);

            return uploadPictureResult;

        } catch (Exception e) {
            log.error("通过 URL 上传图片失败: {}", fileUrl, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片上传失败");
        }
    }

    /**
     * 从 URL 中提取文件名
     */
    private String extractFileNameFromUrl(String url) {
        try {
            String path = new URL(url).getPath();
            if (StrUtil.isNotBlank(path) && path.contains("/")) {
                String fileName = path.substring(path.lastIndexOf("/") + 1);
                // 如果文件名包含扩展名，直接返回
                if (fileName.contains(".")) {
                    return fileName;
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }


    public void validUrl(String url) {
        //验证非空
        ThrowUtils.throwIf(url == null, ErrorCode.NO_AUTH_ERROR);
        //验证格式
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片链接格式错误");
        }
        //校验协议
        ThrowUtils.throwIf(!url.startsWith("http"), ErrorCode.PARAMS_ERROR, "图片链接必须以 http 开头");
        //发送 head 请求获取文件元数据
    }

    public void validPictureByHeadResponse(HttpResponse response) {
        if (response.getStatus() != HttpStatus.HTTP_OK) {
            return;
        }
        //校验文件类型和大小
        String contentType = response.header("Content-Type");
        if (StrUtil.isNotBlank(contentType)) {
            // 允许的图片类型
            final List<String> ALLOW_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/webp");
            ThrowUtils.throwIf(!ALLOW_CONTENT_TYPES.contains(contentType.toLowerCase()),
                    ErrorCode.PARAMS_ERROR, "文件类型错误");
        }
        String contentLengthStr = response.header("Content-Length");
        if (StrUtil.isNotBlank(contentLengthStr)) {
            try {
                long contentLength = Long.parseLong(contentLengthStr);
                final long ONE_M = 1024 * 1024;
                ThrowUtils.throwIf(contentLength > 2 * ONE_M, ErrorCode.PARAMS_ERROR, "文件大小不能超过 2MB");
            } catch (NumberFormatException e) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小格式异常");
            }
        }

    }


    public void validPictureFile(MultipartFile multipartFile) {
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
        // 1. 校验文件大小
        long fileSize = multipartFile.getSize();
        final long ONE_M = 1024 * 1024L;
        ThrowUtils.throwIf(fileSize > 2 * ONE_M, ErrorCode.PARAMS_ERROR, "文件大小不能超过 2M");
        // 2. 校验文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        // 允许上传的文件后缀
        final List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpeg", "jpg", "png", "webp");
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "文件类型错误");
    }

}