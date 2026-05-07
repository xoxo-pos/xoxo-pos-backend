package com.xoxo.pos.users;
import org.springframework.security.crypto.password.PasswordEncoder;import org.springframework.web.bind.annotation.*;import java.util.List;
@RestController @RequestMapping("/api/users") public class UserController{private final UserRepository repo;private final PasswordEncoder enc;public UserController(UserRepository repo,PasswordEncoder enc){this.repo=repo;this.enc=enc;}
@GetMapping public List<UserEntity> list(){return repo.findAll();}
@PostMapping public UserEntity create(@RequestBody CreateUserRequest r){if(repo.existsByUsername(r.username()))throw new RuntimeException("El usuario ya existe");return repo.save(new UserEntity(r.username(),enc.encode(r.password()),r.role()==null?Role.CASHIER:r.role(),true));}
@PutMapping("/{id}") public UserEntity update(@PathVariable Long id,@RequestBody UpdateUserRequest r){var u=repo.findById(id).orElseThrow();if(r.username()!=null&&!r.username().isBlank())u.setUsername(r.username());if(r.password()!=null&&!r.password().isBlank())u.setPassword(enc.encode(r.password()));if(r.role()!=null)u.setRole(r.role());if(r.active()!=null)u.setActive(r.active());return repo.save(u);}
@PatchMapping("/{id}/toggle") public UserEntity toggle(@PathVariable Long id){var u=repo.findById(id).orElseThrow();u.setActive(!u.isActive());return repo.save(u);}}
