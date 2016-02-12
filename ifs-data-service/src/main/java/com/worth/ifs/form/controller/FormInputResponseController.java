package com.worth.ifs.form.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.form.domain.FormInputResponse;
import com.worth.ifs.form.repository.FormInputResponseRepository;
import com.worth.ifs.form.transactional.FormInputService;
import com.worth.ifs.validator.ValidatedResponse;
import com.worth.ifs.validator.util.ValidationUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

import static com.worth.ifs.commons.service.ServiceResult.serviceSuccess;

/**
 * ApplicationController exposes Application data and operations through a REST API.
 */
@RestController
@RequestMapping("/forminputresponse")
public class FormInputResponseController {

    @Autowired
    private FormInputResponseRepository formInputResponseRepository;

    @Autowired
    private FormInputService formInputService;

    private static final Log LOG = LogFactory.getLog(FormInputResponseController.class);

    @RequestMapping("/findResponsesByApplication/{applicationId}")
    public RestResult<List<FormInputResponse>> findResponsesByApplication(@PathVariable("applicationId") final Long applicationId){
        return formInputService.findResponsesByApplication(applicationId).toDefaultRestResultForGet();
    }

    @RequestMapping(value = "/saveQuestionResponse", method = RequestMethod.POST)
    public RestResult<List<String>> saveQuestionResponse(@RequestBody JsonNode jsonObj) {

        Long userId = jsonObj.get("userId").asLong();
        Long applicationId = jsonObj.get("applicationId").asLong();
        Long formInputId = jsonObj.get("formInputId").asLong();
        String value = HtmlUtils.htmlUnescape(jsonObj.get("value").asText(""));

        ServiceResult<List<String>> result = formInputService.saveQuestionResponse(userId, applicationId, formInputId, value).andOnSuccess(response -> {

            BindingResult bindingResult = ValidationUtil.validateResponse(response);
            if (bindingResult.hasErrors()) {
                LOG.debug("Got validation errors: ");
                bindingResult.getAllErrors().stream().forEach(e -> LOG.debug("Validation: " + e.getDefaultMessage()));
            }

            formInputResponseRepository.save(response);
            LOG.debug("Single question saved!");

            ValidatedResponse validatedResponse = new ValidatedResponse(bindingResult, response);
            return serviceSuccess(validatedResponse.getAllErrors());
        });

        return result.toDefaultRestResultForPutWithBody();
    }
}