package com.suguna.weighment_api_service.admin.services.impl;



import com.suguna.weighment_api_service.admin.dtos.responses.UserFilterDTO;
import com.suguna.weighment_api_service.admin.dtos.requests.UserRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.UserResponseDTO;
import com.suguna.weighment_api_service.admin.entities.AppUser;
import com.suguna.weighment_api_service.admin.entities.Employee;
import com.suguna.weighment_api_service.admin.entities.Role;
import com.suguna.weighment_api_service.admin.mappers.UserMapper;
import com.suguna.weighment_api_service.admin.repositories.AppUserRepository;
import com.suguna.weighment_api_service.admin.repositories.EmployeeRepository;
import com.suguna.weighment_api_service.admin.repositories.RoleRepository;
import com.suguna.weighment_api_service.admin.dtos.responses.PageResponse;
import com.suguna.weighment_api_service.admin.services.UserService;
import com.suguna.weighment_api_service.admin.specifications.UserSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private AppUserRepository repository;

    @Autowired
    private UserMapper mapper;


  //  @Autowired
   // private PasswordEncoder passwordEncoder;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private RoleRepository roleRepository;


    @Override
    public UserResponseDTO createUser(UserRequestDTO dto) {


// ✅ Validate duplicate username
        repository.findByUserNameAndApplication(dto.getUserName(), dto.getApplication())
                .ifPresent(u -> {
                    throw new RuntimeException("Username already exists");
                });


// ✅ 2. Fetch Role
        Role role = roleRepository.findById(dto.getRoleId())
                .orElseThrow(() -> new RuntimeException("Invalid Role"));


// ✅ 3. Role-based validation 🔥
        validateRoleRequirements(dto, role);



        AppUser user = mapper.toEntity(dto);


        // ✅ Employee Handling
        if ("EMPLOYEE".equalsIgnoreCase(dto.getUserType())) {

            Employee employee = employeeRepository.findByEmpNo(dto.getUserName());

            if (employee == null) {
                throw new RuntimeException("Employee not found for userName: " + dto.getUserName());
            }

            user.setUserId(employee.getPersonId());
            user.setName(employee.getName());
            user.setEmail(employee.getEmail());
            user.setOrgId(employee.getOrgId());

            // ✅ Prefer mobile over phone
            if (employee.getMobileNumber() != null && !employee.getMobileNumber().isEmpty()) {
                user.setMobile(Long.valueOf(employee.getMobileNumber()));
            } else if (employee.getPhoneNumber() != null && !employee.getPhoneNumber().isEmpty()) {
                user.setMobile(Long.valueOf(employee.getPhoneNumber()));
            }
        }
        else{
            if(role.getFranchiseeRequired().equals("Y"))
            {
                user.setOrgId(dto.getBranchId());
            }

        }


        user.setCreatedAt(new Date());
        user.setStatus("A");
        user.setPassword(dto.getPassword());

        user.setPasswordChangedAt(new Date());
        user.setPasswordExpiryDays(30); // default 30 days


        return mapper.toDTO(repository.save(user));
    }

    private void validateRoleRequirements(UserRequestDTO dto, Role role) {

        // ✅ Feedmill validation
        if ("Y".equalsIgnoreCase(role.getFeedmillRequired())) {

            if (dto.getBranchId() == null) {
                throw new RuntimeException(
                        "Feedmill is required for role: " + role.getRoleName()
                );
            }
        }

        // ✅ Franchise validation
        if ("Y".equalsIgnoreCase(role.getFranchiseeRequired())) {

            if (dto.getBranchId() == null) {
                throw new RuntimeException(
                        "Franchise is required for role: " + role.getRoleName()
                );
            }
        }
    }

    @Override
    public UserResponseDTO updateUser(Long id, UserRequestDTO dto) {

        AppUser user = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(dto.getName());
        user.setMobile(dto.getMobile());
        user.setEmail(dto.getEmail());
        user.setUserType(dto.getUserType());
        user.setApplication(dto.getApplication());
        user.setUpdatedAt(new Date());
        user.setStatus(dto.getStatus().equals("ACTIVE") ? "A":"I");

        return mapper.toDTO(repository.save(user));
    }

    @Override
    public UserResponseDTO getUserById(Long id) {

        return mapper.toDTO(
                repository.findById(id)
                        .orElseThrow(() -> new RuntimeException("User not found"))
        );
    }

    @Override
    public List<UserResponseDTO> getAllUsers() {

        return repository.findAll()
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public PageResponse<UserResponseDTO> getUsers(UserFilterDTO filter) {

        Pageable pageable = PageRequest.of(
                filter.getPage(),
                filter.getSize(),
                Sort.by(Sort.Direction.DESC, "id")
        );

        Specification<AppUser> spec =
                UserSpecification.filterUsers(filter);

        Page<AppUser> page = repository.findAll(spec, pageable);

        return PageResponse.<UserResponseDTO>builder()
                .data(page.getContent().stream()
                        .map(mapper::toDTO)
                        .toList())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    @Override
    public String deleteUser(Long id) {

        AppUser user = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setStatus("I"); // soft delete
        repository.save(user);

        return "User Deactivated";
    }

    public void updateFirebaseToken(Long userId, String token) {

        AppUser user = repository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFirebaseToken(token);
        repository.save(user);
    }
}

