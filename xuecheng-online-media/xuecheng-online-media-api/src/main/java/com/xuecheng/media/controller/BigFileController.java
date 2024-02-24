package com.xuecheng.media.controller;

import com.xuecheng.base.enumeration.FileType;
import com.xuecheng.base.model.result.MediaResult;
import com.xuecheng.media.model.dto.UploadFileDTO;
import com.xuecheng.media.service.MediaFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
public class BigFileController {

    @Autowired
    private MediaFileService mediaFileService;

    @PostMapping("/upload/checkfile")
    public MediaResult<Boolean> checkFile(@RequestParam("fileMd5") String fileMd5){
        log.info("检查文件是否存在");
        Boolean result = mediaFileService.checkFile(fileMd5);
        return MediaResult.success(result);
    }


    @PostMapping("/upload/checkchunk")
    public MediaResult<Boolean> checkChunk(@RequestParam("fileMd5") String fileMd5,
                                      @RequestParam("chunk") int chunkIndex){
        log.info("检查分块文件是否存在");
        Boolean result = mediaFileService.checkChunk(fileMd5, chunkIndex);
        return MediaResult.success(result);
    }

    @PostMapping("/upload/uploadchunk")
    public MediaResult<Boolean> uploadChunk(@RequestParam("file") MultipartFile file,
                              @RequestParam("fileMd5") String fileMd5,
                              @RequestParam("chunk") int chunkIndex){
        log.info("上传分块文件");
        Boolean result = mediaFileService.uploadChunk(file, fileMd5, chunkIndex);
        if(result) return MediaResult.success(true);
        else return MediaResult.validfail(false,"上传分块文件失败");
    }

    @PostMapping("/upload/mergechunks")
    public MediaResult<Boolean> mergeChunks(@RequestParam("fileMd5") String fileMd5,
                              @RequestParam("fileName") String fileName,
                              @RequestParam("chunkTotal") int chunkTotal){
        log.info("合并分块文件");
        UploadFileDTO uploadFileDTO = UploadFileDTO.builder()
                .filename(fileName)
                .fileType(FileType.VIDEO.getValue())
                .tags("课程视频")
                .remark("")
                .build();
        Boolean result = mediaFileService.mergeChunks(fileMd5, chunkTotal, uploadFileDTO);
        if(result) return MediaResult.success(true);
        else return MediaResult.validfail(false,"合并分块文件失败");
    }
}
