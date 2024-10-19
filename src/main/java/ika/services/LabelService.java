package ika.services;

import ika.entities.Label;
import ika.repositories.LabelRepository;
import ika.entities.aux_classes.label.LabelResponse;
import ika.utils.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageImpl;

import java.util.UUID;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LabelService {

    @Autowired
    private LabelRepository labelRepository;

    public Page<LabelResponse> getAllLabels(String description, Pageable pageable) {
        Page<Label> labels;
        labels = labelRepository.findAllWithFilters(description, pageable);

        return new PageImpl<>(labels.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList()), pageable, labels.getTotalElements());
    }

    public LabelResponse getLabelById(UUID id) {
        Optional<Label> label = labelRepository.findById(id);
        return label.map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found"));
    }

    private LabelResponse mapToResponse(Label label) {
        return new LabelResponse(label.getId(), label.getDescription());
    }
}
