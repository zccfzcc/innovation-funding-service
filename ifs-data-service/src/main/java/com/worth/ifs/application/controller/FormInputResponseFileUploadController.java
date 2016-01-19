package com.worth.ifs.application.controller;

import com.worth.ifs.application.resource.FormInputResponseFileEntryId;
import com.worth.ifs.application.resource.FormInputResponseFileEntryResource;
import com.worth.ifs.application.transactional.ApplicationService;
import com.worth.ifs.commons.controller.ServiceFailureToJsonResponseHandler;
import com.worth.ifs.commons.controller.SimpleServiceFailureToJsonResponseHandler;
import com.worth.ifs.file.resource.FileEntryResource;
import com.worth.ifs.transactional.ServiceResult;
import com.worth.ifs.util.Either;
import com.worth.ifs.util.JsonStatusResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Supplier;

import static com.worth.ifs.application.controller.FormInputResponseFileEntryJsonStatusResponse.fileEntryCreated;
import static com.worth.ifs.application.controller.FormInputResponseFileEntryJsonStatusResponse.fileEntryUpdated;
import static com.worth.ifs.application.transactional.ApplicationServiceImpl.ServiceFailures.FILE_ALREADY_LINKED_TO_FORM_INPUT_RESPONSE;
import static com.worth.ifs.application.transactional.ApplicationServiceImpl.ServiceFailures.UNABLE_TO_FIND_FILE;
import static com.worth.ifs.commons.controller.ControllerErrorHandlingUtil.*;
import static com.worth.ifs.file.transactional.FileServiceImpl.ServiceFailures.*;
import static com.worth.ifs.transactional.BaseTransactionalService.Failures.*;
import static com.worth.ifs.util.Either.left;
import static com.worth.ifs.util.Either.right;
import static com.worth.ifs.util.JsonStatusResponse.*;
import static com.worth.ifs.util.ParsingFunctions.validLong;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 *
 */
@RestController
@RequestMapping("/forminputresponse")
public class FormInputResponseFileUploadController {

    private static final Log LOG = LogFactory.getLog(FormInputResponseFileUploadController.class);

    @Value("${ifs.data.service.file.storage.fileinputresponse.max.filesize.bytes}")
    private Long maxFilesizeBytes;

    @Value("${ifs.data.service.file.storage.fileinputresponse.valid.media.types}")
    private List<String> validMediaTypes;

    @Autowired
    private ApplicationService applicationService;

    private List<ServiceFailureToJsonResponseHandler> serviceFailureHandlers = asList(

            new SimpleServiceFailureToJsonResponseHandler(singletonList(UNABLE_TO_FIND_FILE), (serviceFailure, response) -> notFound("Unable to find file", response)),
            new SimpleServiceFailureToJsonResponseHandler(singletonList(FORM_INPUT_NOT_FOUND), (serviceFailure, response) -> notFound("Unable to find Form Input", response)),
            new SimpleServiceFailureToJsonResponseHandler(singletonList(APPLICATION_NOT_FOUND), (serviceFailure, response) -> notFound("Unable to find Application", response)),
            new SimpleServiceFailureToJsonResponseHandler(singletonList(PROCESS_ROLE_NOT_FOUND), (serviceFailure, response) -> notFound("Unable to find Process Role", response)),
            new SimpleServiceFailureToJsonResponseHandler(singletonList(FORM_INPUT_RESPONSE_NOT_FOUND), (serviceFailure, response) -> notFound("Unable to find Form Input Response", response)),
            new SimpleServiceFailureToJsonResponseHandler(singletonList(INCORRECTLY_REPORTED_FILESIZE), (serviceFailure, response) -> badRequest("Incorrectly reported filesize", response)),
            new SimpleServiceFailureToJsonResponseHandler(singletonList(INCORRECTLY_REPORTED_MEDIA_TYPE), (serviceFailure, response) -> unsupportedMediaType("Incorrectly reported Content Type", response)),
            new SimpleServiceFailureToJsonResponseHandler(singletonList(DUPLICATE_FILE_CREATED), (serviceFailure, response) -> conflict("File already exists", response)),
            new SimpleServiceFailureToJsonResponseHandler(singletonList(FILE_ALREADY_LINKED_TO_FORM_INPUT_RESPONSE), (serviceFailure, response) -> conflict("File already linked to Form Input Response", response))
    );

    @RequestMapping(value = "/file", method = POST, produces = "application/json")
    public JsonStatusResponse createFile(
            @RequestHeader(value = "Content-Type", required = false) String contentType,
            @RequestHeader(value = "Content-Length", required = false) String contentLength,
            @RequestParam("formInputId") long formInputId,
            @RequestParam("applicationId") long applicationId,
            @RequestParam("processRoleId") long processRoleId,
            @RequestParam(value = "filename", required = false) String originalFilename,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        Supplier<JsonStatusResponse> internalServerError = () -> internalServerError("Error creating file", response);

        return handlingErrors(internalServerError, () -> validContentLengthHeader(contentLength, response).
            map(lengthFromHeader -> validContentTypeHeader(contentType, response).
            map(typeFromHeader -> validFilename(originalFilename, response).
            map(filenameParameter -> validContentLength(lengthFromHeader, response).
            map(validLength -> validMediaType(typeFromHeader, response).
            map(validType -> createFormInputResponseFile(validType, lengthFromHeader, originalFilename, formInputId, applicationId, processRoleId, request, response).
            map(fileEntry -> returnFileEntryCreatedResponse(fileEntry, response)

        ))))))).mapLeftOrRight(failure -> failure, success -> success);
    }

    @RequestMapping(value = "/file", method = PUT, produces = "application/json")
    public JsonStatusResponse updateFile(
            @RequestHeader(value = "Content-Type", required = false) String contentType,
            @RequestHeader(value = "Content-Length", required = false) String contentLength,
            @RequestParam("formInputId") long formInputId,
            @RequestParam("applicationId") long applicationId,
            @RequestParam("processRoleId") long processRoleId,
            @RequestParam(value = "filename", required = false) String originalFilename,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        Supplier<JsonStatusResponse> internalServerError = () -> internalServerError("Error updating file", response);

        return handlingErrors(internalServerError, () -> validContentLengthHeader(contentLength, response).
            map(lengthFromHeader -> validContentTypeHeader(contentType, response).
            map(typeFromHeader -> validFilename(originalFilename, response).
            map(filenameParameter -> validContentLength(lengthFromHeader, response).
            map(validLength -> validMediaType(typeFromHeader, response).
            map(validType -> updateFormInputResponseFile(validType, lengthFromHeader, originalFilename, formInputId, applicationId, processRoleId, request, response).
            map(fileEntry -> returnFileEntryUpdatedResponse(fileEntry, response)
        ))))))).mapLeftOrRight(failure -> failure, success -> success);
    }

    @RequestMapping(value = "/file", method = GET)
    public @ResponseBody ResponseEntity<?> getFileContents(
            @RequestParam("formInputId") long formInputId,
            @RequestParam("applicationId") long applicationId,
            @RequestParam("processRoleId") long processRoleId,
            HttpServletResponse response) throws IOException {

        Supplier<JsonStatusResponse> internalServerError = () -> internalServerError("Error retrieving file", response);

        return handlingErrorsWithResponseEntity(internalServerError, () -> {

            Either<JsonStatusResponse, Pair<FormInputResponseFileEntryResource, Supplier<InputStream>>> result =
                    doGetFile(formInputId, applicationId, processRoleId, response);

            return result.mapLeftOrRight(
                    failure ->
                        Either. <ResponseEntity<JsonStatusResponse>, ResponseEntity<?>> left(new ResponseEntity<>(failure, failure.getStatus())),
                    success -> {
                        FormInputResponseFileEntryResource fileEntry = success.getKey();
                        InputStream inputStream = success.getValue().get();
                        InputStreamResource inputStreamResource = new InputStreamResource(inputStream);
                        HttpHeaders httpHeaders = new HttpHeaders();
                        httpHeaders.setContentLength(fileEntry.getFileEntryResource().getFilesizeBytes());
                        httpHeaders.setContentType(MediaType.parseMediaType(fileEntry.getFileEntryResource().getMediaType()));
                        return Either. <ResponseEntity<JsonStatusResponse>, ResponseEntity<?>> right(new ResponseEntity<>(inputStreamResource, httpHeaders, HttpStatus.OK));
                    }
            );

        }).mapLeftOrRight(failure -> failure, success -> success);
    }

    @RequestMapping(value = "/fileentry", method = GET, produces = "application/json")
    public @ResponseBody ResponseEntity<?> getFileEntryDetails(
            @RequestParam("formInputId") long formInputId,
            @RequestParam("applicationId") long applicationId,
            @RequestParam("processRoleId") long processRoleId,
            HttpServletResponse response) throws IOException {

        Supplier<JsonStatusResponse> internalServerError = () -> internalServerError("Error retrieving file", response);

        return handlingErrorsWithResponseEntity(internalServerError, () -> {

            Either<JsonStatusResponse, Pair<FormInputResponseFileEntryResource, Supplier<InputStream>>> result =
                    doGetFile(formInputId, applicationId, processRoleId, response);

            return result.mapLeftOrRight(
                    failure ->
                            Either. <ResponseEntity<JsonStatusResponse>, ResponseEntity<?>> left(new ResponseEntity<>(failure, failure.getStatus())),
                    success -> {
                        FormInputResponseFileEntryResource fileEntry = success.getKey();
                        return Either. <ResponseEntity<JsonStatusResponse>, ResponseEntity<?>> right(new ResponseEntity<>(fileEntry, HttpStatus.OK));
                    }
            );

        }).mapLeftOrRight(failure -> failure, success -> success);
    }

    @RequestMapping(value = "/file", method = DELETE, produces = "application/json")
    public @ResponseBody JsonStatusResponse deleteFileEntry(
            @RequestParam("formInputId") long formInputId,
            @RequestParam("applicationId") long applicationId,
            @RequestParam("processRoleId") long processRoleId,
            HttpServletResponse response) throws IOException {

        Supplier<JsonStatusResponse> internalServerError = () -> internalServerError("Error deleting file", response);

        return handlingErrors(internalServerError, () -> {

            FormInputResponseFileEntryId compoundId = new FormInputResponseFileEntryId(formInputId, applicationId, processRoleId);

            return applicationService.deleteFormInputResponseFileUpload(compoundId).mapLeftOrRight(
                    failure -> Either.<JsonStatusResponse, JsonStatusResponse> left(handleServiceFailure(failure, serviceFailureHandlers, response).orElseGet(internalServerError)),
                    success -> Either.<JsonStatusResponse, JsonStatusResponse> right(noContent("File deleted successfully", response)));

        }).mapLeftOrRight(failure -> failure, success -> success);
    }

    private Either<JsonStatusResponse, Pair<FormInputResponseFileEntryResource, Supplier<InputStream>>> doGetFile(long formInputId, long applicationId, long processRoleId, HttpServletResponse response) {

        FormInputResponseFileEntryId formInputResponseFileEntryId = new FormInputResponseFileEntryId(formInputId, applicationId, processRoleId);

        ServiceResult<Pair<FormInputResponseFileEntryResource, Supplier<InputStream>>> file =
                applicationService.getFormInputResponseFileUpload(formInputResponseFileEntryId);

        return file.mapLeftOrRight(
                failure ->
                        Either. <JsonStatusResponse, Pair<FormInputResponseFileEntryResource, Supplier<InputStream>>> left(handleServiceFailure(failure, serviceFailureHandlers, response).orElseGet(() -> internalServerError("Error retrieving file", response))),
                success ->
                        Either. <JsonStatusResponse, Pair<FormInputResponseFileEntryResource, Supplier<InputStream>>> right(success)
        );
    }

    private Either<JsonStatusResponse, JsonStatusResponse> returnFileEntryCreatedResponse(FormInputResponseFileEntryResource fileEntry, HttpServletResponse response) {
        return right(fileEntryCreated(fileEntry.getFileEntryResource().getId(), response));
    }

    private Either<JsonStatusResponse, JsonStatusResponse> returnFileEntryUpdatedResponse(FormInputResponseFileEntryResource fileEntry, HttpServletResponse response) {
        return right(fileEntryUpdated(fileEntry.getFileEntryResource().getId(), response));
    }

    private Either<JsonStatusResponse, FormInputResponseFileEntryResource> createFormInputResponseFile(MediaType mediaType, long length, String originalFilename, long formInputId, long applicationId, long processRoleId, HttpServletRequest request, HttpServletResponse response) {

        LOG.debug("Creating file with filename - " + originalFilename + "; Content Type - " + mediaType + "; Content Length - " + length);

        FileEntryResource fileEntry = new FileEntryResource(null, originalFilename, mediaType, length);
        FormInputResponseFileEntryResource formInputResponseFile = new FormInputResponseFileEntryResource(fileEntry, formInputId, applicationId, processRoleId);
        ServiceResult<Pair<File, FormInputResponseFileEntryResource>> creationResult = applicationService.createFormInputResponseFileUpload(formInputResponseFile, inputStreamSupplier(request));

        return creationResult.mapLeftOrRight(
                failure ->
                        Either. <JsonStatusResponse, FormInputResponseFileEntryResource> left(handleServiceFailure(failure, serviceFailureHandlers, response).orElseGet(() -> internalServerError("Error creating file", response))),
                success ->
                        Either. <JsonStatusResponse, FormInputResponseFileEntryResource> right(success.getValue()));
    }

    private Either<JsonStatusResponse, FormInputResponseFileEntryResource> updateFormInputResponseFile(MediaType mediaType, long length, String originalFilename, long formInputId, long applicationId, long processRoleId, HttpServletRequest request, HttpServletResponse response) {

        LOG.debug("Updating file with filename - " + originalFilename + "; Content Type - " + mediaType + "; Content Length - " + length);

        FileEntryResource fileEntry = new FileEntryResource(null, originalFilename, mediaType, length);
        FormInputResponseFileEntryResource formInputResponseFile = new FormInputResponseFileEntryResource(fileEntry, formInputId, applicationId, processRoleId);
        ServiceResult<Pair<File, FormInputResponseFileEntryResource>> creationResult = applicationService.updateFormInputResponseFileUpload(formInputResponseFile, inputStreamSupplier(request));

        return creationResult.mapLeftOrRight(
                failure ->
                        Either. <JsonStatusResponse, FormInputResponseFileEntryResource> left(handleServiceFailure(failure, serviceFailureHandlers, response).orElseGet(() -> internalServerError("Error updating file", response))),
                success ->
                        Either. <JsonStatusResponse, FormInputResponseFileEntryResource> right(success.getValue()));
    }

    private Supplier<InputStream> inputStreamSupplier(HttpServletRequest request) {
        return () -> {
            try {
                return request.getInputStream();
            } catch (IOException e) {
                LOG.error("Unable to open an input stream from request", e);
                throw new RuntimeException("Unable to open an input stream from request", e);
            }
        };
    }

    private Either<JsonStatusResponse, Long> validContentLengthHeader(String contentLengthHeader, HttpServletResponse response) {

        return validLong(contentLengthHeader).map(length -> Either.<JsonStatusResponse, Long> right(length)).
                orElseGet(() -> left(lengthRequired("Please supply a valid Content-Length HTTP header.  Maximum " + maxFilesizeBytes, response)));
    }

    private Either<JsonStatusResponse, String> validContentTypeHeader(String contentTypeHeader, HttpServletResponse response) {
        return !StringUtils.isBlank(contentTypeHeader) ? right(contentTypeHeader) :
                left(unsupportedMediaType("Please supply a valid Content-Type HTTP header.  Valid types are " + validMediaTypes.stream().collect(joining(", ")), response));
    }

    private Either<JsonStatusResponse, Long> validContentLength(long length, HttpServletResponse response) {
        if (length > maxFilesizeBytes) {
            return left(payloadTooLarge("File upload was too large for FormInputResponse.  Max filesize in bytes is " + maxFilesizeBytes, response));
        }
        return right(length);
    }

    private Either<JsonStatusResponse, String> validFilename(String filename, HttpServletResponse response) {
        return checkParameterIsPresent(filename, "Please supply an original filename as a \"filename\" HTTP Request Parameter", response);
    }

    private Either<JsonStatusResponse, MediaType> validMediaType(String contentType, HttpServletResponse response) {
        if (!validMediaTypes.contains(contentType)) {
            return left(unsupportedMediaType("Please supply a valid Content-Type HTTP header.  Valid types are " + validMediaTypes.stream().collect(joining(", ")), response));
        }
        return right(MediaType.valueOf(contentType));
    }

    private Either<JsonStatusResponse, String> checkParameterIsPresent(String parameterValue, String failureMessage, HttpServletResponse response) {
        return !StringUtils.isBlank(parameterValue) ? right(parameterValue) : left(badRequest(failureMessage, response));
    }

    void setMaxFilesizeBytes(Long maxFilesizeBytes) {
        this.maxFilesizeBytes = maxFilesizeBytes;
    }

    void setValidMediaTypes(List<String> validMediaTypes) {
        this.validMediaTypes = validMediaTypes;
    }
}
