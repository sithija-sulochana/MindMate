package com.mindmate.backend.controller;



import com.mindmate.backend.dto.UserDTO;
import com.mindmate.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;


    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.createUser(userDTO));
    }


    @GetMapping("/{clerkId}")
    public ResponseEntity<UserDTO> getUser(@PathVariable String clerkId) {
        return ResponseEntity.ok(userService.getUserByClerkId(clerkId));
    }
}