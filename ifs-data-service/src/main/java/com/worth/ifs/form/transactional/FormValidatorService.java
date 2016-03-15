package com.worth.ifs.form.transactional;

import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.form.resource.FormValidatorResource;
import com.worth.ifs.security.NotSecured;

public interface FormValidatorService {

    @NotSecured("TODO")
    ServiceResult<FormValidatorResource> findOne(Long id);
}