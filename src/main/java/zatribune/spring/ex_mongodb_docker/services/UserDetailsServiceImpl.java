package zatribune.spring.ex_mongodb_docker.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zatribune.spring.ex_mongodb_docker.entities.Role;
import zatribune.spring.ex_mongodb_docker.entities.User;
import zatribune.spring.ex_mongodb_docker.repositories.RoleRepository;
import zatribune.spring.ex_mongodb_docker.repositories.UserRepository;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String username) {
        log.info("Loading user by username: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Transactional
    @Override
    public void save(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Assign default USER role
        Role userRole = roleRepository.findByName("USER")
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName("USER");
                    return roleRepository.save(newRole);
                });
        user.setRoles(new HashSet<>(Set.of(userRole)));

        // Set account flags to active
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNotExpired(true);
        user.setEnabled(true);

        userRepository.save(user);
        log.info("User registered: {}", user.getUsername());
    }
}
