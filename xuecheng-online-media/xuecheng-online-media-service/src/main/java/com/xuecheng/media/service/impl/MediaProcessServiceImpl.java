package com.xuecheng.media.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.exmsg.CommonExMsg;
import com.xuecheng.base.exception.CustomException;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessHistoryMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.model.po.MediaProcessHistory;
import com.xuecheng.media.service.MediaProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
@Slf4j
@Service
public class MediaProcessServiceImpl extends ServiceImpl<MediaProcessMapper, MediaProcess> implements MediaProcessService {

    @Autowired
    private MediaProcessHistoryMapper mediaProcessHistoryMapper;

    @Autowired
    private MediaFilesMapper mediaFilesMapper;

    //获取待处理任务
    @Override
    public List<MediaProcess> getList(int shardIndex, int shardTotal, int count) {
        LambdaQueryWrapper<MediaProcess> queryWrapper = new LambdaQueryWrapper<MediaProcess>()
                .apply("id % {0} = {1}", shardTotal, shardIndex)
                .and(i -> i.eq(MediaProcess::getStatus, "1")
                        .or()
                        .eq(MediaProcess::getStatus, "3"))
                .lt(MediaProcess::getFailCount, 3)
                .last("limit " + count);
        return list(queryWrapper);
    }

    //更改任务状态为处理中
    @Override
    public Boolean startTask(Long id) {
        LambdaUpdateWrapper<MediaProcess> updateWrapper = new LambdaUpdateWrapper<MediaProcess>()
                .eq(MediaProcess::getId, id)
                .and(wrapper -> wrapper.eq(MediaProcess::getStatus, "1")
                        .or()
                        .eq(MediaProcess::getStatus, "3"))
                .le(MediaProcess::getFailCount, 3)
                .set(MediaProcess::getStatus, "4");
        return update(updateWrapper);
    }

    //任务处理后更新信息
    @Transactional
    @Override
    public void afterVideoTransCode(Long taskId, String fileId, String result, String url, String failedMessage) {
        //业务逻辑校验
        MediaProcess mediaProcess = getById(taskId);
        if(mediaProcess==null) return;
        log.info("处理结果:{}",result);
        if(result.equals("3")){
            //处理失败
            /*更新任务信息*/
            LambdaUpdateWrapper<MediaProcess> updateWrapper = new LambdaUpdateWrapper<MediaProcess>()
                    .eq(MediaProcess::getId, taskId)
                    .set(MediaProcess::getStatus, "3")
                    .set(MediaProcess::getFailCount, mediaProcess.getFailCount() + 1)
                    .set(MediaProcess::getErrormsg, failedMessage);
            boolean update = update(updateWrapper);
            if(!update) throw new CustomException(CommonExMsg.UPDATE_FAILED);
        }else{
            //处理成功
            /*保存历史任务信息*/
            MediaProcessHistory mediaProcessHistory = BeanUtil.copyProperties(mediaProcess, MediaProcessHistory.class);
            mediaProcessHistory.setId(null);
            mediaProcessHistory.setStatus("2");
            mediaProcessHistory.setFinishDate(LocalDateTime.now());
            mediaProcessHistory.setUrl(url);
            int insert = mediaProcessHistoryMapper.insert(mediaProcessHistory);
            if(insert!=1) throw new CustomException(CommonExMsg.INSERT_FAILED);
            /*删除任务信息*/
            removeById(taskId);
            /*更新媒资文件url*/
            LambdaUpdateWrapper<MediaFiles> updateWrapper = new LambdaUpdateWrapper<MediaFiles>()
                    .eq(MediaFiles::getFileId, fileId)
                    .set(MediaFiles::getUrl, url);
            int update = mediaFilesMapper.update(null, updateWrapper);
            if(update!=1) throw new CustomException(CommonExMsg.UPDATE_FAILED);
        }
    }

}
