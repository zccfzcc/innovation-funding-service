package com.worth.ifs.assessment.security;

import com.worth.ifs.invite.domain.CompetitionInvite;
import com.worth.ifs.invite.resource.CompetitionInviteResource;
import com.worth.ifs.invite.resource.CompetitionParticipantResource;
import com.worth.ifs.security.BasePermissionRules;
import com.worth.ifs.security.PermissionRule;
import com.worth.ifs.security.PermissionRules;
import com.worth.ifs.user.resource.UserResource;
import com.worth.ifs.user.resource.UserRoleType;
import org.springframework.stereotype.Component;

/**
 * Provides the permissions around CRUD operations for {@link com.worth.ifs.invite.domain.CompetitionParticipant} resources.
 */
@Component
@PermissionRules
public class CompetitionParticipantPermissionRules extends BasePermissionRules {

    @PermissionRule(value = "ACCEPT", description = "only the same user can accept an invitation")
    public boolean userCanAcceptCompetitionInvite(CompetitionParticipantResource competitionParticipant, UserResource user) {
        return  user != null &&
                competitionParticipant != null &&
                isAssessor(user) &&
                isSameUser(competitionParticipant, user);
    }

    private boolean isAssessor(UserResource user) {
        return user.hasRole(UserRoleType.ASSESSOR);
    }

    private static boolean isSameUser(CompetitionParticipantResource competitionParticipant, UserResource user) {
        if (user.getId() == competitionParticipant.getUserId()) {
            return true;
        }
        else if (   competitionParticipant.getUserId() == null &&
                    competitionParticipant.getInvite() !=null &&
                    user.getEmail().equals(competitionParticipant.getInvite().getEmail())) {
            return true;
        }
        return false;
    }
}