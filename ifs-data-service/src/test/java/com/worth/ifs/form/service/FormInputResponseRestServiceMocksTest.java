package com.worth.ifs.form.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.worth.ifs.BaseRestServiceUnitTest;
import com.worth.ifs.form.domain.FormInputResponse;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static com.worth.ifs.form.builder.FormInputResponseBuilder.newFormInputResponse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

/**
 *
 */
public class FormInputResponseRestServiceMocksTest extends BaseRestServiceUnitTest<FormInputResponseRestServiceImpl> {

    private static final String formInputResponseRestURL = "/forminputresponses";

    @Override
    protected FormInputResponseRestServiceImpl registerRestServiceUnderTest() {
        FormInputResponseRestServiceImpl formInputResponseService = new FormInputResponseRestServiceImpl();
        formInputResponseService.formInputResponseRestURL = formInputResponseRestURL;
        return formInputResponseService;
    }

    @Test
    public void test_getResponsesByApplicationId() {
        String expectedUrl = dataServicesUrl + formInputResponseRestURL + "/findResponsesByApplication/123";
        FormInputResponse[] returnedResponses = newFormInputResponse().buildArray(3, FormInputResponse.class);
        ResponseEntity<FormInputResponse[]> returnedEntity = new ResponseEntity<>(returnedResponses, OK);

        when(mockRestTemplate.exchange(expectedUrl, GET, httpEntityForRestCall(), FormInputResponse[].class)).thenReturn(returnedEntity);

        List<FormInputResponse> responses = service.getResponsesByApplicationId(123L);
        assertNotNull(responses);
    }

    @Test
    public void test_saveQuestionResponse() {
        String expectedUrl = dataServicesUrl + formInputResponseRestURL + "/saveQuestionResponse/";

        ObjectNode expectedEntity = new ObjectMapper().createObjectNode().
                put("userId", 123L).put("applicationId", 456L).
                put("formInputId", 789L).put("value", "Very good answer!");

        String[] returnedResponses = new String[] {"A returned string"};
        ResponseEntity<String[]> returnedEntity = new ResponseEntity<>(returnedResponses, OK);

        when(mockRestTemplate.postForEntity(expectedUrl, httpEntityForRestCall(expectedEntity), String[].class)).thenReturn(returnedEntity);

        List<String> responses = service.saveQuestionResponse(123L, 456L, 789L, "Very good answer!");
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("A returned string", responses.get(0));
    }
}
