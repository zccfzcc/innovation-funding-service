package com.worth.ifs.commons.service;

import java.util.List;

import com.worth.ifs.application.domain.Question;
import com.worth.ifs.application.domain.QuestionStatus;
import com.worth.ifs.application.domain.Response;
import com.worth.ifs.application.resource.ApplicationResource;
import com.worth.ifs.application.resource.QuestionStatusResource;
import com.worth.ifs.assessment.domain.Assessment;
import com.worth.ifs.competition.domain.Competition;
import com.worth.ifs.competition.resource.CompetitionResource;
import com.worth.ifs.finance.domain.Cost;
import com.worth.ifs.finance.resource.ApplicationFinanceResource;
import com.worth.ifs.finance.resource.CostFieldResource;
import com.worth.ifs.finance.resource.cost.CostItem;
import com.worth.ifs.form.domain.FormInputResponse;
import com.worth.ifs.invite.resource.InviteOrganisationResource;
import com.worth.ifs.user.domain.ProcessRole;
import com.worth.ifs.user.domain.User;
import com.worth.ifs.user.resource.OrganisationTypeResource;
import com.worth.ifs.user.resource.UserResource;

import org.springframework.core.ParameterizedTypeReference;

/**
 * A utility for commonly used ParameterizedTypeReferences
 */
public class ParameterizedTypeReferences {


    /**
     * Basic types
     */

    public static ParameterizedTypeReference<List<Long>> longsListType() {
        return new ParameterizedTypeReference<List<Long>>() {};
    }

    public static ParameterizedTypeReference<List<String>> stringsListType() {
        return new ParameterizedTypeReference<List<String>>() {};
    }

    /**
     * IFS types
     */

    public static ParameterizedTypeReference<List<ApplicationResource>> applicationResourceListType() {
        return new ParameterizedTypeReference<List<ApplicationResource>>() {};
    }

    public static ParameterizedTypeReference<List<ProcessRole>> processRoleListType() {
        return new ParameterizedTypeReference<List<ProcessRole>>() {};
    }

    public static ParameterizedTypeReference<List<User>> userListType() {
        return new ParameterizedTypeReference<List<User>>() {};
    }

    public static ParameterizedTypeReference<List<UserResource>> userResourceListType() {
        return new ParameterizedTypeReference<List<UserResource>>() {};
    }

    public static ParameterizedTypeReference<List<Competition>> competitionListType() {
        return new ParameterizedTypeReference<List<Competition>>() {};
    }

    public static ParameterizedTypeReference<List<CompetitionResource>> competitionResourceListType() {
        return new ParameterizedTypeReference<List<CompetitionResource>>() {};
    }

    public static ParameterizedTypeReference<List<QuestionStatus>> questionStatusListType() {
        return new ParameterizedTypeReference<List<QuestionStatus>>() {};
    }

    public static ParameterizedTypeReference<List<QuestionStatusResource>> questionStatusResourceListType() {
        return new ParameterizedTypeReference<List<QuestionStatusResource>>() {};
    }

    public static ParameterizedTypeReference<List<Response>> responseListType() {
        return new ParameterizedTypeReference<List<Response>>() {};
    }

    public static ParameterizedTypeReference<List<Question>> questionListType() {
        return new ParameterizedTypeReference<List<Question>>() {};
    }

    public static ParameterizedTypeReference<List<Assessment>> assessmentListType() {
        return new ParameterizedTypeReference<List<Assessment>>() {};
    }

    public static ParameterizedTypeReference<List<FormInputResponse>> formInputResponseListType() {
        return new ParameterizedTypeReference<List<FormInputResponse>>() {};
    }

    public static ParameterizedTypeReference<List<CostFieldResource>> costFieldResourceListType() {
        return new ParameterizedTypeReference<List<CostFieldResource>>() {};
    }

    public static ParameterizedTypeReference<List<InviteOrganisationResource>> inviteOrganisationResourceListType() {
        return new ParameterizedTypeReference<List<InviteOrganisationResource>>() {};
    }

    public static ParameterizedTypeReference<List<Cost>> costListType() {
        return new ParameterizedTypeReference<List<Cost>>() {};
    }

    public static ParameterizedTypeReference<List<CostItem>> costItemListType() {
        return new ParameterizedTypeReference<List<CostItem>>() {};
    }

    public static ParameterizedTypeReference<List<ApplicationFinanceResource>> applicationFinanceResourceListType() {
        return new ParameterizedTypeReference<List<ApplicationFinanceResource>>() {};
    }

    public static ParameterizedTypeReference<List<OrganisationTypeResource>> organisationTypeResourceListType() {
        return new ParameterizedTypeReference<List<OrganisationTypeResource>>() {};
    }
}
