package com.mindmate.backend.service;

import com.mindmate.backend.dto.UserPreferenceDTO;
import java.util.List;

public interface UserPreferenceService {

    List<UserPreferenceDTO> getPreferencesByUser(String clerkId);


    void updatePreference(String clerkId, UserPreferenceDTO preferenceDTO);
    void updatePreferenceByUserId(Long userId, UserPreferenceDTO dto);
}