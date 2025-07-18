package com.example.demo.services;

import com.example.demo.entities.Admins;
import com.example.demo.repositories.AdminsRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Transactional
@Service
public class AdminsService {
    private final AdminsRepository adminsRepository;

    @Autowired
    public AdminsService(AdminsRepository adminsRepository) {
        this.adminsRepository = adminsRepository;
    }
    
    public Admins addAdmin(UUID createdByAdminId, UUID newAdminId) {
        Admins admin = new Admins();
        admin.setAdminId(newAdminId);
        admin.setCreatedByAdminId(createdByAdminId);
        return adminsRepository.save(admin);
    }

    public List<UUID> findCreatedAdminIdsBy(UUID creatorId) {
        return adminsRepository.findAdminIdsByCreator(creatorId);
    }

    public void updateCreatorForAdmin(UUID deletedAdminId, UUID newAdminId) {
        adminsRepository.updateAdminReferences(deletedAdminId, newAdminId);
    }

    public void deleteByAdminId(UUID adminId) {
        adminsRepository.deleteByAdminId(adminId);
    }

    public boolean isCreatedBy(UUID creatorId, UUID targetAdminId) {
        return adminsRepository.existsByCreatorIdAndAdminId(creatorId, targetAdminId);
    }

    public boolean isCreatedByAnotherAdmin(UUID adminId) {
        return adminsRepository.isCreatedByAnotherAdmin(adminId);
    }
}