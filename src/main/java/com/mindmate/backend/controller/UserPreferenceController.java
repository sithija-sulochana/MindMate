package com.mindmate.backend.controller;



import com.mindmate.backend.dto.UserPreferenceDTO;
import com.mindmate.backend.service.UserPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/preferences")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserPreferenceController {

    private final UserPreferenceService preferenceService;

    @GetMapping("/{clerkId}")
    public ResponseEntity<List<UserPreferenceDTO>> getPreferences(@PathVariable String clerkId) {
        return ResponseEntity.ok(preferenceService.getPreferencesByUser(clerkId));
    }

    @PostMapping("/{clerkId}")
    public ResponseEntity<String> savePreference(@PathVariable String clerkId, @RequestBody UserPreferenceDTO dto) {
        preferenceService.updatePreference(clerkId, dto);
        return ResponseEntity.ok("Preference saved!");
    }
}
