package org.innovateuk.ifs.invite.builder;

import org.innovateuk.ifs.BaseBuilder;
import org.innovateuk.ifs.category.resource.CategoryResource;
import org.innovateuk.ifs.invite.resource.AssessorInviteOverviewResource;

import java.util.List;
import java.util.function.BiConsumer;

import static java.util.Collections.emptyList;

public class AssessorInviteOverviewResourceBuilder extends BaseBuilder<AssessorInviteOverviewResource, AssessorInviteOverviewResourceBuilder> {

    private AssessorInviteOverviewResourceBuilder(List<BiConsumer<Integer, AssessorInviteOverviewResource>> newMultiActions) {
        super(newMultiActions);
    }

    @Override
    protected AssessorInviteOverviewResourceBuilder createNewBuilderWithActions(List<BiConsumer<Integer, AssessorInviteOverviewResource>> actions) {
        return new AssessorInviteOverviewResourceBuilder(actions);
    }

    @Override
    protected AssessorInviteOverviewResource createInitial() {
        return new AssessorInviteOverviewResource();
    }

    public static AssessorInviteOverviewResourceBuilder newAssessorInviteOverviewResource() {
        return new AssessorInviteOverviewResourceBuilder(emptyList());
    }

    public AssessorInviteOverviewResourceBuilder withFirstName(String... value) {
        return withArraySetFieldByReflection("firstName", value);
    }

    public AssessorInviteOverviewResourceBuilder withLastName(String... value) {
        return withArraySetFieldByReflection("lastName", value);
    }

    public AssessorInviteOverviewResourceBuilder withInnovationArea(CategoryResource... value) {
        return withArraySetFieldByReflection("innovationArea", value);
    }

    public AssessorInviteOverviewResourceBuilder withCompliant(Boolean... value) {
        return withArraySetFieldByReflection("compliant", value);
    }

    public AssessorInviteOverviewResourceBuilder withStatus(String... value) {
        return withArraySetFieldByReflection("status", value);
    }


    public AssessorInviteOverviewResourceBuilder withDetails(String... value) {
        return withArraySetFieldByReflection("details", value);
    }
}