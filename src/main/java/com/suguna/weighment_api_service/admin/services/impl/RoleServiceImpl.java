package com.suguna.weighment_api_service.admin.services.impl;


import com.suguna.weighment_api_service.admin.dtos.requests.RoleRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.RoleResponseDTO;
import com.suguna.weighment_api_service.admin.entities.Role;
import com.suguna.weighment_api_service.admin.mappers.RoleMapper;
import com.suguna.weighment_api_service.admin.repositories.RoleRepository;
import com.suguna.weighment_api_service.admin.services.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository repository;

    @Autowired
    private RoleMapper mapper;

    @Override
    public RoleResponseDTO createRole(RoleRequestDTO dto) {

        Role role = mapper.toEntity(dto);
        role.setCreatedAt(new Date());
        role.setStatus("A");

        if(dto.getFeedmillRequired() == null || dto.getFeedmillRequired().isEmpty())
            role.setFeedmillRequired("N");
        else
            role.setFeedmillRequired("Y");

        if(dto.getFranchiseeRequired() == null || dto.getFranchiseeRequired().isEmpty())
            role.setFranchiseeRequired("N");
        else
            role.setFranchiseeRequired("Y");

        return mapper.toDTO(repository.save(role));
    }

    @Override
    public RoleResponseDTO updateRole(Long id, RoleRequestDTO dto) {

        Role role = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        role.setRoleName(dto.getRoleName());
        role.setRoleDesc(dto.getRoleDesc());
        role.setApplication(dto.getApplication());
        role.setUpdatedAt(new Date());

        if(dto.getFeedmillRequired() == null || dto.getFeedmillRequired().isEmpty())
            role.setFeedmillRequired("N");
        else
            role.setFeedmillRequired("Y");

        if(dto.getFranchiseeRequired() == null || dto.getFranchiseeRequired().isEmpty())
            role.setFranchiseeRequired("N");
        else
            role.setFranchiseeRequired("Y");

        return mapper.toDTO(repository.save(role));
    }

    @Override
    public RoleResponseDTO getRoleById(Long id) {
        return mapper.toDTO(
                repository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Role not found"))
        );
    }

    @Override
    public List<RoleResponseDTO> getAllRoles() {
        return repository.findAll()
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public List<RoleResponseDTO> getActiveRoles(String application) {
        return repository.findByApplicationAndStatus(application, "A")
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public String deleteRole(Long id) {
        Role role = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        role.setStatus("I"); // soft delete
        repository.save(role);
        return "Deleted";
    }
}

