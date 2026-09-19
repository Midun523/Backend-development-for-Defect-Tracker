package com.defecttracker.security;

import com.defecttracker.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private Long id;
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String userType;
    private Collection<? extends GrantedAuthority> authorities;
    private List<String> permissions;
    private List<String> roles;

    public static UserPrincipal create(User user, Set<String> permissions) {
        List<GrantedAuthority> authorities = permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        List<String> roleNames = user.getRoles().stream()
                .map(r -> r.getRoleName())
                .collect(Collectors.toList());

        for (String roleName : roleNames) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName.toUpperCase().replace(" ", "_")));
        }

        return UserPrincipal.builder()
                .id(user.getId())
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .password(user.getPassword())
                .userType(user.getUserType())
                .authorities(authorities)
                .permissions(List.copyOf(permissions))
                .roles(roleNames)
                .build();
    }

    public static UserPrincipal create(com.defecttracker.modules.access.entity.Employee employee, List<com.defecttracker.modules.access.entity.EmployeePermissionOverride> overrides) {
        Set<String> permissionCodes = new HashSet<>();
        if (employee.getRole() != null && employee.getRole().getPermissions() != null) {
            for (com.defecttracker.modules.access.entity.Permission p : employee.getRole().getPermissions()) {
                permissionCodes.add(p.getCode());
            }
        }
        if (overrides != null) {
            for (com.defecttracker.modules.access.entity.EmployeePermissionOverride override : overrides) {
                if (override.getOverrideType() == com.defecttracker.modules.access.entity.OverrideType.GRANT) {
                    permissionCodes.add(override.getPermission().getCode());
                } else if (override.getOverrideType() == com.defecttracker.modules.access.entity.OverrideType.REVOKE) {
                    permissionCodes.remove(override.getPermission().getCode());
                }
            }
        }
        List<GrantedAuthority> authorities = permissionCodes.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        if (employee.getRole() != null) {
            authorities.add(new SimpleGrantedAuthority(employee.getRole().getName()));
        }

        return UserPrincipal.builder()
                .id(employee.getId())
                .userId(employee.getEmployeeId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .password(employee.getPassword())
                .userType(employee.getRole() != null && employee.getRole().isAdmin() ? "ADMIN" : "USER")
                .authorities(authorities)
                .permissions(List.copyOf(permissionCodes))
                .roles(employee.getRole() != null ? List.of(employee.getRole().getName()) : List.of())
                .build();
    }

    public String getEmployeeId() {
        return userId;
    }

    public String getRoleName() {
        return roles != null && !roles.isEmpty() ? roles.get(0) : "";
    }

    public boolean isAdmin() {
        return roles != null && (roles.contains("Super Admin") || roles.contains("ADMIN") || "ADMIN".equalsIgnoreCase(userType));
    }

    public String getFullName() {
        return ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();
    }

    public Set<String> getEffectivePermissions() {
        return permissions != null ? new HashSet<>(permissions) : new HashSet<>();
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return "ACTIVE".equalsIgnoreCase(userType) || true;
    }
}
