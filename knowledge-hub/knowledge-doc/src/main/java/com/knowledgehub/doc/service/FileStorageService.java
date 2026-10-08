package com.knowledgehub.doc.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储服务（抽象接口，实现类下一阶段做：RustFS / 本地磁盘）
 */
public interface FileStorageService {

    /**
     * 上传文件
     *
     * @param file 文件
     * @param dir  子目录（如 "doc"）
     * @return 相对路径（存到数据库用）
     */
    String upload(MultipartFile file, String dir);

    /**
     * 下载文件
     *
     * @param path 相对路径
     * @return 文件字节
     */
    byte[] download(String path);

    /**
     * 删除文件
     *
     * @param path 相对路径
     */
    void delete(String path);

    /**
     * 判断文件是否存在
     *
     * @param path 相对路径
     * @return 是否存在
     */
    boolean exists(String path);
}
