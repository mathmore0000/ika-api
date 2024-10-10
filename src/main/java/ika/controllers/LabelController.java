package ika.controllers;

import ika.entities.Label;
import ika.controllers.aux_classes.label.LabelResponse;
import ika.controllers.aux_classes.CustomPageResponse;
import ika.services.LabelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/labels")
public class LabelController {

    @Autowired
    private LabelService labelService;

    @GetMapping()
    public ResponseEntity<CustomPageResponse<LabelResponse>> getAllLabels(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "200") int size,
            @RequestParam(value = "description", defaultValue = "", required = false) String description,
            @RequestParam(defaultValue = "description") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        page = CustomPageResponse.getValidPage(page);
        size = CustomPageResponse.getValidSize(size);

        Pageable pageable = CustomPageResponse.createPageableWithSort(page, size, sortBy, sortDirection);

        Page<LabelResponse> labelPage = labelService.getAllLabels(description, pageable);

        CustomPageResponse<LabelResponse> customPageResponse = new CustomPageResponse<>(
                labelPage.getContent(),
                labelPage.getNumber(),
                labelPage.getSize(),
                labelPage.getSort(),
                labelPage.getPageable().getOffset(),
                labelPage.getTotalPages()
        );

        return ResponseEntity.ok(customPageResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LabelResponse> getLabelById(@PathVariable UUID id) {
        LabelResponse labelResponse = labelService.getLabelById(id);
        return ResponseEntity.ok(labelResponse);
    }
}
