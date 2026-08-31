package models.registration;

import java.util.List;

public record ExistingNoParamResponseModel(
        List<String> username,
        List<String> password
) {
}