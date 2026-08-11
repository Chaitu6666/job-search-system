package com.jobsearch.user_service.controller;

import com.jobsearch.user_service.entity.User;
import com.jobsearch.user_service.enums.UserType;
import com.jobsearch.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    // Called by job-service via Feign — get any user by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(mapToResponse(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    // Called by job-service via Feign — search job seekers by skill
    @GetMapping("/jobseekers/search")
    public ResponseEntity<List<?>> searchJobSeekersBySkill(
            @RequestParam("skill") String skill) {
        List<User> users = userRepository
                .findByUserTypeAndSkillSetContainingIgnoreCase(
                        UserType.JOB_SEEKER, skill);
        return ResponseEntity.ok(
                users.stream().map(this::mapToResponse).toList());
    }

    private java.util.Map<String, Object> mapToResponse(User user) {
        return java.util.Map.of(
                "id",        user.getId(),
                "username",  user.getUsername(),
                "email",     user.getEmail(),
                "orgName",   user.getOrgName()   != null ? user.getOrgName()   : "",
                "name",      user.getName()       != null ? user.getName()       : "",
                "contactNo", user.getContactNo(),
                "address",   user.getAddress(),
                "skillSet",  user.getSkillSet()   != null ? user.getSkillSet()   : "",
                "userType",  user.getUserType().name()
        );
    }
}
