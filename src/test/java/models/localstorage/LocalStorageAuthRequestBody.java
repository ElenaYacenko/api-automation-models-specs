package models.localstorage;

import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

public record LocalStorageAuthRequestBody(
        @JsonProperty("user") UserData user,
        @JsonProperty("accessToken") String access,
        @JsonProperty("refreshToken") String refresh,
        @JsonProperty("isAuthenticated") boolean authenticated
) {
    private static final JsonMapper JSON = JsonMapper.builder().build();

    public String toJson() {
        try {
            return JSON.writeValueAsString(this);
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }
}