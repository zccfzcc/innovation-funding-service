package org.innovateuk.ifs.publiccontent.service;

import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.publiccontent.resource.PublicContentResource;

public interface PublicContentService {

    ServiceResult<PublicContentResource> getCompetitionById(final Long competitionId);

    ServiceResult<Void> publishByCompetitionId(Long publicContentId);
}
