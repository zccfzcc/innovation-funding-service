package org.innovateuk.ifs.project.queries.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.innovateuk.ifs.application.service.OrganisationService;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.controller.ValidationHandler;
import org.innovateuk.ifs.file.resource.FileEntryResource;
import org.innovateuk.ifs.project.ProjectService;
import org.innovateuk.ifs.project.queries.form.FinanceChecksQueriesAddResponseForm;
import org.innovateuk.ifs.project.queries.form.FinanceChecksQueriesFormConstraints;
import org.innovateuk.ifs.project.queries.viewmodel.*;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.innovateuk.ifs.project.resource.ProjectUserResource;
import org.innovateuk.ifs.user.resource.OrganisationResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.service.UserService;
import org.innovateuk.ifs.util.CookieUtil;
import org.innovateuk.ifs.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;

import static org.innovateuk.ifs.file.controller.FileDownloadControllerUtils.getFileResponseEntity;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleFindFirst;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping(FinanceChecksQueriesAddResponseController.FINANCE_CHECKS_QUERIES_BASE_URL)
public class FinanceChecksQueriesAddResponseController {

    @Autowired
    private OrganisationService organisationService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private CookieUtil cookieUtil;

    public static final String FINANCE_CHECKS_QUERIES_BASE_URL = "/project/{projectId}/finance-check/organisation/{organisationId}/query/{queryId}/new-response";

    private static final String ATTACHMENT_COOKIE = "finance_checks_queries_new_response_attachments";
    private static final String FORM_ATTR = "form";
    private static final String UNKNOWN_FIELD = "Unknown";

    @PreAuthorize("hasPermission(#projectId, 'ACCESS_FINANCE_CHECKS_QUERIES_SECTION')")
    @RequestMapping(method = GET)
    public String viewNewResponse(@PathVariable Long projectId,
                                  @PathVariable Long organisationId,
                                  @PathVariable Long queryId,
                                  @RequestParam(value = "query_section", required = false) String querySection,
                                  Model model,
                                  @ModelAttribute("loggedInUser") UserResource loggedInUser,
                                  HttpServletRequest request) {

        List<Long> attachments = loadAttachmentsFromCookie(request, projectId, organisationId);
        FinanceChecksQueriesAddResponseViewModel viewModel = populateQueriesViewModel(projectId, organisationId, querySection, attachments, queryId);
        model.addAttribute("model", viewModel);
        return "project/financecheck/new-response";
    }

    @PreAuthorize("hasPermission(#projectId, 'ACCESS_FINANCE_CHECKS_QUERIES_SECTION')")
    @RequestMapping(method = POST, params = "uploadAttachment")
    public String saveNewResponseAttachment(Model model,
                                            @PathVariable("projectId") final Long projectId,
                                            @PathVariable Long organisationId,
                                            @PathVariable Long queryId,
                                            @RequestParam(value = "query_section", required = false) String querySection,
                                            @ModelAttribute(FORM_ATTR) FinanceChecksQueriesAddResponseForm form,
                                            @SuppressWarnings("unused") BindingResult bindingResult,
                                            ValidationHandler validationHandler,
                                            @ModelAttribute("loggedInUser") UserResource loggedInUser,
                                            HttpServletRequest request,
                                            HttpServletResponse response) {
        Supplier<String> view = () -> { return "project/financecheck/new-response";};
        return validationHandler.performActionOrBindErrorsToField(FORM_ATTR, view, view, () -> {
            MultipartFile file = form.getAttachment();
            // TODO store file, get file ID
            List<Long> attachments = loadAttachmentsFromCookie(request, projectId, organisationId);
            attachments.add(Long.valueOf(attachments.size()));
            saveAttachmentsToCookie(response, attachments, projectId, organisationId);

            FinanceChecksQueriesAddResponseViewModel viewModel = populateQueriesViewModel(projectId, organisationId, querySection, attachments, queryId);
            model.addAttribute("model", viewModel);
            return ServiceResult.serviceSuccess();
        });
    }

    @PreAuthorize("hasPermission(#projectId, 'ACCESS_FINANCE_CHECKS_QUERIES_SECTION')")
    @RequestMapping(method = POST)
    public String saveResponse(Model model,
                               @PathVariable("projectId") final Long projectId,
                               @PathVariable Long organisationId,
                               @PathVariable Long queryId,
                               @RequestParam(value = "query_section", required = false) String querySection,
                               @Valid @ModelAttribute(FORM_ATTR) FinanceChecksQueriesAddResponseForm form,
                               @SuppressWarnings("unused") BindingResult bindingResult,
                               ValidationHandler validationHandler,
                               @ModelAttribute("loggedInUser") UserResource loggedInUser,
                               HttpServletRequest request)
    {
        Supplier<String> successView = () -> redirectToQueryPage(projectId, organisationId, querySection);
        Supplier<String> failureView = () -> {
            return "project/financecheck/new-response";
        };

        return validationHandler.performActionOrBindErrorsToField(FORM_ATTR, failureView, successView,
                () -> {
                    List<Long> attachments = loadAttachmentsFromCookie(request, projectId, organisationId);
                    //TODO save response
                    // get file entry IDs from session data
                    //attachments.forEach();
                    //validationHandler.addAnyErrors(result, toField(FORM_ATTR))
                    return ServiceResult.serviceSuccess();
                });
    }

    @PreAuthorize("hasPermission(#projectId, 'ACCESS_FINANCE_CHECKS_QUERIES_SECTION')")
    @RequestMapping(value="/cancel", method = GET)
    public String cancelNewForm(@PathVariable Long projectId,
                                @PathVariable Long organisationId,
                                @PathVariable Long queryId,
                                @RequestParam(value = "query_section", required = false) String querySection,
                                Model model,
                                @ModelAttribute("loggedInUser") UserResource loggedInUser,
                                HttpServletResponse response) {
        // TODO delete attachments
        cookieUtil.removeCookie(response, getCookieName(projectId, organisationId));

        return redirectToQueryPage(projectId, organisationId, querySection);
    }

    @PreAuthorize("hasPermission(#projectId, 'ACCESS_FINANCE_CHECKS_QUERIES_SECTION')")
    @RequestMapping(value = "/attachment/{attachmentId}", method = GET)
    public
    @ResponseBody
    ResponseEntity<ByteArrayResource> downloadAttachment(@PathVariable Long projectId,
                                                         @PathVariable Long organisationId,
                                                         @PathVariable Long queryId,
                                                         @PathVariable Long attachmentId,
                                                         @ModelAttribute("loggedInUser") UserResource loggedInUser,
                                                         HttpServletRequest request) {
        List<Long> attachments = loadAttachmentsFromCookie(request, projectId, organisationId);
        Optional<ByteArrayResource> content = Optional.empty();
        Optional<FileEntryResource> fileDetails = Optional.empty();

        if (attachments.contains(attachmentId)) {
            // TODO get file from service
        }
        return returnFileIfFoundOrThrowNotFoundException(content, fileDetails);
    }

    private FinanceChecksQueriesAddResponseViewModel populateQueriesViewModel(Long projectId, Long organisationId, String querySection, List<Long> attachmentFileIds, Long newPostQueryId) {

        ProjectResource project = projectService.getById(projectId);

        OrganisationResource organisation = organisationService.getOrganisationById(organisationId);
        OrganisationResource leadOrganisation = projectService.getLeadOrganisation(projectId);
        boolean leadPartnerOrganisation = leadOrganisation.getId().equals(organisation.getId());

        Optional<ProjectUserResource> financeContact = getFinanceContact(projectId, organisationId);

        // TODO lookup attachment details from service
        Map<Long, String> attachmentLinks = new HashMap<>();
        attachmentFileIds.forEach(id -> attachmentLinks.put(id, "file_"+id));

        return new FinanceChecksQueriesAddResponseViewModel(
                organisation.getName(),
                leadPartnerOrganisation,
                financeContact.isPresent() ? financeContact.get().getUserName() : UNKNOWN_FIELD,
                financeContact.isPresent() ? financeContact.get().getEmail() : UNKNOWN_FIELD,
                financeContact.isPresent() ? financeContact.get().getPhoneNumber() : UNKNOWN_FIELD,
                querySection == null ? UNKNOWN_FIELD : querySection,
                project.getId(),
                project.getName(),
                attachmentLinks,
                FinanceChecksQueriesFormConstraints.MAX_QUERY_WORDS,
                FinanceChecksQueriesFormConstraints.MAX_QUERY_CHARACTERS,
                newPostQueryId,
                organisationId,
                FINANCE_CHECKS_QUERIES_BASE_URL
        );
    }

    private Optional<ProjectUserResource> getFinanceContact(Long projectId, Long organisationId){
        List<ProjectUserResource> projectUsers = projectService.getProjectUsersForProject(projectId);
        return simpleFindFirst(projectUsers, pr -> pr.isFinanceContact() && organisationId.equals(pr.getOrganisation()));
    }

    private String redirectToQueryPage(Long projectId, Long organisationId, String querySection) {
        return "redirect:/project/" + projectId + "/finance-check/organisation/" + organisationId + "/query?query_section=" + querySection;
    }

    private ResponseEntity<ByteArrayResource> returnFileIfFoundOrThrowNotFoundException(Optional<ByteArrayResource> content, Optional<FileEntryResource> fileDetails) {
        if (content.isPresent() && fileDetails.isPresent()) {
            return getFileResponseEntity(content.get(), fileDetails.get());
        } else {
            return new ResponseEntity<>(null, null, HttpStatus.NO_CONTENT);
        }
    }

    private String getCookieName(Long projectId, Long organisationId) {
        return ATTACHMENT_COOKIE + "_" + projectId + "_" + organisationId;
    }

    private void saveAttachmentsToCookie(HttpServletResponse response, List<Long> attachmentFileIds, Long projectId, Long organisationId) {
        attachmentFileIds.add(projectId);
        attachmentFileIds.add(organisationId);
        String jsonState = JsonUtil.getSerializedObject(attachmentFileIds);
        cookieUtil.saveToCookie(response, getCookieName(projectId,organisationId), jsonState);
    }

    private List<Long> loadAttachmentsFromCookie(HttpServletRequest request, Long projectId, Long organisationId) {

        List<Long> attachments = new LinkedList<>();
        String json = cookieUtil.getCookieValue(request, getCookieName(projectId, organisationId));

        if (json != null && !"".equals(json)) {
            TypeReference<List<Long>> listType = new TypeReference<List<Long>>() {};
            ObjectMapper mapper = new ObjectMapper();
            try {
                attachments = mapper.readValue(json, listType);
                return attachments;
            } catch (IOException e) {
                //ignored
            }
        }
        return attachments;
    }
}
