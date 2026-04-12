package com.mindmate.backend.service.impl;


import com.mindmate.backend.dto.UserPreferenceDTO;
import com.mindmate.backend.entities.User;
import com.mindmate.backend.entities.UserPreference;
import com.mindmate.backend.Repository.UserPreferenceRepository;
import com.mindmate.backend.Repository.UserRepository;
import com.mindmate.backend.service.UserPreferenceService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserPreferenceServiceImpl implements UserPreferenceService {

    private final UserPreferenceRepository preferenceRepo;
    private final UserRepository userRepo;
    private final ModelMapper modelMapper;

    @Override
    public List<UserPreferenceDTO> getPreferencesByUser(String clerkId) {
        User user = userRepo.findByClerkId(clerkId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return preferenceRepo.findByUserId(user.getId()).stream()
                .map(p -> modelMapper.map(p, UserPreferenceDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public void updatePreference(String clerkId, UserPreferenceDTO dto) {
        User user = userRepo.findByClerkId(clerkId)
                .orElseThrow(() -> new RuntimeException("User not found"));


        UserPreference pref = preferenceRepo.findByUserIdAndPrefKey(user.getId(), dto.getPrefKey())
                .orElse(new UserPreference());

        pref.setUser(user);
        pref.setPrefKey(dto.getPrefKey());
        pref.setPrefValue(dto.getPrefValue());

        preferenceRepo.save(pref);
    }

    @Override
    @Transactional
    public void updatePreferenceByUserId(Long userId, UserPreferenceDTO dto) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserPreference pref = preferenceRepo.findByUserIdAndPrefKey(userId, dto.getPrefKey())
                .orElse(new UserPreference());

        pref.setUser(user);
        pref.setPrefKey(dto.getPrefKey());
        pref.setPrefValue(dto.getPrefValue());

        preferenceRepo.save(pref);
    }
}
