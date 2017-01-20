package org.innovateuk.ifs.assessment.builder;

import org.innovateuk.ifs.BaseBuilder;
import org.innovateuk.ifs.assessment.resource.AssessorProfileResource;
import org.innovateuk.ifs.assessment.resource.ProfileResource;
import org.innovateuk.ifs.category.resource.InnovationAreaResource;
import org.innovateuk.ifs.user.builder.UserProfileBaseResourceBuilder;
import org.innovateuk.ifs.user.resource.AffiliationResource;
import org.innovateuk.ifs.user.resource.BusinessType;
import org.innovateuk.ifs.user.resource.UserProfileResource;

import java.util.List;
import java.util.function.BiConsumer;

import static java.util.Collections.emptyList;

/**
 * Builder for {@link ProfileResource}.
 */
public class AssessorProfileResourceBuilder extends BaseBuilder<AssessorProfileResource, AssessorProfileResourceBuilder> {

    public static AssessorProfileResourceBuilder newAssessorProfileResource() {
        return new AssessorProfileResourceBuilder(emptyList());
    }

    public AssessorProfileResourceBuilder(List<BiConsumer<Integer, AssessorProfileResource>> newActions) {
        super(newActions);
    }

//    public AssessorProfileResourceBuilder withInnovationAreas(List<InnovationAreaResource>... innovationAreas) {
//        return withArraySetFieldByReflection("innovationAreas", innovationAreas);
//    }
//
//    public AssessorProfileResourceBuilder withBusinessType(BusinessType... businessTypes) {
//        return withArraySetFieldByReflection("businessType", businessTypes);
//    }
//
//    public AssessorProfileResourceBuilder withSkillsAreas(String... skillsAreas) {
//        return withArraySetFieldByReflection("skillsAreas", skillsAreas);
//    }
//
//    public AssessorProfileResourceBuilder withAffiliations(List<AffiliationResource>... affiliations) {
//        return withArraySetFieldByReflection("affiliations", affiliations);
//    }

    public AssessorProfileResourceBuilder withUserProfile(UserProfileResource... userProfiles) {
        return withArraySetFieldByReflection("user", userProfiles);
    }

    public AssessorProfileResourceBuilder withProfile(ProfileResource... profiles) {
        return withArraySetFieldByReflection("profile", profiles);
    }

    @Override
    protected AssessorProfileResourceBuilder createNewBuilderWithActions(List<BiConsumer<Integer, AssessorProfileResource>> actions) {
        return new AssessorProfileResourceBuilder(actions);
    }

    @Override
    protected AssessorProfileResource createInitial() {
        return new AssessorProfileResource();
    }
}
