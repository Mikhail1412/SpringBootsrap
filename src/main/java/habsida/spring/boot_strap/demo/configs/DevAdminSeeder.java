package habsida.spring.boot_strap.demo.configs;

import habsida.spring.boot_strap.demo.model.Role;
import habsida.spring.boot_strap.demo.model.User;
import habsida.spring.boot_strap.demo.repositories.RoleRepository;
import habsida.spring.boot_strap.demo.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class DevAdminSeeder {

    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder encoder;

    public DevAdminSeeder(UserRepository users, RoleRepository roles, PasswordEncoder encoder) {
        this.users = users;
        this.roles = roles;
        this.encoder = encoder;
    }

    @PostConstruct
    @Transactional
    public void seed() {
        Role rAdmin = roles.findByName("ROLE_ADMIN").orElseGet(() -> {
            Role r = new Role();
            r.setName("ROLE_ADMIN");
            return roles.save(r);
        });
        Role rUser = roles.findByName("ROLE_USER").orElseGet(() -> {
            Role r = new Role();
            r.setName("ROLE_USER");
            return roles.save(r);
        });

        users.findByEmail("admin@local").ifPresentOrElse(u -> {
            String p = u.getPassword();
            if (p == null || !(p.startsWith("$2a$") || p.startsWith("$2b$") || p.startsWith("$2y$"))) {
                u.setPassword(encoder.encode("1234"));
            }
            u.setRoles(new HashSet<>(Set.of(rAdmin, rUser)));
            users.save(u);
        }, () -> {
            User admin = new User();
            admin.setFirstName("Admin");
            admin.setLastName("Root");
            admin.setEmail("admin@local");
            admin.setPassword(encoder.encode("1234"));
            admin.setRoles(new HashSet<>(Set.of(rAdmin, rUser)));
            users.save(admin);
        });
    }
}