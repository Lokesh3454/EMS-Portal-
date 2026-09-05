package com.ems.dto;

import com.ems.entity.Employee;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EmployeeDto {
    private Long id;
    private String empId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String gender;
    private LocalDate dateOfBirth;
    private String address;
    private Long departmentId;
    private String departmentName;
    private String designation;
    private LocalDate dateOfJoining;
    private String employmentType;
    private String status;
    private String profilePhoto;

    public static EmployeeDto fromEntity(Employee emp) {
        EmployeeDto dto = new EmployeeDto();
        dto.setId(emp.getId());
        dto.setEmpId(emp.getEmpId());
        dto.setFirstName(emp.getFirstName());
        dto.setLastName(emp.getLastName());
        dto.setEmail(emp.getEmail());
        dto.setPhone(emp.getPhone());
        dto.setGender(emp.getGender() != null ? emp.getGender().name() : null);
        dto.setDateOfBirth(emp.getDateOfBirth());
        dto.setAddress(emp.getAddress());
        dto.setDepartmentId(emp.getDepartment() != null ? emp.getDepartment().getId() : null);
        dto.setDepartmentName(emp.getDepartment() != null ? emp.getDepartment().getName() : null);
        dto.setDesignation(emp.getDesignation());
        dto.setDateOfJoining(emp.getDateOfJoining());
        dto.setEmploymentType(emp.getEmploymentType() != null ? emp.getEmploymentType().name() : null);
        dto.setStatus(emp.getStatus() != null ? emp.getStatus().name() : null);
        dto.setProfilePhoto(emp.getProfilePhoto());
        return dto;
    }
}
