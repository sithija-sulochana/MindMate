package com.mindmate.backend.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPreferenceDTO {
    private String prefKey;
    private String prefValue;

    // getters and setters
    public String getPrefKey() {
        return prefKey;
    }
    public void setPrefKey(String prefKey) {
        this.prefKey = prefKey;
    }
    public String getPrefValue() {
        return prefValue;

    }
    public void setPrefValue(String prefValue) {
        this.prefValue = prefValue;
    }

}
