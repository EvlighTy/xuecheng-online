package com.xuecheng.media.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.media.model.po.MediaProcess;

import java.util.List;

public interface MediaProcessService extends IService<MediaProcess> {

    List<MediaProcess> getList(int shardIndex, int shardTotal, int count);

    Boolean startTask(Long id);

    void afterVideoTransCode(Long taskId, String fileId, String status, String url, String failedMessage);

}
