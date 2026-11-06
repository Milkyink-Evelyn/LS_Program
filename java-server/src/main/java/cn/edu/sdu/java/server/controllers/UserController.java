package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.ChangePasswordRequest;
import cn.edu.sdu.java.server.payload.request.UpdateProfileRequest;
import cn.edu.sdu.java.server.payload.request.UserCreateRequest;
import cn.edu.sdu.java.server.payload.request.UserUpdateRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.UserService;
import cn.edu.sdu.java.server.util.CommonMethod;
import cn.edu.sdu.java.server.util.RoleUtil;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users/me")
    public DataResponse getCurrentUser() {
        return CommonMethod.getReturnData(userService.getCurrentUser());
    }

    @PutMapping("/users/me")
    public DataResponse updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        userService.updateProfile(request);
        return CommonMethod.getReturnMessageOK();
    }

    @PutMapping("/users/me/password")
    public DataResponse changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return CommonMethod.getReturnMessageOK();
    }

    @GetMapping("/admin/users")
    public DataResponse listUsers(@RequestParam(required = false) String role,
                                  @RequestParam(required = false) String keyword) {
        RoleUtil.requireAdmin();
        return CommonMethod.getReturnData(userService.listUsers(role, keyword));
    }

    @PostMapping("/admin/users")
    public DataResponse createUser(@Valid @RequestBody UserCreateRequest request) {
        RoleUtil.requireAdmin();
        return CommonMethod.getReturnData(userService.createUser(request));
    }

    @PutMapping("/admin/users/{id}")
    public DataResponse updateUser(@PathVariable Integer id,
                                   @Valid @RequestBody UserUpdateRequest request) {
        RoleUtil.requireAdmin();
        userService.updateUser(id, request);
        return CommonMethod.getReturnMessageOK();
    }

    @DeleteMapping("/admin/users/{id}")
    public DataResponse deleteUser(@PathVariable Integer id) {
        RoleUtil.requireAdmin();
        userService.deleteUser(id);
        return CommonMethod.getReturnMessageOK();
    }
}
