package habsida.spring.boot_strap.demo.service;

import habsida.spring.boot_strap.demo.model.User;
import habsida.spring.boot_strap.demo.repositories.RoleRepository;
import habsida.spring.boot_strap.demo.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder encoder;

    public UserServiceImpl(UserRepository users, RoleRepository roles, PasswordEncoder encoder) {
        this.users = users;
        this.roles = roles;
        this.encoder = encoder;
    }

    @Override
    public List<User> findAll() {
        return users.findAll();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return users.findByEmail(email);
    }

    @Override
    public Optional<User> findById(Long id) {
        return users.findById(id);
    }

    private String maybeEncode(String p) {
        if (p == null || p.isBlank()) return null;
        if (p.startsWith("$2a$") || p.startsWith("$2b$") || p.startsWith("$2y$")) return p;
        return encoder.encode(p);
    }

    @Override
    public User save(User user) {
        String enc = maybeEncode(user.getPassword());
        if (enc != null) user.setPassword(enc);
        return users.save(user);
    }

    @Override
    public User create(String firstName, String lastName, String email, String rawPassword, List<Long> roleIds) {
        User u = new User();
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setEmail(email);
        u.setPassword(maybeEncode(rawPassword));
        if (roleIds != null) {
            u.setRoles(new HashSet<>(roles.findAllById(roleIds)));
        }
        return users.save(u);
    }

    @Override
    public User update(Long id, String firstName, String lastName, String email, String rawPassword, List<Long> roleIds) {
        User u = users.findById(id).orElseThrow();
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setEmail(email);
        if (rawPassword != null && !rawPassword.isBlank()) {
            u.setPassword(maybeEncode(rawPassword));
        }
        if (roleIds != null) {
            u.setRoles(new HashSet<>(roles.findAllById(roleIds)));
        }
        return users.save(u);
    }

    @Override
    public void deleteById(Long id) {
        users.deleteById(id);
    }
}
