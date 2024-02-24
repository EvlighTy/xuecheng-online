package com.xuecheng.media.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.constant.ExMsgConstant;
import com.xuecheng.base.constant.MediaExMsg;
import com.xuecheng.base.constant.MinioExMsg;
import com.xuecheng.base.enumeration.ObjectAuditStatus;
import com.xuecheng.base.exception.CustomException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.result.PageResult;
import com.xuecheng.base.utils.StringUtil;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDTO;
import com.xuecheng.media.model.dto.UploadFileDTO;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.model.vo.UploadFileResultVO;
import com.xuecheng.media.service.MediaFileService;
import com.xuecheng.media.utils.MinioUtil;
import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @description TODO
 * @author Mr.M
 * @date 2022/9/10 8:58
 * @version 1.0
 */
@Slf4j
@Service
public class MediaFileServiceImpl extends ServiceImpl<MediaFilesMapper,MediaFiles> implements MediaFileService {

    @Autowired
    private MediaFilesMapper mediaFilesMapper;

    @Autowired
    private MediaProcessMapper mediaProcessMapper;

    @Lazy
    @Autowired
    private MediaFileService mediaFileService;

    //存储其他
    @Value("${minio.bucket.files}")
    private String bucketFiles;

    //存储视频
    @Value("${minio.bucket.videofiles}")
    private String bucketVideoFiles;

    //查询
    @Override
    public PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDTO queryMediaParamsDto) {
/*        // 分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        //构建查询条件对象

        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集*/
        String filename = queryMediaParamsDto.getFilename();
        String fileType = queryMediaParamsDto.getType();
        String auditStatus = queryMediaParamsDto.getAuditStatus();
        Page<MediaFiles> page = Page.of(pageParams.getPageNo(), pageParams.getPageSize());
        Page<MediaFiles> result = lambdaQuery()
                .like(filename!=null && !filename.isEmpty(), MediaFiles::getFilename, filename)
                .eq(fileType!=null && !fileType.isEmpty(),MediaFiles::getFileType, fileType)
                .eq(auditStatus!=null && !auditStatus.isEmpty(),MediaFiles::getAuditStatus, auditStatus)
                .page(page);
        return new PageResult<>(result.getTotal(),result.getRecords());
    }

    //上传图片
    @Override
    public UploadFileResultVO uploadImage(UploadFileDTO uploadFileDTO, String localFilePath) {
        //机构id
        long companyId = 1232141425L;
        //mimeType
        String filename = uploadFileDTO.getFilename();
        String extension = filename.substring(filename.lastIndexOf("."));
        String mimeType = getMimeType(extension);
        //文件目录
        String path = getDefaultFolderPath();
        //md5
        String md5 = getFileMD5(new File(localFilePath));
        //文件路径
        String objectPath = path + md5 + extension;
        //上传文件
        boolean uploaded = MinioUtil.uploadMediaFilesToMinIO(localFilePath, mimeType, bucketFiles, objectPath);
        if(!uploaded) throw new CustomException(MinioExMsg.UPLOAD_FAILED);
        //保存文件信息
        MediaFiles mediaFiles = mediaFileService.saveOrGetFileInfo(uploadFileDTO, md5, companyId, objectPath, bucketFiles);
        //封装返回数据
        UploadFileResultVO uploadFileResultVO = BeanUtil.copyProperties(mediaFiles, UploadFileResultVO.class);
        return uploadFileResultVO;
    }

    //检查文件是否存在
    @Override
    public Boolean checkFile(String fileMd5) {
        MediaFiles mediaFiles = getById(fileMd5);
        if(mediaFiles!=null){
            return MinioUtil.checkFileExist(mediaFiles.getBucket(),mediaFiles.getFilePath());
        }
        return false;
    }

    //检查分块文件是否存在
    @Override
    public Boolean checkChunk(String fileMd5, int chunkIndex) {
        String path = getChunkFileFolderPath(fileMd5);
        return MinioUtil.checkFileExist(bucketVideoFiles, path + chunkIndex);
    }

    //合并文件
    @Override
    public Boolean mergeChunks(String fileMd5, int chunkTotal, UploadFileDTO uploadFileDTO) {
        //获取机构id
        long companyId = 1232141425L;
        //获取分块文件目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        List<ComposeSource> composeSources = Stream.iterate(0, i -> ++i)
                .limit(chunkTotal)
                .map(i -> ComposeSource.builder()
                        .bucket(bucketVideoFiles)
                        .object(chunkFileFolderPath + i)
                        .build())
                .collect(Collectors.toList());
        //合并分块文件
        String mergeFilePath = MinioUtil.mergeChunkFile(fileMd5, composeSources, uploadFileDTO, bucketVideoFiles);
        //校验文件一致性
        File file = MinioUtil.downloadFileFromMinIO(bucketVideoFiles, mergeFilePath);
        try (FileInputStream fileInputStream = new FileInputStream(file)){
            String md5Hex = DigestUtils.md5Hex(fileInputStream);
            if(!fileMd5.equals(md5Hex)) return false;
        }catch (Exception e){
            log.info("校验文件出错:{}",e.getMessage());
            e.printStackTrace();
            throw new CustomException(MinioExMsg.VERIFY_FAILED);
        }
        //保存文件信息
        uploadFileDTO.setFileSize(file.length());
        MediaFiles mediaFiles = mediaFileService.saveOrGetFileInfo(uploadFileDTO, fileMd5, companyId, mergeFilePath, bucketVideoFiles);
        if (mediaFiles==null) return false;
        //清除分块文件
        MinioUtil.clearChunkFiles(chunkFileFolderPath,chunkTotal,bucketVideoFiles);
        return true;
    }

    @Override
    public String preview(String mediaId) {
        MediaFiles mediaFiles = getById(mediaId);
        if(mediaFiles==null) throw new CustomException(MediaExMsg.MEDIA_NO_EXIST);
        if(StringUtils.isEmpty(mediaFiles.getStatus())) throw new CustomException(MediaExMsg.MEDIA_NO_TRANS);
        return mediaFiles.getUrl();
    }

    //上传分块文件
    @Override
    public Boolean uploadChunk(MultipartFile multipartFile, String fileMd5, int chunkIndex) {
        String objectPath = getChunkFileFolderPath(fileMd5) + chunkIndex;
        String mimeType = getMimeType(null);
        String localFilePath;
        try {
            localFilePath = getLocalFilePath(multipartFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return MinioUtil.uploadMediaFilesToMinIO(localFilePath, mimeType, bucketVideoFiles, objectPath);
    }

    /*=================================================================================================================*/

    //保存或获取文件信息
    @Transactional
    public MediaFiles saveOrGetFileInfo(UploadFileDTO uploadFileDTO, String md5, long companyId, String objectPath, String bucketName) {
        MediaFiles mediaFiles = getById(md5);
        if(mediaFiles==null){
            //保存文件信息到数据库
            mediaFiles = BeanUtil.copyProperties(uploadFileDTO, MediaFiles.class);
            mediaFiles.setId(md5);
            mediaFiles.setFileId(md5);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setFileSize(uploadFileDTO.getFileSize());
            mediaFiles.setBucket(bucketName);
            mediaFiles.setFilePath(objectPath);
            mediaFiles.setUrl("/"+bucketName+"/"+ objectPath);
            mediaFiles.setStatus("1");
            mediaFiles.setAuditStatus(ObjectAuditStatus.REVIEW_PASSED.getValue());
            boolean save = save(mediaFiles);
            if(!save) throw new CustomException(ExMsgConstant.INSERT_FAILED);
        }
        //记录待处理任务
        addWaitTask(uploadFileDTO, mediaFiles);
        return mediaFiles;
    }

    //记录待处理任务
    private void addWaitTask(UploadFileDTO uploadFileDTO, MediaFiles mediaFiles) {
        String filename = uploadFileDTO.getFilename();
        String extension = filename.substring(filename.lastIndexOf("."));
        String mimeType = getMimeType(extension);
        if(mimeType.equals("video/x-msvideo")){
            mediaFiles.setId(null);
            MediaProcess mediaProcess = BeanUtil.copyProperties(mediaFiles, MediaProcess.class);
            mediaProcess.setStatus("1");
            mediaProcess.setFailCount(0);
            mediaProcess.setUrl(null);
            int insert = mediaProcessMapper.insert(mediaProcess);
            if(insert!=1) throw new CustomException(ExMsgConstant.INSERT_FAILED);
        }
    }

    //根据文件扩展名获取mimeType
    private String getMimeType(String extension){
        if(extension == null) extension = "";
        //根据扩展名取出mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        //通用mimeType字节流
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if(extensionMatch!=null){
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;
    }

    //获取文件默认存储目录路径(年/月/日)
    private String getDefaultFolderPath(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date()).replace("-", "/")+"/";
    }

    //获取文件的md5
    private String getFileMD5(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            return DigestUtils.md5Hex(fileInputStream);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //获取分块文件的目录
    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + "chunk" + "/";
    }

    //获取文件本地路径
    private static String getLocalFilePath(MultipartFile multipartFile) throws IOException {
        File tempFile = File.createTempFile("minio", ".temp");
        multipartFile.transferTo(tempFile);
        return tempFile.getAbsolutePath();
    }

}
