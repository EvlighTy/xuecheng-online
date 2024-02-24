package com.xuecheng.media.controller;

import com.xuecheng.base.enumeration.FileType;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.result.PageResult;
import com.xuecheng.media.model.dto.QueryMediaParamsDTO;
import com.xuecheng.media.model.dto.UploadFileDTO;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.vo.UploadFileResultVO;
import com.xuecheng.media.service.MediaFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * @description 媒资文件管理接口
 * @author Mr.M
 * @date 2022/9/6 11:29
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping
public class MediaFilesController {

    @Autowired
    MediaFileService mediaFileService;

    @PostMapping("/files")
    public PageResult<MediaFiles> list(PageParams pageParams, @RequestBody QueryMediaParamsDTO queryMediaParamsDto){
        log.info("查询媒资");
        Long companyId = 1232141425L;
        return mediaFileService.queryMediaFiles(companyId,pageParams,queryMediaParamsDto);
    }

    @PostMapping(value = "/upload/coursefile",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadFileResultVO uploadImage(@RequestPart("filedata") MultipartFile multipartFile) throws IOException {
        log.info("上传课程图片");
        UploadFileDTO uploadFileDTO = UploadFileDTO.builder()
                .filename(multipartFile.getOriginalFilename())
                .fileSize(multipartFile.getSize())
                .fileType(FileType.IMAGE.getValue())
                .build();
        String localFilePath = getLocalFilePath(multipartFile);
        UploadFileResultVO uploadFileResultVO = mediaFileService.uploadImage(uploadFileDTO,localFilePath);
        return uploadFileResultVO;
    }

    private static String getLocalFilePath(MultipartFile multipartFile) throws IOException {
        File tempFile = File.createTempFile("minio", ".temp");
        multipartFile.transferTo(tempFile);
        return tempFile.getAbsolutePath();
    }

}
