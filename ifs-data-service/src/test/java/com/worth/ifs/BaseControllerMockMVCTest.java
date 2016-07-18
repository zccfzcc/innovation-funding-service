package com.worth.ifs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.worth.ifs.commons.error.Error;
import com.worth.ifs.commons.rest.RestErrorResponse;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.file.resource.FileEntryResource;
import com.worth.ifs.file.service.BasicFileAndContents;
import com.worth.ifs.file.service.FileAndContents;
import com.worth.ifs.file.transactional.FileHeaderAttributes;
import com.worth.ifs.rest.ErrorControllerAdvice;
import com.worth.ifs.rest.RestResultHandlingHttpMessageConverter;
import org.junit.Before;
import org.junit.Rule;
import org.mockito.InjectMocks;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.restdocs.RestDocumentation;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.worth.ifs.BaseBuilderAmendFunctions.id;
import static com.worth.ifs.InputStreamTestUtil.assertInputStreamContents;
import static com.worth.ifs.JsonTestUtil.toJson;
import static com.worth.ifs.LambdaMatcher.createLambdaMatcher;
import static com.worth.ifs.commons.service.ServiceResult.serviceSuccess;
import static com.worth.ifs.file.resource.builders.FileEntryResourceBuilder.newFileEntryResource;
import static com.worth.ifs.util.CollectionFunctions.combineLists;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * This is the base class for testing Controllers using MockMVC in addition to standard Mockito mocks.  Using MockMVC
 * allows Controllers to be tested via their routes and their responses' HTTP responses tested also.
 *
 * Created by dwatson on 02/10/15.
 */
public abstract class BaseControllerMockMVCTest<ControllerType> extends BaseUnitTestMocksTest {

    @InjectMocks
    protected ControllerType controller = supplyControllerUnderTest();

    protected MockMvc mockMvc;

    protected abstract ControllerType supplyControllerUnderTest();

    @Rule
    public final RestDocumentation restDocumentation = new RestDocumentation("build/generated-snippets");// having to "fake" the request body as JSON because Spring Restdocs does not support other content types other

    // than JSON and XML
    private final String dummyFileContent = "{\"description\":\"The request body is the binary content of the file being uploaded - it is NOT JSON as seen here!\"}";

    @Before
    public void setupMockMvc() {

        MockMvc originalMvc = MockMvcBuilders.standaloneSetup(controller).build();

        //
        // We need to register custom MessageConverters with MockMVC manually.  Unfortunately there's no way to simply add a new
        // one to the custom set of MessageConverters that comes out of the box.  Therefore we need to get the original set as
        // a list, add our custom one to the list, and then set this list as the full set of MessageConverters
        //
        List<HandlerAdapter> defaultHandlerAdapters = (List<HandlerAdapter>) getField(getField(originalMvc, "servlet"), "handlerAdapters");
        RequestMappingHandlerAdapter requestMappingHandlerAdapter = (RequestMappingHandlerAdapter) defaultHandlerAdapters.stream().filter(handler -> handler instanceof RequestMappingHandlerAdapter).collect(toList()).get(0);
        List<HttpMessageConverter<?>> originalMessageConverters = requestMappingHandlerAdapter.getMessageConverters();
        List<HttpMessageConverter<?>> nonMappingJackson2Converters = originalMessageConverters.stream().filter(converter -> !(converter instanceof MappingJackson2HttpMessageConverter)).collect(toList());

        RestResultHandlingHttpMessageConverter customHttpMessageConverter = new RestResultHandlingHttpMessageConverter();
        List<HttpMessageConverter<?>> newListOfConverters = combineLists(nonMappingJackson2Converters, customHttpMessageConverter);
        HttpMessageConverter[] newListOfConvertersArray = newListOfConverters.toArray(new HttpMessageConverter[newListOfConverters.size()]);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(newListOfConvertersArray)
                .setControllerAdvice(new ErrorControllerAdvice())
                .apply(documentationConfiguration(this.restDocumentation)
                        .uris()
                        .withScheme("http")
                        .withHost("localhost")
                        .withPort(8090))
                .build();
    }

    private static ResultMatcher contentObject(final Object json) throws JsonProcessingException {
        return content().string(new ObjectMapper().writeValueAsString(json));
    }

    protected static ResultMatcher contentError(final Error error) throws JsonProcessingException {
        return contentObject(new RestErrorResponse(error));
    }

    protected void assertResponseErrorMessageEqual(String expectedMessage, Error expectedError, MvcResult mvcResult) throws IOException {
        String content = mvcResult.getResponse().getContentAsString();
        RestErrorResponse restErrorResponse = new ObjectMapper().readValue(content, RestErrorResponse.class);
        assertErrorMessageEqual(expectedMessage, expectedError, restErrorResponse);
    }

    private void assertErrorMessageEqual(String expectedMessage, Error expectedError, RestErrorResponse restErrorResponse) {
        assertEquals(expectedMessage, restErrorResponse.getErrors().get(0).getErrorMessage());
        assertEqualsUpNoIncludingStatusCode(restErrorResponse, expectedError);
    }

    protected void assertEqualsUpNoIncludingStatusCode(final RestErrorResponse restErrorResponse, final Error expectedError){
        assertTrue(restErrorResponse.getErrors().size() == 1);
        assertEquals(restErrorResponse.getErrors().get(0).getErrorMessage(), expectedError.getErrorMessage());
        assertEquals(restErrorResponse.getErrors().get(0).getArguments() , expectedError.getArguments());
        assertEquals(restErrorResponse.getErrors().get(0).getErrorKey() , expectedError.getErrorKey());
    }

    /**
     * A useful shorthand way to test file upload endpoints that take a file, call a service to create the file on the filesystem,
     * and return the new FileEntryResource as a JSON POST response.  Essentially this method:
     *
     * 1) Constructs a new POST request with some file details on it
     * 2) Sets expectations on a fileValidator validating the file being uploaded
     * 3) Lets the calling code supply a function that sets expectations on the "create file" service method that will be
     *    invoked by the Controller
     * 4) Invokes the POST request
     * 5) Verifies the fileValidator was called as expected
     * 6) Verifies the "create file" service method was called as expected
     */
    protected <T> ResultActions assertFileUploadProcess(
            String url,
            T serviceToCall,
            BiFunction<T, FileEntryResource, ServiceResult<FileEntryResource>> createFileServiceCall)
            throws Exception {

        return assertFileUploadProcess(url, new Object[] {}, emptyMap(), serviceToCall, createFileServiceCall);
    }

    protected <T> ResultActions assertFileUpdateProcess(
            String url,
            T serviceToCall,
            BiFunction<T, FileEntryResource, ServiceResult<Void>> createFileServiceCall)
            throws Exception {

        return assertFileUpdateProcess(url, new Object[] {}, emptyMap(), serviceToCall, createFileServiceCall);
    }

    /**
     * A useful shorthand way to test file upload endpoints that take a file, call a service to create the file on the filesystem,
     * and return the new FileEntryResource as a JSON POST response.  Essentially this method:
     *
     * 1) Constructs a new POST request with some file details on it
     * 2) Sets expectations on a fileValidator validating the file being uploaded
     * 3) Lets the calling code supply a function that sets expectations on the "create file" service method that will be
     *    invoked by the Controller
     * 4) Invokes the POST request
     * 5) Verifies the fileValidator was called as expected
     * 6) Verifies the "create file" service method was called as expected
     */
    protected <T> ResultActions assertFileUpdateProcess(
            String url,
            Object[] urlParams,
            Map<String, String> requestParams,
            T serviceToCall,
            BiFunction<T, FileEntryResource, ServiceResult<Void>> updateFileServiceCall) throws Exception {



        FileEntryResource expectedTemporaryFile = newFileEntryResource().
                with(id(null)).
                withFilesizeBytes(dummyFileContent.length()).
                withName("filename.txt").
                withMediaType("text/plain").
                build();

        FileHeaderAttributes fileAttributes = new FileHeaderAttributes(TEXT_PLAIN, dummyFileContent.length(), "filename.txt");
        when(fileValidatorMock.validateFileHeaders("text/plain", dummyFileContent.length() + "", "filename.txt")).thenReturn(serviceSuccess(fileAttributes));

        when(updateFileServiceCall.apply(serviceToCall, expectedTemporaryFile)).
                thenReturn(serviceSuccess());

        MockHttpServletRequestBuilder mainRequest = put(url, urlParams).
                content(dummyFileContent).
                param("filename", "filename.txt").
                header("Content-Type", "text/plain").
                header("Content-Length", dummyFileContent.length() + "").
                header("IFS_AUTH_TOKEN", "123abc");

        requestParams.forEach(mainRequest::param);

        ResultActions resultActions = mockMvc.perform(mainRequest).
                andExpect(content().string("")).
                andExpect(status().isOk());

        verify(fileValidatorMock).validateFileHeaders("text/plain", dummyFileContent.length() + "", "filename.txt");
        updateFileServiceCall.apply(verify(serviceToCall), expectedTemporaryFile);

        return resultActions;
    }

    /**
     * A useful shorthand way to test file upload endpoints that take a file, call a service to create the file on the filesystem,
     * and return the new FileEntryResource as a JSON POST response.  Essentially this method:
     *
     * 1) Constructs a new POST request with some file details on it
     * 2) Sets expectations on a fileValidator validating the file being uploaded
     * 3) Lets the calling code supply a function that sets expectations on the "create file" service method that will be
     *    invoked by the Controller
     * 4) Invokes the POST request
     * 5) Verifies the fileValidator was called as expected
     * 6) Verifies the "create file" service method was called as expected
     */
    protected <T> ResultActions assertFileUploadProcess(
        String url,
        Object[] urlParams,
        Map<String, String> requestParams,
        T serviceToCall,
        BiFunction<T, FileEntryResource, ServiceResult<FileEntryResource>> createFileServiceCall) throws Exception {

        FileEntryResource expectedTemporaryFile = newFileEntryResource().
                with(id(null)).
                withFilesizeBytes(dummyFileContent.length()).
                withName("filename.txt").
                withMediaType("text/plain").
                build();

        FileHeaderAttributes fileAttributes = new FileHeaderAttributes(TEXT_PLAIN, dummyFileContent.length(), "filename.txt");
        when(fileValidatorMock.validateFileHeaders("text/plain", dummyFileContent.length() + "", "filename.txt")).thenReturn(serviceSuccess(fileAttributes));

        FileEntryResource savedFile = newFileEntryResource().with(id(456L)).build();
        when(createFileServiceCall.apply(serviceToCall, expectedTemporaryFile)).
                thenReturn(serviceSuccess(savedFile));

        MockHttpServletRequestBuilder mainRequest = post(url, urlParams).
            content(dummyFileContent).
            param("filename", "filename.txt").
            header("Content-Type", "text/plain").
            header("Content-Length", dummyFileContent.length() + "").
            header("IFS_AUTH_TOKEN", "123abc");

        requestParams.forEach(mainRequest::param);

        ResultActions resultActions = mockMvc.perform(mainRequest).
                andExpect(content().json(toJson(savedFile))).
                andExpect(status().isCreated());

        verify(fileValidatorMock).validateFileHeaders("text/plain", dummyFileContent.length() + "", "filename.txt");
        createFileServiceCall.apply(verify(serviceToCall), expectedTemporaryFile);

        return resultActions;
    }

    protected <T> ResultActions assertGetFileDetails(String url, Object[] urlParams, Map<String, String> requestParams, T serviceToCall, BiFunction<T, FileEntryResource, ServiceResult<FileEntryResource>> getFileFn) throws Exception {

        FileEntryResource fileToReturn = newFileEntryResource().build();

        when(getFileFn.apply(serviceToCall, fileToReturn)).thenReturn(serviceSuccess(fileToReturn));

        MockHttpServletRequestBuilder mainRequest = get(url, urlParams).
                header("IFS_AUTH_TOKEN", "123abc");
        requestParams.forEach(mainRequest::param);

        ResultActions resultActions = mockMvc.perform(mainRequest).
                andExpect(status().isOk()).
                andExpect(content().json(toJson(fileToReturn)));

        getFileFn.apply(verify(serviceToCall), fileToReturn);

        return resultActions;
    }

    protected <T> ResultActions assertDeleteFileDetails(String url, Object[] urlParams, Map<String, String> requestParams, T serviceToCall, Function<T, ServiceResult<Void>> deleteFileFn) throws Exception {

        when(deleteFileFn.apply(serviceToCall)).thenReturn(serviceSuccess());

        MockHttpServletRequestBuilder mainRequest = delete(url, urlParams).
                header("IFS_AUTH_TOKEN", "123abc");

        requestParams.forEach(mainRequest::param);

        ResultActions resultActions = mockMvc.perform(mainRequest).
                andExpect(status().isNoContent()).
                andExpect(content().string(""));

        deleteFileFn.apply(verify(serviceToCall));

        return resultActions;
    }

    protected <T> ResultActions assertGetFileContents(String url, Object[] urlParams, Map<String, String> requestParams, T serviceToCall, BiFunction<T, FileEntryResource, ServiceResult<FileAndContents>> getFileFn) throws Exception {

        FileEntryResource expectedFileEntryResource = newFileEntryResource().build();

        Supplier<InputStream> inputStreamSupplier = () -> new ByteArrayInputStream("The returned binary file data".getBytes());

        FileAndContents getResult = new BasicFileAndContents(expectedFileEntryResource, inputStreamSupplier);
        when(getFileFn.apply(serviceToCall, expectedFileEntryResource)).thenReturn(serviceSuccess(getResult));

        MockHttpServletRequestBuilder mainRequest = get(url, urlParams).
                header("IFS_AUTH_TOKEN", "123abc");
        requestParams.forEach(mainRequest::param);

        ResultActions resultActions = mockMvc.perform(mainRequest).
                andExpect(status().isOk());

        MvcResult result = resultActions.andReturn();

        assertEquals("The returned binary file data", result.getResponse().getContentAsString());

        return resultActions;
    }

    protected Supplier<InputStream> fileUploadInputStreamExpectations(String expectedContent) {
        return createLambdaMatcher(is -> {
            assertInputStreamContents(is.get(), expectedContent);
        });
    }

    protected Supplier<InputStream> fileUploadInputStreamExpectations() {
        return fileUploadInputStreamExpectations(dummyFileContent);
    }
}