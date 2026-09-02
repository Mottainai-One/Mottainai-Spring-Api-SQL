package com.institutojf.mottainai.dto.request;

import com.institutojf.mottainai.model.enums.PriorityLevel;
import com.institutojf.mottainai.model.enums.SuggestedActionType;
import jakarta.validation.constraints.NotNull;

public record CreateSuggestedActionRequest(
    @NotNull Integer alertId,
    @NotNull SuggestedActionType actionType,
    String description,
    @NotNull PriorityLevel priority
) {
}
