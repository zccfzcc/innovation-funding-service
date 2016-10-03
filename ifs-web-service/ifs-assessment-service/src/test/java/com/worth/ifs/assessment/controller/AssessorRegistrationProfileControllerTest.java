package com.worth.ifs.assessment.controller;

import com.worth.ifs.BaseControllerMockMVCTest;
import com.worth.ifs.assessment.model.AssessorRegistrationSkillsModelPopulator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.context.TestPropertySource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(MockitoJUnitRunner.class)
@TestPropertySource(locations = "classpath:application.properties")
public class AssessorRegistrationProfileControllerTest extends BaseControllerMockMVCTest<AssessorRegistrationProfileController> {

    @Spy
    @InjectMocks
    private AssessorRegistrationSkillsModelPopulator assessorRegistrationSkillsModelPopulator;

    @Override
    protected AssessorRegistrationProfileController supplyControllerUnderTest() {
        return new AssessorRegistrationProfileController();
    }

    @Test
    public void profileSkills() throws Exception {
        mockMvc.perform(get("/registration/skills"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("model"))
                .andExpect(view().name("registration/innovation-areas"));
    }
}
