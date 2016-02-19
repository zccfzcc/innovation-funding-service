*** Settings ***
Documentation     INFUND-921 : As an applicant I want to be able to select a link from the competition web page to visit a competition further description page containing relevant links so that I can apply into the competition.
Suite Setup       Login as User    &{lead_applicant_credentials}
Suite Teardown    TestTeardown User closes the browser
Resource          ../../../resources/GLOBAL_LIBRARIES.robot
Resource          ../../../resources/variables/GLOBAL_VARIABLES.robot
Resource          ../../../resources/variables/User_credentials.robot
Resource          ../../../resources/keywords/Login_actions.robot
Resource          ../../../resources/keywords/Applicant_actions.robot

*** Test Cases ***
The user is logged in
    [Documentation]    INFUND-921
    [Tags]    Applicant    Details page     HappyPath
    Given the Applicant is in Competition details page
    When the Applicant clicks on "Create application" button
    Then the Applicant should redirect to the Check Eligibility

The user is not logged in and later enters correct login
    [Documentation]    INFUND-921
    [Tags]    Applicant    Details page
    Given the applicant is logged-out
    When the Applicant is in Competition details page
    Then the Applicant should see "Sign in to Apply" button
    And the Applicant will click on "Sign in to Apply" button
    And the Applicant will input correct login details
    And the Applicant will redirect to the "Your Details" page

The user is not logged in and later enters incorrect login
    [Documentation]    INFUND-921
    [Tags]    Applicant    Details page
    Given the applicant is logged-out
    and the Applicant is in Competition details page
    and the Applicant should see "Sign in to Apply" button
    When the Applicant will click on "Sign in to Apply" button
    And the Applicant will input incorrect login details
    and the Applicant will input correct login details
    And the Applicant will redirect to the "Your Details" page

*** Keywords ***
the Applicant should see "Sign in to Apply" button
    Page Should Contain Element    jQuery=.column-third .button:contains('Sign in')

the Applicant is in Competition details page
    go to    ${COMPETITION_DETAILS_URL}

the Applicant will click on "Sign in to Apply" button
    Click Element    jQuery=.column-third .button:contains('Sign in')
    # TODO DW - INFUND-936 - reinstate expectations
    # Wait Until Page Contains    Sign in
    Wait Until Page Contains   Don't Remember Login

the Applicant will redirect to the "Your Details" page
    Location Should Be    ${YOUR_DETAILS}
    Page Should Not Contain Element    css=.error

the Applicant will input incorrect login details
    Input Text    id=username    steve.smith@empire.com
    Input Password    id=password    abcd
    Click Button    css=button[name="_eventId_proceed"]

the Applicant will input correct login details
    Input Text    id=username    steve.smith@empire.com
    Input Password    id=password    test
    Click Button    css=button[name="_eventId_proceed"]

the applicant is logged-out
    go to    ${LOG_OUT}
    go to    ${LOG_OUT}

the Applicant should see "Create Application" button
    Page Should Contain Element    css=.column-third a.button

the Applicant clicks on "Create application" button
    Click Element    jQuery=.column-third .button:contains('Create application')

the Applicant should redirect to the Check Eligibility
    Location should be    ${CHECK_ELIGIBILITY}
    Page Should Not Contain Element    css=.error
