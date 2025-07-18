package com.example.demo.services;

import com.example.demo.entities.UserBucket;
import com.example.demo.repositories.UserBucketRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserBucketService {

    public static UUID getUserBucketByBucketUuid;
    private final UserBucketRepository userBucketRepository;

    public UserBucketService(UserBucketRepository userBucketRepository) {
        this.userBucketRepository = userBucketRepository;
    }

    public UserBucket createUserBucket(UUID userId,String username) {
        UUID bucketUuid=UUID.randomUUID();
        UserBucket userBucket = new UserBucket(userId, bucketUuid,username);
        return userBucketRepository.save(userBucket);
    }
    public UserBucket getUserBucket(UUID userId) {
        return userBucketRepository.findByUserId(userId).orElse(null);
    }


    public String getBucketUuid(UUID userId) {
        return userBucketRepository.findByUserId(userId).map(UserBucket::getBucketUuid).map(UUID::toString).orElse(null);
    }
    
    public UserBucket getUserBucketByBucketUuid(UUID bucketUuid) {
        return userBucketRepository.findByBucketUuid(bucketUuid).orElse(null);
    }

    @Transactional
    public void deleteUserBucket(UUID userId) {
        userBucketRepository.deleteByUserId(userId);
    }


}
