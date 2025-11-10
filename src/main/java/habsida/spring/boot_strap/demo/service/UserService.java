package habsida.spring.boot_strap.demo.service;

import habsida.spring.boot_strap.demo.model.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> findAll();
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);
    User save(User user);
    User create(String firstName,
                String lastName,
                int age,
                String email,
                String rawPassword,
                List<Long> roleIds);
    User update(Long id,
                String firstName,
                String lastName,
                int age,
                String email,
                String rawPassword,
                List<Long> roleIds);
    void deleteById(Long id);
}
