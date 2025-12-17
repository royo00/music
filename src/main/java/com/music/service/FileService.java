package com.music.service;

import com.music.exception.BusinessException;
import com.music.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件服务类
 */
@Slf4j
@Service
public class FileService {

    @Value("${file.upload-path}")
    private String uploadPath;

    @Value("${file.base-url}")
    private String baseUrl;

    // 允许的音乐文件格式
    private static final List<String> ALLOWED_MUSIC_EXTENSIONS = Arrays.asList(
            "mp3", "flac", "wav", "ape", "m4a", "aac", "ogg"
    );

    // 允许的图片文件格式
    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp"
    );

    // 最大文件大小：100MB
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024;

    /**
     * 上传音乐文件
     *
     * @param file 音乐文件
     * @return 文件信息对象
     */
    public FileInfo uploadMusicFile(MultipartFile file) {
        return uploadFile(file, "music", ALLOWED_MUSIC_EXTENSIONS);
    }

    /**
     * 上传封面图片
     *
     * @param file 图片文件
     * @return 文件信息对象
     */
    public FileInfo uploadCoverImage(MultipartFile file) {
        return uploadFile(file, "cover", ALLOWED_IMAGE_EXTENSIONS);
    }

    /**
     * 上传头像图片
     *
     * @param file 图片文件
     * @return 文件信息对象
     */
    public FileInfo uploadAvatar(MultipartFile file) {
        return uploadFile(file, "avatar", ALLOWED_IMAGE_EXTENSIONS);
    }

    /**
     * 通用文件上传方法
     *
     * @param file               上传的文件
     * @param fileType           文件类型（music/cover/avatar）
     * @param allowedExtensions  允许的文件扩展名列表
     * @return 文件信息对象
     */
    private FileInfo uploadFile(MultipartFile file, String fileType, List<String> allowedExtensions) {
        // 1. 验证文件
        validateFile(file, allowedExtensions);

        // 2. 生成文件UUID和路径
        String originalFilename = file.getOriginalFilename();
        String extension = FileUtil.getFileExtension(originalFilename);
        String fileUuid = UUID.randomUUID().toString().replace("-", "");
        String fileName = fileUuid + "." + extension;

        // 3. 构建存储路径：uploadPath/fileType/yyyy-MM-dd/uuid.ext
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String relativePath = fileType + File.separator + datePath + File.separator + fileName;
        String fullPath = uploadPath + File.separator + relativePath;

        // 4. 创建目录
        File destFile = new File(fullPath);
        if (!destFile.getParentFile().exists()) {
            if (!destFile.getParentFile().mkdirs()) {
                log.error("创建目录失败: {}", destFile.getParentFile().getAbsolutePath());
                throw new BusinessException("文件上传失败：无法创建存储目录");
            }
        }

        // 5. 保存文件
        try {
            file.transferTo(destFile);
            log.info("文件上传成功: {}", fullPath);
        } catch (IOException e) {
            log.error("文件保存失败: {}", e.getMessage(), e);
            throw new BusinessException("文件上传失败：" + e.getMessage());
        }

        // 6. 构建访问URL
        String fileUrl = baseUrl + "/" + relativePath.replace(File.separator, "/");

        // 7. 返回文件信息
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileUuid(fileUuid);
        fileInfo.setOriginalName(originalFilename);
        fileInfo.setFileName(fileName);
        fileInfo.setFilePath(relativePath);
        fileInfo.setFileUrl(fileUrl);
        fileInfo.setFileSize(file.getSize());
        fileInfo.setExtension(extension);

        return fileInfo;
    }

    /**
     * 验证文件
     *
     * @param file              上传的文件
     * @param allowedExtensions 允许的扩展名列表
     */
    private void validateFile(MultipartFile file, List<String> allowedExtensions) {
        // 检查文件是否为空
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("文件大小不能超过100MB");
        }

        // 检查文件扩展名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new BusinessException("文件名不能为空");
        }

        String extension = FileUtil.getFileExtension(originalFilename).toLowerCase();
        if (!allowedExtensions.contains(extension)) {
            throw new BusinessException("不支持的文件格式：" + extension);
        }
    }

    /**
     * 删除文件
     *
     * @param filePath 文件相对路径
     * @return 是否删除成功
     */
    public boolean deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }

        try {
            String fullPath = uploadPath + File.separator + filePath;
            Path path = Paths.get(fullPath);

            if (Files.exists(path)) {
                Files.delete(path);
                log.info("文件删除成功: {}", fullPath);
                return true;
            } else {
                log.warn("文件不存在: {}", fullPath);
                return false;
            }
        } catch (IOException e) {
            log.error("文件删除失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取文件完整路径
     *
     * @param filePath 文件相对路径
     * @return 文件完整路径
     */
    public String getFullPath(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }
        return uploadPath + File.separator + filePath;
    }

    /**
     * 获取文件访问URL
     *
     * @param filePath 文件相对路径
     * @return 文件访问URL
     */
    public String getFileUrl(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }
        return baseUrl + "/" + filePath.replace(File.separator, "/");
    }

    /**
     * 文件信息内部类
     */
    @lombok.Data
    public static class FileInfo {
        private String fileUuid;
        private String originalName;
        private String fileName;
        private String filePath;
        private String fileUrl;
        private Long fileSize;
        private String extension;
    }
}
