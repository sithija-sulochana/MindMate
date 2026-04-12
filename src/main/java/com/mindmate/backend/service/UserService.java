package com.mindmate.backend.service;



import com.mindmate.backend.dto.UserDTO;

public interface UserService {

    UserDTO createUser(UserDTO userDTO);


    UserDTO getUserByClerkId(String clerkId);
}