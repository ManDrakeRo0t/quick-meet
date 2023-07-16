package ru.bogatov.quickmeet.controller.v1.file;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.bogatov.quickmeet.constant.RouteConstants;
import ru.bogatov.quickmeet.entity.File;
import ru.bogatov.quickmeet.service.file.FileService;

import java.util.UUID;

@RestController
@RequestMapping(RouteConstants.API_V1 + RouteConstants.FILE_MANAGEMENT + RouteConstants.FILE)
public class FileController {

    private final FileService fileService;
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<File> fileUpload(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fileService.saveFile(file));
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity removeFile(@PathVariable UUID fileId) {
        fileService.deleteFile(fileId);
        return ResponseEntity.noContent().build();
    }


}
