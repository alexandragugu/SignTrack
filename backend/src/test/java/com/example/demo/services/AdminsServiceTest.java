package com.example.demo.services;

import com.example.demo.entities.Admins;
import com.example.demo.repositories.AdminsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AdminsServiceTest {

    @Mock
    private AdminsRepository adminsRepository;

    @InjectMocks
    private AdminsService adminsService;


    @Test
    void addAdmin(){
        UUID createdBy=UUID.randomUUID();
        UUID newAdmin=UUID.randomUUID();

        Admins savedAdmin=new Admins();
        savedAdmin.setAdminId(newAdmin);
        savedAdmin.setCreatedByAdminId(createdBy);

        when(adminsRepository.save(any(Admins.class))).thenReturn(savedAdmin);

        Admins saveResult=adminsService.addAdmin(createdBy, newAdmin);

        assertEquals(newAdmin, saveResult.getAdminId());
        assertEquals(createdBy, saveResult.getCreatedByAdminId());
        verify(adminsRepository).save(any(Admins.class));
    }

    @Test
    void findCreatedAdminIdsByAdminId() {
        UUID creatorId = UUID.randomUUID();
        List<UUID> expected = List.of(UUID.randomUUID(), UUID.randomUUID());

        when(adminsRepository.findAdminIdsByCreator(creatorId)).thenReturn(expected);

        List<UUID> result = adminsService.findCreatedAdminIdsBy(creatorId);

        assertEquals(expected, result);
    }

    @Test
    void updateCreatorForAdmin() {
        UUID oldAdmin = UUID.randomUUID();
        UUID newAdmin = UUID.randomUUID();

        adminsService.updateCreatorForAdmin(oldAdmin, newAdmin);

        verify(adminsRepository).updateAdminReferences(oldAdmin, newAdmin);
    }

    @Test
    void deleteByAdminId() {
        UUID id = UUID.randomUUID();
        adminsService.deleteByAdminId(id);
        verify(adminsRepository).deleteByAdminId(id);
    }

    @Test
    void isCreatedBy() {
        UUID creator = UUID.randomUUID();
        UUID admin = UUID.randomUUID();

        when(adminsRepository.existsByCreatorIdAndAdminId(creator, admin)).thenReturn(true);

        assertTrue(adminsService.isCreatedBy(creator, admin));
    }

    @Test
    void isCreatedByAnotherAdmin() {
        UUID admin = UUID.randomUUID();
        when(adminsRepository.isCreatedByAnotherAdmin(admin)).thenReturn(false);
        assertFalse(adminsService.isCreatedByAnotherAdmin(admin));
    }

}
