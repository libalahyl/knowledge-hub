package com.knowledgehub.doc.service.impl;

import com.knowledgehub.common.constant.DocConst;
import com.knowledgehub.common.exception.BusinessException;
import com.knowledgehub.doc.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 本地磁盘文件存储实现（兜底方案）
 */
@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "local")
public class LocalFileStorageServiceImpl implements FileStorageService {

    @Value("${storage.local.base-path}")
    private String basePath;

    @Override
    public String upload(MultipartFile file, String dir) {
        // 1. 校验文件后缀
        String ext = extractExtension(file.getOriginalFilename());
        if (!DocConst.ALLOWED_EXTENSIONS.contains(ext)) {
            throw new BusinessException("只支持 .md 文件");
        }
        // 2. 校验文件大小
        if (file.getSize() > DocConst.MAX_FILE_SIZE) {
            throw new BusinessException("文件不能超过 5MB");
        }
        // 3. 生成路径：{dir}/{yyyyMMdd}/{UUID}.{后缀}
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String path = dir + "/" + date + "/" + UUID.randomUUID().toString().replace("-", "") + "." + ext;

        Path target = Paths.get(basePath, path);
        try {
            // 4. 建目录并拷贝
            Files.createDirectories(target.getParent());
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BusinessException("上传文件失败: " + e.getMessage());
        }
        return path;
    }

    @Override
    public byte[] download(String path) {
        try {
            return Files.readAllBytes(Paths.get(basePath, path));
        } catch (IOException e) {
            throw new BusinessException("下载文件失败: " + e.getMessage());
        }
    }

    @Override
    public void delete(String path) {
        try {
            Files.deleteIfExists(Paths.get(basePath, path));
        } catch (IOException e) {
            throw new BusinessException("删除文件失败: " + e.getMessage());
        }
    }

    @Override
    public boolean exists(String path) {
        return Files.exists(Paths.get(basePath, path));
    }

    /**
     * 提取文件后缀（小写）
     */
    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}
