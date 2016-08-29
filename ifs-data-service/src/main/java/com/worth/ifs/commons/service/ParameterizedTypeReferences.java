package com.worth.ifs.commons.service;

import com.worth.ifs.address.resource.AddressResource;
import com.worth.ifs.alert.resource.AlertResource;
import com.worth.ifs.application.resource.ApplicationResource;
import com.worth.ifs.application.resource.QuestionResource;
import com.worth.ifs.application.resource.QuestionStatusResource;
import com.worth.ifs.assessment.resource.AssessorFormInputResponseResource;
import com.worth.ifs.commons.rest.ValidationMessages;
import com.worth.ifs.category.resource.CategoryResource;
import com.worth.ifs.competition.resource.CompetitionResource;
import com.worth.ifs.competition.resource.CompetitionTypeResource;
import com.worth.ifs.competition.resource.MilestoneResource;
import com.worth.ifs.finance.domain.FinanceRow;
import com.worth.ifs.finance.resource.ApplicationFinanceResource;
import com.worth.ifs.finance.resource.FinanceRowMetaFieldResource;
import com.worth.ifs.finance.resource.cost.FinanceRowItem;
import com.worth.ifs.form.resource.FormInputResource;
import com.worth.ifs.form.resource.FormInputResponseResource;
import com.worth.ifs.invite.resource.InviteOrganisationResource;
import com.worth.ifs.invite.resource.InviteProjectResource;
import com.worth.ifs.invite.resource.InviteResource;
import com.worth.ifs.project.resource.ProjectResource;
import com.worth.ifs.project.resource.ProjectUserResource;
import com.worth.ifs.user.resource.OrganisationResource;
import com.worth.ifs.user.resource.OrganisationTypeResource;
import com.worth.ifs.user.resource.ProcessRoleResource;
import com.worth.ifs.user.resource.UserResource;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;
import java.util.Set;

/**
 * A utility for commonly used ParameterizedTypeReferences
 */
public final class ParameterizedTypeReferences {

	private ParameterizedTypeReferences() {}

    /**
     * Basic types
     */

    public static ParameterizedTypeReference<List<Long>> longsListType() {
        return new ParameterizedTypeReference<List<Long>>() {};
    }

    public static ParameterizedTypeReference<Set<Long>> longsSetType() {
        return new ParameterizedTypeReference<Set<Long>>() {};
    }

    public static ParameterizedTypeReference<List<String>> stringsListType() {
        return new ParameterizedTypeReference<List<String>>() {};
    }

    /**
     * IFS types
     */

    public static ParameterizedTypeReference<List<AlertResource>> alertResourceListType() {
        return new ParameterizedTypeReference<List<AlertResource>>() {};
    }

    public static ParameterizedTypeReference<List<ApplicationResource>> applicationResourceListType() {
        return new ParameterizedTypeReference<List<ApplicationResource>>() {};
    }

    public static ParameterizedTypeReference<List<AssessorFormInputResponseResource>> assessorFormInputResponseResourceListType() {
        return new ParameterizedTypeReference<List<AssessorFormInputResponseResource>>() {};
    }

    public static ParameterizedTypeReference<List<ProcessRoleResource>> processRoleResourceListType() {
        return new ParameterizedTypeReference<List<ProcessRoleResource>>() {};
    }

    public static ParameterizedTypeReference<List<UserResource>> userListType() {
        return new ParameterizedTypeReference<List<UserResource>>() {};
    }

    public static ParameterizedTypeReference<List<CompetitionResource>> competitionResourceListType() {
        return new ParameterizedTypeReference<List<CompetitionResource>>() {};
    }

    public static ParameterizedTypeReference<List<CategoryResource>> categoryResourceListType() {
        return new ParameterizedTypeReference<List<CategoryResource>>() {};
    }

    public static ParameterizedTypeReference<List<CompetitionTypeResource>> competitionTypeResourceListType() {
        return new ParameterizedTypeReference<List<CompetitionTypeResource>>() {};
    }

    public static ParameterizedTypeReference<List<QuestionStatusResource>> questionStatusResourceListType() {
        return new ParameterizedTypeReference<List<QuestionStatusResource>>() {};
    }

    public static ParameterizedTypeReference<List<FormInputResource>> formInputResourceListType() {
        return new ParameterizedTypeReference<List<FormInputResource>>() {};
    }

    public static ParameterizedTypeReference<List<QuestionResource>> questionResourceListType() {
        return new ParameterizedTypeReference<List<QuestionResource>>() {};
    }

    public static ParameterizedTypeReference<List<FormInputResponseResource>> formInputResponseListType() {
        return new ParameterizedTypeReference<List<FormInputResponseResource>>() {};
    }

    public static ParameterizedTypeReference<List<FinanceRowMetaFieldResource>> financeRowMetaFieldResourceListType() {
        return new ParameterizedTypeReference<List<FinanceRowMetaFieldResource>>() {};
    }

    public static ParameterizedTypeReference<List<InviteOrganisationResource>> inviteOrganisationResourceListType() {
        return new ParameterizedTypeReference<List<InviteOrganisationResource>>() {};
    }

    public static ParameterizedTypeReference<List<InviteProjectResource>> inviteProjectResourceListType() {
        return new ParameterizedTypeReference<List<InviteProjectResource>>() {};
    }

    public static ParameterizedTypeReference<List<FinanceRow>> costListType() {
        return new ParameterizedTypeReference<List<FinanceRow>>() {};
    }

    public static ParameterizedTypeReference<List<FinanceRowItem>> costItemListType() {
        return new ParameterizedTypeReference<List<FinanceRowItem>>() {};
    }

    public static ParameterizedTypeReference<List<ApplicationFinanceResource>> applicationFinanceResourceListType() {
        return new ParameterizedTypeReference<List<ApplicationFinanceResource>>() {};
    }

    public static ParameterizedTypeReference<List<OrganisationTypeResource>> organisationTypeResourceListType() {
        return new ParameterizedTypeReference<List<OrganisationTypeResource>>() {};
    }

    public static ParameterizedTypeReference<List<AddressResource>> addressResourceListType() {
        return new ParameterizedTypeReference<List<AddressResource>>() {};
    }

    public static ParameterizedTypeReference<List<OrganisationResource>> organisationResourceListType() {
        return new ParameterizedTypeReference<List<OrganisationResource>>() {};
    }

    public static ParameterizedTypeReference<List<ProjectResource>> projectResourceListType() {
        return new ParameterizedTypeReference<List<ProjectResource>>() {
        };
    }

    public static ParameterizedTypeReference<List<ProjectUserResource>> projectUserResourceList() {
        return new ParameterizedTypeReference<List<ProjectUserResource>>() {
        };
    }

    public static ParameterizedTypeReference<List<ValidationMessages>> validationMessagesListType() {
        return new ParameterizedTypeReference<List<ValidationMessages>>() {};
    }
    public static ParameterizedTypeReference<List<MilestoneResource>> milestoneResourceListType() {
        return new ParameterizedTypeReference<List<MilestoneResource>>() {};
    }
}
