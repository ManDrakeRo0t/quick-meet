package ru.bogatov.quickmeet.service.file;

import com.jlefebure.spring.boot.minio.MinioException;
import com.jlefebure.spring.boot.minio.MinioService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.bogatov.quickmeet.entity.File;
import ru.bogatov.quickmeet.error.ErrorUtils;
import ru.bogatov.quickmeet.model.enums.ApplicationError;
import ru.bogatov.quickmeet.repository.file.FileRepository;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

@Service
@Slf4j
public class FileService {

    private final FileRepository fileRepository;
    private final MinioService minioService;
    @Value("${spring.minio.url}")
    private String minioHost;
    @Value("${spring.minio.bucket}")
    private String minioBucket;


    public FileService(FileRepository fileRepository, MinioService minioService) {
        this.fileRepository = fileRepository;
        this.minioService = minioService;
    }

    public File saveFile(MultipartFile file) {
        try {
            Path path = getPathForFile(file);
            minioService.upload(getPathForFile(file), file.getInputStream(), file.getContentType());
            File toSave = new File();
            toSave.setFileName(path.toString());
            toSave.setHref(generateHrefToFile(path.toString()));
            return fileRepository.save(toSave);
        } catch (MinioException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    @SneakyThrows
    public File deleteFile(UUID fileId) {
        File file = findById(fileId);
        Path path = Path.of(file.getFileName());
        file.setHref("deleted");
        minioService.remove(path);
        return fileRepository.save(file);
    }

    @SneakyThrows
    public void deleteFile(String fileName) {
        Path path = Path.of(fileName);
        minioService.remove(path);
    }

    public File updateFile(UUID fileId, MultipartFile file) {
        File fileToUpdate = findById(fileId);
        Path path = getPathForFile(file);
        try {
            minioService.upload(path, file.getInputStream(), file.getContentType());
            fileToUpdate.setFileName(path.toString());
            fileToUpdate.setHref(generateHrefToFile(path.toString()));
        } catch (MinioException | IOException e) {
            throw new RuntimeException(e);
        }
        return fileRepository.save(fileToUpdate);
    }

    private Path getPathForFile(MultipartFile file) {
        return Path.of(String.format("%s_avatar_%s", UUID.randomUUID(), file.getOriginalFilename()));
    }

    private File findById(UUID fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> ErrorUtils.buildException(ApplicationError.FILE_PROCESSING_ERROR));
    }

    private String generateHrefToFile(String filePath) {
        return String.format("%s/%s/%s", this.minioHost, this.minioBucket, filePath);
    }
}
