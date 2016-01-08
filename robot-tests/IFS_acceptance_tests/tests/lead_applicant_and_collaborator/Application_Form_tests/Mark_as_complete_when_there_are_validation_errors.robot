*** Settings ***
Documentation     INFUND-406: As an applicant, and on the application form I have validation error, I cannot mark questions or sections as complete in order to submit my application
Suite Setup       Login as User    &{lead_applicant_credentials}
Suite Teardown    TestTeardown User closes the browser
Resource          ../../../resources/GLOBAL_LIBRARIES.robot
Resource          ../../../resources/variables/GLOBAL_VARIABLES.robot
Resource          ../../../resources/variables/User_credentials.robot
Resource          ../../../resources/keywords/Login_actions.robot
Resource          ../../../resources/keywords/Applicant_actions.robot

*** Variables ***

*** Test Cases ***
Verify that the user can't mark as complete empty text areas
    [Documentation]    -INFUND-406
    [Tags]    Applicant    Validations
    Given Applicant goes to the Project summary of the new application
    When the "Project Summary" question is empty
    and the applicant marks the public description question as complete
    Then the applicant should get a warning to enter data in the "Project Summary" question
    And the applicant should get an alert with the description of the error

Verify that the user doesn't get the error when the text area is not empty anymore
    [Documentation]    -INFUND-406
    [Tags]    Applicant    Validations
    Given Applicant goes to the Project summary of the new application
    When the "Project Summary" question is empty
    and the applicant marks the public description question as complete
    and the applicant inserts some text again in the "Project Summary" question
    Then applicant should be able to mark the question as complete
    And the applicant can click edit to make the section editable again

*** Keywords ***
the "Project Summary" question is empty
    Clear Element Text    css=#form-input-11 .editor
    Press Key    css=#form-input-11 .editor    \\8
    #Click Element    css=.bold_button

the applicant marks the public description question as complete
    Click Element    css=#form-input-11 .buttonlink[name="mark_as_complete"]

the applicant should get an alert with the description of the error
    Wait Until Element Is Visible    css=.error-summary li

the applicant should get a warning to enter data in the "Project Summary" question
    Wait Until Element Is Visible    css=#form-input-11 .error-message

the applicant inserts some text again in the "Project Summary" question
    Input Text    css=#form-input-11 .editor    test if the applicant can mark the question as complete
    Click Element    css=.bold_button

applicant should be able to mark the question as complete
    Click Element    css=#form-input-11 .buttonlink[name="mark_as_complete"]
    Wait Until Element Is Not Visible    css=#form-input-11 .error-message
    Wait Until Element Is Not Visible    css=.error-summary li

Applicant goes to the Project summary of the new application
    #go to    ${NEW_TEST_APPLICATION_PROJECT_SUMMARY}
    go to    ${PROJECT_SUMMARY_URL}

the applicant can click edit to make the section editable again
    Click Element      name=mark_as_incomplete