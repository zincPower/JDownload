package com.zinc.libdownload.internet.listener;

import com.zinc.libdownload.internet.bean.DownloadingInfo;

/**
 * @author Jiang zinc
 * @date 创建时间：2017/11/14
 * @description 下载信息的接口
 */

public interface DownloadInfoListener {

    /**
     * @param downloadingInfo 下载的信息，包括下载的总共包大小，已下载的量，是否已下载完
     * @date 创建时间 2017/11/14
     * @author Jiang zinc
     * @Description 下载进度接口
     * @version
     */
    void onProgress(DownloadingInfo downloadingInfo);

    /**
     *
     * @date 创建时间 2017/11/14
     * @author Jiang zinc
     * @Description 获取文件名称，用于下载保存时的文件名
     * @version
     *
     */
//    String getDownloadFileName();

    /**
     *
     * @date 创建时间 2017/11/14
     * @author Jiang zinc
     * @Description 获取文件路径，用于下载保存的路径，不包括文件名
     * @version
     *
     */
//    String getDownloadPath();

    /**
     * @date 创建时间 2017/11/14
     * @author Jiang zinc
     * @Description 获取文件名称前缀
     * @version
     */
    String getFileNamePrefix();

}
