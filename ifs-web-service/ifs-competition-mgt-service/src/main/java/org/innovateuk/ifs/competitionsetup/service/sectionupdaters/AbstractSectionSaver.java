package org.innovateuk.ifs.competitionsetup.service.sectionupdaters;

import org.innovateuk.ifs.commons.error.Error;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competitionsetup.form.CompetitionSetupForm;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.innovateuk.ifs.commons.service.ServiceResult.serviceFailure;
import static org.apache.commons.beanutils.ConvertUtils.convert;
import static org.apache.commons.beanutils.PropertyUtils.getPropertyType;
import static org.apache.commons.beanutils.PropertyUtils.setNestedProperty;

/**
 * Class to hold all the common functionality in the section savers.
 */
public abstract class AbstractSectionSaver implements CompetitionSetupSaver {

    @Override
    public ServiceResult<Void> autoSaveSectionField(CompetitionResource competitionResource, CompetitionSetupForm form, String fieldName, String value, Optional<Long> questionId) {
        try {
            form.setMarkAsCompleteAction(false);
            Class<?> propertyType = getPropertyType(form, fieldName);
            setNestedProperty(form, fieldName, convert(value, propertyType));
            return saveSection(competitionResource, form);
        } catch (Exception e) {
            return handleIrregularAutosaveCase(competitionResource, fieldName, value, questionId);
        }
    }


    protected ServiceResult<Void> handleIrregularAutosaveCase(CompetitionResource competitionResource, String fieldName, String value, Optional<Long> questionId) {
        return serviceFailure(new Error("Field not found", HttpStatus.BAD_REQUEST));
    }

}
