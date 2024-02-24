package com.xuecheng.media.xxljob;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaProcessService;
import com.xuecheng.media.utils.MinioUtil;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;
@Slf4j
@Component
public class VideoJob {

    @Autowired
    private MediaProcessService mediaProcessService;

    @Value("${videoprocess.ffmpegpath}")
    private String ffmpegPath;

    private CountDownLatch latch;

    private static ThreadPoolExecutor threadPool;

    public VideoJob() {
        //查询cpu数量
        int cpuNum = Runtime.getRuntime().availableProcessors();
        //创建线程池
        VideoJob.threadPool=new ThreadPoolExecutor(
                3, //核心线程数量
                cpuNum, //最大线程数量
                60, //空闲线程最大存活时间值
                TimeUnit.SECONDS, //空闲线程最大存活时间单位
                new ArrayBlockingQueue<>(1), //阻塞队列
                Executors.defaultThreadFactory(), //线程工厂生产线程
                new ThreadPoolExecutor.AbortPolicy() //线程拒绝策略
        );
    }

    @XxlJob("videoJobHandler")
    public void videoJobHandler(){
        //获取分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        //查询待处理任务
        List<MediaProcess> mediaProcesses = mediaProcessService.getList(shardIndex, shardTotal, 5);
        log.info("待处理任务数:[{}]",mediaProcesses.size());
        latch = new CountDownLatch(mediaProcesses.size());
        //批量处理
        mediaProcesses.forEach(mediaProcess -> {
            threadPool.execute(()->{
                try {
                    //开启任务
                    Long taskId = mediaProcess.getId();
                    Boolean start = mediaProcessService.startTask(taskId);
                    if (!start) log.info("抢占任务失败,任务id:{}", taskId);
                    //执行任务(视频转码)
                    /*从minio下载文件*/
                    File file = MinioUtil.downloadFileFromMinIO(mediaProcess.getBucket(), mediaProcess.getFilePath());
                    if (file == null) {
                        log.info("下载文件出错");
                        mediaProcessService.afterVideoTransCode(taskId, mediaProcess.getFileId(), "failed", null, "下载文件出错");
                        return;
                    }
                    /*创建临时文件作为转换后的文件*/
                    File tempFile;
                    try {
                        tempFile = File.createTempFile("minio", ".mp4");
                    } catch (IOException e) {
                        log.info("创建临时文件出错");
                        mediaProcessService.afterVideoTransCode(taskId, mediaProcess.getFileId(), "failed", null, "创建临时文件出错");
                        e.printStackTrace();
                        return;
                    }
                    /*创建工具类对象*/
                    Mp4VideoUtil videoUtil = new Mp4VideoUtil(
                            ffmpegPath,
                            file.getAbsolutePath(),
                            tempFile.getName(),
                            tempFile.getAbsolutePath());
                    /*开始视频转换*/
                    String result = videoUtil.generateMp4();
                    if (!result.equals("success")) {
                        log.info("视频转换出错:{}", result);
                        mediaProcessService.afterVideoTransCode(taskId, mediaProcess.getFileId(), result, null, "视频转换出错");
                        return;
                    }
                    //上传至minio
                    String mp4FilePath = getMP4FilePath(mediaProcess.getFileId(), ".mp4");
                    boolean uploaded = MinioUtil.uploadMediaFilesToMinIO(
                            tempFile.getAbsolutePath(),
                            "video/mp4",
                            mediaProcess.getBucket(),
                            mp4FilePath);
                    if (!uploaded) {
                        mediaProcessService.afterVideoTransCode(taskId, mediaProcess.getFileId(), result, null, "上传MP4视频失败");
                        return;
                    }
                    //保存任务处理结果
                    /*文件url*/
                    String url = "/" + mediaProcess.getBucket() + "/" + mp4FilePath;
                    mediaProcessService.afterVideoTransCode(taskId, mediaProcess.getFileId(), result, url, null);
                }catch (Exception e){
                    e.printStackTrace();
                    mediaProcessService.afterVideoTransCode(mediaProcess.getId(), mediaProcess.getFileId(), "failed", null, null);
                }finally {
                    //标记当前线程已结束
                    latch.countDown();
                }
            });
        });
        //等待其他线程执行结束
        try {
            latch.await(30,TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            log.info("线程阻塞出错");
            throw new RuntimeException(e);
        }
    }

    //获取MP4文件路径
    private static String getMP4FilePath(String fileMd5,String extension) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + fileMd5 + extension;
    }

}
