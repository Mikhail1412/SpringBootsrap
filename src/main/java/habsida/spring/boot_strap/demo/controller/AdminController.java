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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final RoleRepository roleRepository;

    private static final Pattern NAME_RX = Pattern.compile("^\\p{L}+$");

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
                         @RequestParam(name = "roleIds", required = false) List<Long> roleIds,
                         Model model) {

        if (!NAME_RX.matcher(firstName).matches()) {
            model.addAttribute("roles", roleRepository.findAll());
            model.addAttribute("selectedRoleIds", roleIds == null ? List.of() : roleIds);
            model.addAttribute("errorFirstName", "Имя: только буквы.");
            model.addAttribute("draftFirstName", firstName);
            model.addAttribute("draftLastName", lastName);
            model.addAttribute("draftAge", age);
            model.addAttribute("draftEmail", email);
            return "admin/add";
        }
        if (!NAME_RX.matcher(lastName).matches()) {
            model.addAttribute("roles", roleRepository.findAll());
            model.addAttribute("selectedRoleIds", roleIds == null ? List.of() : roleIds);
            model.addAttribute("errorLastName", "Фамилия: только буквы.");
            model.addAttribute("draftFirstName", firstName);
            model.addAttribute("draftLastName", lastName);
            model.addAttribute("draftAge", age);
            model.addAttribute("draftEmail", email);
            return "admin/add";
        }
        if (age <= 0) {
            model.addAttribute("roles", roleRepository.findAll());
            model.addAttribute("selectedRoleIds", roleIds == null ? List.of() : roleIds);
            model.addAttribute("errorAge", "Возраст должен быть больше 0.");
            model.addAttribute("draftFirstName", firstName);
            model.addAttribute("draftLastName", lastName);
            model.addAttribute("draftAge", age);
            model.addAttribute("draftEmail", email);
            return "admin/add";
        }
        if (roleIds == null || roleIds.isEmpty()) {
            model.addAttribute("roles", roleRepository.findAll());
            model.addAttribute("selectedRoleIds", List.of());
            model.addAttribute("errorRoles", "Выберите хотя бы одну роль.");
            model.addAttribute("draftFirstName", firstName);
            model.addAttribute("draftLastName", lastName);
            model.addAttribute("draftAge", age);
            model.addAttribute("draftEmail", email);
            return "admin/add";
        }

        User u = new User();
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setAge(age);
        u.setEmail(email);
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
                         @RequestParam int age,
                         @RequestParam String email,
                         @RequestParam(name = "password", required = false) String password,
                         @RequestParam(name = "roleIds", required = false) List<Long> roleIds,
                         Model model) {

        User draft = new User();
        draft.setId(id);
        draft.setFirstName(firstName);
        draft.setLastName(lastName);
        draft.setAge(age);
        draft.setEmail(email);

        if (!NAME_RX.matcher(firstName).matches()) {
            model.addAttribute("user", draft);
            model.addAttribute("roles", roleRepository.findAll());
            model.addAttribute("selectedRoleIds", roleIds == null ? List.of() : roleIds);
            model.addAttribute("errorFirstName", "Имя: только буквы.");
            return "admin/edit";
        }
        if (!NAME_RX.matcher(lastName).matches()) {
            model.addAttribute("user", draft);
            model.addAttribute("roles", roleRepository.findAll());
            model.addAttribute("selectedRoleIds", roleIds == null ? List.of() : roleIds);
            model.addAttribute("errorLastName", "Фамилия: только буквы.");
            return "admin/edit";
        }
        if (age <= 0) {
            model.addAttribute("user", draft);
            model.addAttribute("roles", roleRepository.findAll());
            model.addAttribute("selectedRoleIds", roleIds == null ? List.of() : roleIds);
            model.addAttribute("errorAge", "Возраст должен быть больше 0.");
            return "admin/edit";
        }
        if (roleIds == null || roleIds.isEmpty()) {
            model.addAttribute("user", draft);
            model.addAttribute("roles", roleRepository.findAll());
            model.addAttribute("selectedRoleIds", List.of());
            model.addAttribute("errorRoles", "Выберите хотя бы одну роль.");
            return "admin/edit";
        }

        userService.update(id, firstName, lastName, age, email, password, roleIds);
        return "redirect:/admin";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        userService.deleteById(id);
        return "redirect:/admin";
    }
}