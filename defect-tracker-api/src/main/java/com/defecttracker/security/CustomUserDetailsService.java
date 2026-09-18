package com.defecttracker.security;

import com.defecttracker.entity.Role;
import com.defecttracker.entity.RolePermission;
import com.defecttracker.entity.User;
import com.defecttracker.repository.RolePermissionRepository;
import com.defecttracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByEmailOrUserId(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with identifier: " + usernameOrEmail));

        Set<String> permissions = new HashSet<>();
        for (Role role : user.getRoles()) {
            if ("Super Admin".equalsIgnoreCase(role.getRoleName())) {
                permissions.add("ALL_PERMISSIONS");
            }
            List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleId(role.getId());
            for (RolePermission rp : rolePermissions) {
                if (rp.getPermission() != null) {
                    permissions.add(rp.getPermission().getAction());
                }
            }
        }

        return UserPrincipal.create(user, permissions);
    }
}
