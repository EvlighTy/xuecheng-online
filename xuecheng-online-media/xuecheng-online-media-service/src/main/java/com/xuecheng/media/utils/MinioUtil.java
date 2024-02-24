package com.xuecheng.media.utils;

import com.xuecheng.base.constant.ExMsgConstant;
import com.xuecheng.base.constant.MinioExMsg;
import com.xuecheng.base.exception.CustomException;
import com.xuecheng.media.model.dto.UploadFileDTO;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class MinioUtil {

    private static MinioClient minioClient;

    @Autowired
    public void setMinioClient(MinioClient minioClient) {
        MinioUtil.minioClient = minioClient;
    }

    //上传文件
    public static boolean uploadMediaFilesToMinIO(String localFilePath,String mimeType,String bucket, String objectPath){
        log.info("开始上传文件");
        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectPath)
                    .filename(localFilePath)
                    .contentType(mimeType)
                    .build();
            minioClient.uploadObject(uploadObjectArgs);
            log.info("上传文件到minio成功,bucket:{},objectName:{}",bucket,objectPath);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传文件到minio出错,bucket:{},objectName:{},错误原因:{}",bucket,objectPath,e.getMessage(),e);
        }
        return false;
    }

    //检查文件是否存在
    public static Boolean checkFileExist(String bucketName, String filePath) {
        log.info("开始检查文件是否存在");
        StatObjectArgs statObjectArgs = StatObjectArgs.builder()
                .bucket(bucketName)
                .object(filePath)
                .build();
        try {
            minioClient.statObject(statObjectArgs);
            return true;
        } catch (MinioException e) {
            if(e.getMessage().equals("Object does not exist")) return false;
            throw new RuntimeException(e);
        }catch (Exception e){
            throw new CustomException(e.getMessage());
        }
    }

    //删除单个文件
    public static void clearFiles(String bucketName,String filePath) {
        RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(filePath)
                .build();
        try {
            minioClient.removeObject(removeObjectArgs);
        }catch (Exception e){
            log.info("删除单个文件失败");
            e.printStackTrace();
        }
    }

    //清除分块文件
    public static void clearChunkFiles(String chunkFileFolderPath,int chunkTotal,String bucketName){
        log.info("开始清除分块文件");
        try {
            List<DeleteObject> deleteObjects = Stream.iterate(0, i -> ++i)
                    .limit(chunkTotal)
                    .map(i -> new DeleteObject(chunkFileFolderPath+i))
                    .collect(Collectors.toList());
            RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder()
                    .bucket(bucketName)
                    .objects(deleteObjects)
                    .build();
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
            results.forEach(r->{
                DeleteError deleteError = null;
                try {
                    deleteError = r.get();
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("清除分块文件失败,objectname:{}",deleteError.objectName(),e);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            log.error("清除分块文件失败,chunkFileFolderPath:{}",chunkFileFolderPath,e);
        }
    }

    //下载文件
    public static File downloadFileFromMinIO(String bucket, String objectName){
        log.info("开始下载文件");
        //临时文件
        File minioFile;
        FileOutputStream outputStream = null;
        try{
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            //创建临时文件
            minioFile=File.createTempFile("minio", ".merge");
            outputStream = new FileOutputStream(minioFile);
            IOUtils.copy(stream,outputStream);
            return minioFile;
        } catch (Exception e) {
            log.info("下载文件出错");
            e.printStackTrace();
        }finally {
            if(outputStream!=null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    //合并分块文件
    public static String mergeChunkFile(String fileMd5, List<ComposeSource> composeSources, UploadFileDTO uploadFileDTO,String bucketName){
        log.info("开始合并分块文件");
        //获取合并文件路径
        String filename = uploadFileDTO.getFilename();
        String extension = filename.substring(filename.lastIndexOf("."));
        String mergeFilePath = getMergeFilePath(fileMd5, extension);
        //合并文件
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket(bucketName)
                .object(mergeFilePath)
                .sources(composeSources)
                .build();
        try {
            minioClient.composeObject(composeObjectArgs);
        } catch (Exception e) {
            log.info("合并文件出错:{}",e.getMessage());
            e.printStackTrace();
            throw new CustomException(MinioExMsg.MERGE_FAILED);
        }
        return mergeFilePath;
    }

    //获取合并文件路径
    private static String getMergeFilePath(String fileMd5,String extension) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + fileMd5 + extension;
    }

}
