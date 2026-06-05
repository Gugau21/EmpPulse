package com.oman.EmpPulse.service;

import com.oman.EmpPulse.dto.DepartmentCreateRequest;
import com.oman.EmpPulse.dto.DepartmentUpdateRequest;
import com.oman.EmpPulse.dto.response.*;
import com.oman.EmpPulse.entity.*;
import com.oman.EmpPulse.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final AdminRepository adminRepository;
    private final AdminDepartmentRepository adminDepartmentRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    public DepartmentService(DepartmentRepository departmentRepository,
                             AdminRepository adminRepository,
                             AdminDepartmentRepository adminDepartmentRepository,
                             UserRepository userRepository,
                             EmployeeRepository employeeRepository) {
        this.departmentRepository = departmentRepository;
        this.adminRepository = adminRepository;
        this.adminDepartmentRepository = adminDepartmentRepository;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
    }

    public DepartmentResponse getDepartment(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));
        return toDepartmentResponse(department);
    }

    /** Returns true if the given user is an admin assigned to the department. */
    public boolean isAdminOfDepartment(Long userId, Long departmentId) {
        return adminRepository.findByUserId(userId)
                .map(admin -> adminDepartmentRepository.findByAdminId(admin.getId()).stream()
                        .anyMatch(ad -> ad.getDepartmentId().equals(departmentId)))
                .orElse(false);
    }

    @Transactional
    public void deleteDepartment(Long departmentId) {
        if (!departmentRepository.existsById(departmentId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found");
        }
        if (employeeRepository.existsByDepartmentId(departmentId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot delete department: employees are assigned");
        }
        if (!adminDepartmentRepository.findByDepartmentId(departmentId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot delete department: administrators are assigned");
        }
        departmentRepository.deleteById(departmentId);
    }

    public DepartmentListResponse getAllDepartments() {
        List<DepartmentResponse> items = departmentRepository.findAll().stream()
                .map(this::toDepartmentResponse)
                .toList();
        return new DepartmentListResponse(items);
    }

    public DepartmentListResponse getDepartmentsForAdmin(Long userId) {
        Admin admin = adminRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied"));
        List<Long> deptIds = adminDepartmentRepository.findByAdminId(admin.getId())
                .stream().map(AdminDepartment::getDepartmentId).toList();
        List<DepartmentResponse> items = departmentRepository.findAllById(deptIds).stream()
                .map(this::toDepartmentResponse)
                .toList();
        return new DepartmentListResponse(items);
    }

    @Transactional
    public void createDepartment(DepartmentCreateRequest req) {
        if (departmentRepository.existsByName(req.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Department name already in use");
        }

        Department department = new Department(req.getName());
        departmentRepository.save(department);

        if (req.getAdminIds() != null) {
            for (Long adminId : req.getAdminIds()) {
                if (!adminRepository.existsById(adminId)) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found: " + adminId);
                }
                adminDepartmentRepository.save(new AdminDepartment(adminId, department.getId()));
            }
        }
    }

    @Transactional
    public void updateDepartment(Long departmentId, DepartmentUpdateRequest req) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));

        if (req.getName() != null) {
            if (!req.getName().equals(department.getName()) && departmentRepository.existsByName(req.getName())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Department name already in use");
            }
            department.setName(req.getName());
            departmentRepository.save(department);
        }

        if (req.getAdminIds() != null) {
            List<Long> currentAdminIds = adminDepartmentRepository.findByDepartmentId(departmentId)
                    .stream().map(AdminDepartment::getAdminId).toList();
            Set<Long> newAdminIdSet = new HashSet<>(req.getAdminIds());
            Set<Long> currentAdminIdSet = new HashSet<>(currentAdminIds);

            for (Long adminId : currentAdminIds) {
                if (!newAdminIdSet.contains(adminId)) {
                    long deptCount = adminDepartmentRepository.findByAdminId(adminId).size();
                    if (deptCount <= 1) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT,
                                "Cannot detach admin " + adminId + ": must oversee more than 1 department");
                    }
                }
            }

            for (Long adminId : req.getAdminIds()) {
                if (!adminRepository.existsById(adminId)) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found: " + adminId);
                }
            }

            for (Long adminId : currentAdminIds) {
                if (!newAdminIdSet.contains(adminId)) {
                    adminDepartmentRepository.deleteById(new AdminDepartmentId(adminId, departmentId));
                }
            }

            for (Long adminId : req.getAdminIds()) {
                if (!currentAdminIdSet.contains(adminId)) {
                    adminDepartmentRepository.save(new AdminDepartment(adminId, departmentId));
                }
            }
        }
    }

    private DepartmentResponse toDepartmentResponse(Department department) {
        List<AdminDepartment> assignments = adminDepartmentRepository.findByDepartmentId(department.getId());
        List<AdminSummaryResponse> admins = assignments.stream()
                .map(ad -> {
                    Admin admin = adminRepository.findById(ad.getAdminId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Data inconsistency"));
                    User user = userRepository.findById(admin.getUserId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Data inconsistency"));
                    if (user.isDeleted()) {
                        return null;
                    }
                    List<Long> deptIds = adminDepartmentRepository.findByAdminId(admin.getId())
                            .stream().map(AdminDepartment::getDepartmentId).toList();
                    return new AdminSummaryResponse(admin.getId(),
                            new UserSummaryResponse(user.getId(), user.getName(), user.getSurname(), user.getEmail()),
                            deptIds);
                })
                .filter(java.util.Objects::nonNull)
                .toList();
        return new DepartmentResponse(department.getId(), department.getName(), admins);
    }
}
