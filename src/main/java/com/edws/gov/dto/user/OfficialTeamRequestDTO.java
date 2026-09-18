package com.edws.gov.dto.user;

import java.util.List;

public record OfficialTeamRequestDTO(
        List<String> reviewerIds,
        List<String> drawerIds
) {
}
