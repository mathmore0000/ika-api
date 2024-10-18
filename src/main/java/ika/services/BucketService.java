package ika.services;

import ika.entities.Bucket;
import ika.repositories.BucketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class BucketService {
    @Autowired
    private BucketRepository bucketRepository;
    public Page<Bucket> getAllBuckets(Pageable pageable) {
        return bucketRepository.findAll(pageable);
    }
}
