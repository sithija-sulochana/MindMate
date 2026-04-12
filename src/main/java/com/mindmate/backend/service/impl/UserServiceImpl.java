package com.mindmate.backend.service.impl;



import com.mindmate.backend.dto.UserDTO;
import com.mindmate.backend.entities.User;
import com.mindmate.backend.Repository.UserRepository;
import com.mindmate.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;
    private final ModelMapper modelMapper;

    @Override
    public UserDTO createUser(UserDTO userDTO) {
        User user = new User();

        user.setClerkId(userDTO.getClerkId());
        user.setEmail(userDTO.getEmail());
        user.setPetName(userDTO.getPetName());
        user.setBirthDay(userDTO.getBirthDay());

        User savedUser = userRepo.save(user);


        return modelMapper.map(savedUser, UserDTO.class);
    }

    @Override
    public UserDTO getUserByClerkId(String clerkId) {
        User user = userRepo.findByClerkId(clerkId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return modelMapper.map(user, UserDTO.class);
    }
}