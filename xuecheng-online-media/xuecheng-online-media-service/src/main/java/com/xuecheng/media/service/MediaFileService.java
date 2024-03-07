package com.xuecheng.media.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.base.model.result.PageResult;
import com.xuecheng.media.model.dto.QueryMediaParamsDTO;
import com.xuecheng.media.model.dto.UploadFileDTO;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.vo.UploadFileResultVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * @description 媒资文件管理业务类
 * @author Mr.M
 * @date 2022/9/10 8:55
 * @version 1.0
 */
public interface MediaFileService extends IService<MediaFiles> {

    PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDTO queryMediaParamsDto);

    UploadFileResultVO uploadFile(UploadFileDTO uploadFileDTO, String localFilePath, String objectPath);

    MediaFiles saveOrGetFileInfo(UploadFileDTO uploadFileDTO, String md5, long companyId, String objectPath, String bucketName);

    Boolean checkFile(String fileMD5);

    Boolean checkChunk(String fileMd5, int chunkIndex);

    Boolean uploadChunk(MultipartFile file, String fileMd5, int chunkIndex);

    Boolean mergeChunks(String fileMd5, int chunkTotal, UploadFileDTO uploadFileDTO);

    String preview(String mediaId);

    RestResponse deleteMedia(String mediaId);
}
