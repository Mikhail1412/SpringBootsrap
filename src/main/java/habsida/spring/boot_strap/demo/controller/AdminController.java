package habsida.spring.boot_strap.demo.controller;

import habsida.spring.boot_strap.demo.model.Role;
import habsida.spring.boot_strap.demo.model.User;
import habsida.spring.boot_strap.demo.repositories.RoleRepository;
import habsida.spring.boot_strap.demo.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final RoleRepository roleRepository;

    public AdminController(UserService userService, RoleRepository roleRepository) {
        this.userService = userService;
        this.roleRepository = roleRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", userService.findAll());
        return "admin/list";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("roles", roleRepository.findAll());
        model.addAttribute("selectedRoleIds", List.of());
        return "admin/add";
    }

    @PostMapping("/create")
    public String create(@RequestParam String firstName,
                         @RequestParam String lastName,
                         @RequestParam int age,
                         @RequestParam String email,
                         @RequestParam String password,
                         @RequestParam(name = "roleIds", required = false) List<Long> roleIds) {

        User u = new User();
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setAge(age);
        u.setEmail(email);
        u.setPassword(password);

        if (roleIds != null) {
            var roles = new HashSet<>(roleRepository.findAllById(roleIds));
            u.setRoles(roles);
        }

        userService.save(u);
        return "redirect:/admin";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        var u = userService.findById(id)
                .orElseThrow(() -> new IllegalStateException("User " + id + " not found"));

        model.addAttribute("user", u);
        model.addAttribute("roles", roleRepository.findAll());
        model.addAttribute("selectedRoleIds",
                u.getRoles().stream().map(Role::getId).collect(Collectors.toList()));
        return "admin/edit";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id,
                         @RequestParam String firstName,
                         @RequestParam String lastName,
                         @RequestParam int age,
                         @RequestParam String email,
                         @RequestParam(name = "password", required = false) String password,
                         @RequestParam(name = "roleIds", required = false) List<Long> roleIds) {

        userService.update(id, firstName, lastName, age, email, password, roleIds);
        return "redirect:/admin";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        userService.deleteById(id);
        return "redirect:/admin";
    }
}
