package com.knowledgehub.doc.service.impl;

import com.knowledgehub.common.constant.DocConst;
import com.knowledgehub.common.exception.BusinessException;
import com.knowledgehub.doc.service.FileStorageService;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * RustFS 文件存储实现（S3 兼容对象存储）
 */
@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "rustfs", matchIfMissing = true)
public class RustFsFileStorageServiceImpl implements FileStorageService {

    @Resource
    private MinioClient minioClient;

    @Value("${storage.rustfs.bucket}")
    private String bucket;

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

        try {
            // 4. 确保 bucket 存在
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
            // 5. 上传
            String contentType = file.getContentType() != null ? file.getContentType() : "text/markdown";
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(path)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(contentType)
                    .build());
        } catch (Exception e) {
            throw new BusinessException("上传文件失败: " + e.getMessage());
        }
        return path;
    }

    @Override
    public byte[] download(String path) {
        try (GetObjectResponse response = minioClient.getObject(
                GetObjectArgs.builder().bucket(bucket).object(path).build())) {
            return response.readAllBytes();
        } catch (Exception e) {
            throw new BusinessException("下载文件失败: " + e.getMessage());
        }
    }

    @Override
    public void delete(String path) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(path).build());
        } catch (Exception e) {
            throw new BusinessException("删除文件失败: " + e.getMessage());
        }
    }

    @Override
    public boolean exists(String path) {
        try {
            minioClient.statObject(StatObjectArgs.builder().bucket(bucket).object(path).build());
            return true;
        } catch (Exception e) {
            return false;
        }
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
