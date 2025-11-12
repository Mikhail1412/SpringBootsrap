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
                         @RequestParam(required = false) Integer age,
                         @RequestParam String email,
                         @RequestParam String password,
                         @RequestParam(name = "roleIds", required = false) List<Long> roleIds,
                         Model model) {

        String nameRe = "^[\\p{L}\\s-]+$";

        if (firstName == null || !firstName.trim().matches(nameRe)) {
            return backToAddWithError(model, "Имя: только буквы/пробел/дефис.",
                    firstName, lastName, age, email, roleIds);
        }
        if (lastName == null || !lastName.trim().matches(nameRe)) {
            return backToAddWithError(model, "Фамилия: только буквы/пробел/дефис.",
                    firstName, lastName, age, email, roleIds);
        }
        if (age == null || age <= 0) {
            return backToAddWithError(model, "Возраст должен быть положительным числом.",
                    firstName, lastName, age, email, roleIds);
        }
        if (roleIds == null || roleIds.isEmpty()) {
            return backToAddWithError(model, "Выберите хотя бы одну роль.",
                    firstName, lastName, age, email, roleIds);
        }

        User u = new User();
        u.setFirstName(firstName.trim());
        u.setLastName(lastName.trim());
        u.setAge(age);
        u.setEmail(email.trim());
        u.setPassword(password);
        u.setRoles(new HashSet<>(roleRepository.findAllById(roleIds)));

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
                         @RequestParam(required = false) Integer age,
                         @RequestParam String email,
                         @RequestParam(name = "password", required = false) String password,
                         @RequestParam(name = "roleIds", required = false) List<Long> roleIds,
                         Model model) {

        String nameRe = "^[\\p{L}\\s-]+$";

        if (firstName == null || !firstName.trim().matches(nameRe)) {
            return backToEditWithError(model, id, firstName, lastName, age, email,
                    "Имя: только буквы/пробел/дефис.", roleIds);
        }
        if (lastName == null || !lastName.trim().matches(nameRe)) {
            return backToEditWithError(model, id, firstName, lastName, age, email,
                    "Фамилия: только буквы/пробел/дефис.", roleIds);
        }
        if (age == null || age <= 0) {
            return backToEditWithError(model, id, firstName, lastName, age, email,
                    "Возраст должен быть положительным числом.", roleIds);
        }
        if (roleIds == null || roleIds.isEmpty()) {
            return backToEditWithError(model, id, firstName, lastName, age, email,
                    "Выберите хотя бы одну роль.", roleIds);
        }

        userService.update(id, firstName.trim(), lastName.trim(), age, email.trim(), password, roleIds);
        return "redirect:/admin";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        userService.deleteById(id);
        return "redirect:/admin";
    }

    private String backToAddWithError(Model model, String err,
                                      String firstName, String lastName, Integer age, String email,
                                      List<Long> roleIds) {
        model.addAttribute("roles", roleRepository.findAll());
        model.addAttribute("selectedRoleIds", roleIds == null ? List.of() : roleIds);
        model.addAttribute("errorRoles", err);
        model.addAttribute("draftFirstName", firstName);
        model.addAttribute("draftLastName", lastName);
        model.addAttribute("draftAge", age);
        model.addAttribute("draftEmail", email);
        return "admin/add";
    }

    private String backToEditWithError(Model model, Long id,
                                       String firstName, String lastName, Integer age, String email,
                                       String err, List<Long> roleIds) {
        User draft = new User();
        draft.setId(id);
        draft.setFirstName(firstName);
        draft.setLastName(lastName);
        if (age != null) draft.setAge(age);
        draft.setEmail(email);

        model.addAttribute("user", draft);
        model.addAttribute("roles", roleRepository.findAll());
        model.addAttribute("selectedRoleIds", roleIds == null ? List.of() : roleIds);
        model.addAttribute("errorRoles", err);
        return "admin/edit";
    }
}