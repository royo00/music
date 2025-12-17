package com.music.util;

import com.music.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
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
 * 文件工具类
 * 用于处理文件上传、存储、删除等操作
 */
@Component
public class FileUtil {

    /**
     * 文件上传根路径
     */
    @Value("${file.upload.path:/data/music-platform/uploads}")
    private String uploadPath;

    /**
     * 文件访问URL前缀
     */
    @Value("${file.access.url:http://localhost:8080/files}")
    private String accessUrlPrefix;

    /**
     * 允许的音乐文件扩展名
     */
    private static final List<String> ALLOWED_MUSIC_EXTENSIONS = Arrays.asList(
            "mp3", "wav", "flac", "aac", "ogg", "m4a", "wma"
    );

    /**
     * 允许的图片文件扩展名
     */
    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "webp"
    );

    /**
     * 最大音乐文件大小（字节），默认50MB
     */
    @Value("${file.max.music.size:52428800}")
    private Long maxMusicSize;

    /**
     * 最大图片文件大小（字节），默认5MB
     */
    @Value("${file.max.image.size:5242880}")
    private Long maxImageSize;

    /**
     * 上传音乐文件
     *
     * @param file 音乐文件
     * @return 文件信息对象
     * @throws IOException 文件操作异常
     */
    public FileInfo uploadMusicFile(MultipartFile file) throws IOException {
        // 验证文件
        validateMusicFile(file);

        // 生成文件UUID
        String fileUuid = UUID.randomUUID().toString().replace("-", "");

        // 获取文件扩展名
        String extension = getFileExtension(file.getOriginalFilename());

        // 构建存储路径：uploads/music/2024/01/15/uuid.mp3
        String relativePath = buildFilePath("music", fileUuid, extension);
        String absolutePath = uploadPath + File.separator + relativePath;

        // 保存文件
        saveFile(file, absolutePath);

        // 构建返回信息
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileUuid(fileUuid);
        fileInfo.setOriginalName(file.getOriginalFilename());
        fileInfo.setFilePath(relativePath);
        fileInfo.setFileSize(file.getSize());
        fileInfo.setExtension(extension);
        fileInfo.setAccessUrl(accessUrlPrefix + "/" + relativePath.replace("\\", "/"));

        return fileInfo;
    }

    /**
     * 上传图片文件（封面）
     *
     * @param file 图片文件
     * @return 文件信息对象
     * @throws IOException 文件操作异常
     */
    public FileInfo uploadImageFile(MultipartFile file) throws IOException {
        // 验证文件
        validateImageFile(file);

        // 生成文件UUID
        String fileUuid = UUID.randomUUID().toString().replace("-", "");

        // 获取文件扩展名
        String extension = getFileExtension(file.getOriginalFilename());

        // 构建存储路径：uploads/images/2024/01/15/uuid.jpg
        String relativePath = buildFilePath("images", fileUuid, extension);
        String absolutePath = uploadPath + File.separator + relativePath;

        // 保存文件
        saveFile(file, absolutePath);

        // 构建返回信息
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileUuid(fileUuid);
        fileInfo.setOriginalName(file.getOriginalFilename());
        fileInfo.setFilePath(relativePath);
        fileInfo.setFileSize(file.getSize());
        fileInfo.setExtension(extension);
        fileInfo.setAccessUrl(accessUrlPrefix + "/" + relativePath.replace("\\", "/"));

        return fileInfo;
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
            String absolutePath = uploadPath + File.separator + filePath;
            Path path = Paths.get(absolutePath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 根据UUID删除文件
     *
     * @param fileUuid 文件UUID
     * @param fileType 文件类型（music/images）
     * @return 是否删除成功
     */
    public boolean deleteFileByUuid(String fileUuid, String fileType) {
        // 这里需要根据实际存储结构查找文件
        // 简化实现：遍历目录查找
        File typeDir = new File(uploadPath + File.separator + fileType);
        return searchAndDeleteFile(typeDir, fileUuid);
    }

    /**
     * 验证音乐文件
     *
     * @param file 文件对象
     */
    private void validateMusicFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        // 验证文件大小
        if (file.getSize() > maxMusicSize) {
            throw new BusinessException("音乐文件大小不能超过" + (maxMusicSize / 1024 / 1024) + "MB");
        }

        // 验证文件扩展名
        String extension = getFileExtension(file.getOriginalFilename());
        if (!ALLOWED_MUSIC_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BusinessException("不支持的音乐文件格式，仅支持：" + String.join(", ", ALLOWED_MUSIC_EXTENSIONS));
        }
    }

    /**
     * 验证图片文件
     *
     * @param file 文件对象
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        // 验证文件大小
        if (file.getSize() > maxImageSize) {
            throw new BusinessException("图片文件大小不能超过" + (maxImageSize / 1024 / 1024) + "MB");
        }

        // 验证文件扩展名
        String extension = getFileExtension(file.getOriginalFilename());
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BusinessException("不支持的图片文件格式，仅支持：" + String.join(", ", ALLOWED_IMAGE_EXTENSIONS));
        }
    }

    /**
     * 获取文件扩展名
     *
     * @param filename 文件名
     * @return 扩展名（不含点）
     */
    public static String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * 构建文件存储路径
     *
     * @param type      文件类型目录（music/images）
     * @param fileUuid  文件UUID
     * @param extension 文件扩展名
     * @return 相对路径
     */
    private String buildFilePath(String type, String fileUuid, String extension) {
        // 按日期分目录：music/2024/01/15/uuid.mp3
        LocalDate now = LocalDate.now();
        String datePath = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return type + File.separator + datePath + File.separator + fileUuid + "." + extension;
    }

    /**
     * 保存文件到磁盘
     *
     * @param file         文件对象
     * @param absolutePath 绝对路径
     * @throws IOException 文件操作异常
     */
    private void saveFile(MultipartFile file, String absolutePath) throws IOException {
        // 创建目录
        File dest = new File(absolutePath);
        File parentDir = dest.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }

        // 保存文件
        file.transferTo(dest);
    }

    /**
     * 递归搜索并删除文件
     *
     * @param directory 目录
     * @param fileUuid  文件UUID
     * @return 是否删除成功
     */
    private boolean searchAndDeleteFile(File directory, String fileUuid) {
        if (!directory.exists() || !directory.isDirectory()) {
            return false;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return false;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                if (searchAndDeleteFile(file, fileUuid)) {
                    return true;
                }
            } else if (file.getName().startsWith(fileUuid)) {
                return file.delete();
            }
        }
        return false;
    }

    /**
     * 格式化文件大小
     *
     * @param size 文件大小（字节）
     * @return 格式化后的字符串
     */
    public static String formatFileSize(Long size) {
        if (size == null || size <= 0) {
            return "0 B";
        }

        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.2f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    /**
     * 文件信息内部类
     */
    public static class FileInfo {
        private String fileUuid;
        private String originalName;
        private String filePath;
        private Long fileSize;
        private String extension;
        private String accessUrl;

        // Getters and Setters
        public String getFileUuid() {
            return fileUuid;
        }

        public void setFileUuid(String fileUuid) {
            this.fileUuid = fileUuid;
        }

        public String getOriginalName() {
            return originalName;
        }

        public void setOriginalName(String originalName) {
            this.originalName = originalName;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public Long getFileSize() {
            return fileSize;
        }

        public void setFileSize(Long fileSize) {
            this.fileSize = fileSize;
        }

        public String getExtension() {
            return extension;
        }

        public void setExtension(String extension) {
            this.extension = extension;
        }

        public String getAccessUrl() {
            return accessUrl;
        }

        public void setAccessUrl(String accessUrl) {
            this.accessUrl = accessUrl;
        }
    }
}
