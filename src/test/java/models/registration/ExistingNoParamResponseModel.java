package models.registration;

import java.util.List;
import java.util.SequencedCollection;

public record ExistingNoParamResponseModel(
        List<String> username,
        List<String> password
) {}