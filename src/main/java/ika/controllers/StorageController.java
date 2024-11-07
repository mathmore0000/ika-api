package ika.controllers;

import ika.entities.User;
import ika.entities.aux_classes.CustomPageResponse;
import ika.entities.Bucket;
import ika.services.BucketService;
import ika.services.FileService;
import ika.utils.CurrentUserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/v1/storage")
public class StorageController {

    @Autowired
    private CurrentUserProvider currentUserProvider;

    @Autowired
    private FileService fileService;

    @Autowired
    private BucketService bucketService;

    @PostMapping("/buckets/{bucketDescription}/files")
    public ResponseEntity<String> uploadFile(@PathVariable String bucketDescription,
                                             @RequestParam("file") MultipartFile file) throws Exception {
        User user = currentUserProvider.getCurrentUser();
        // Use MultipartFile's InputStream instead of creating a temp file

        // Call the uploadFile method from the service with correct parameters
        fileService.uploadVideo(user.getId(), bucketDescription, file);

        return ResponseEntity.ok("File uploaded successfully");
    }

    @DeleteMapping("/files/{fileId}")
    public ResponseEntity<String> deleteFile(@PathVariable UUID fileId) {
        fileService.deleteFile(fileId);
        return ResponseEntity.ok("File deleted successfully.");
    }

    @GetMapping("/buckets")
    public ResponseEntity<CustomPageResponse<Bucket>> getAllBuckets(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "description") String sortBy,
            @RequestParam(value = "sortDirection", defaultValue = "asc") String sortDirection,
            @RequestParam(value = "description", required = false) String description) {

        // Pageable request with sorting options
        Pageable pageable = CustomPageResponse.createPageableWithSort(page, size, sortBy, sortDirection);

        // If there's a description filter, apply it here (customize as needed)
        Page<Bucket> bucketPage = bucketService.getAllBuckets(pageable);

        // Create custom page response
        CustomPageResponse<Bucket> customPageResponse = new CustomPageResponse<>(
                bucketPage.getContent(),
                bucketPage.getNumber(),
                bucketPage.getSize(),
                bucketPage.getSort(),
                bucketPage.getPageable().getOffset(),
                bucketPage.getTotalPages()
        );

        return ResponseEntity.ok(customPageResponse);
    }
}
