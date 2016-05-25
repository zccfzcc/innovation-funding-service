*** Settings ***
Documentation     INFUND-1110: As an applicant/partner applicant I want to add my required Funding Level so that innovate uk know my grant request
Suite Setup       A new application is created to test the org size options
Suite Teardown    User closes the browser
Force Tags
Resource          ../../../../resources/GLOBAL_LIBRARIES.robot
Resource          ../../../../resources/variables/GLOBAL_VARIABLES.robot
Resource          ../../../../resources/variables/User_credentials.robot
Resource          ../../../../resources/keywords/Login_actions.robot
Resource          ../../../../resources/keywords/User_actions.robot

*** Variables ***
${small_org_option}    SMALL
${medium_org_option}    MEDIUM
${large_org_option}    LARGE
${no_org_selected_message}    Funding level allowed depends on organisation size. Please select your organisation size.
${incorrect_funding_level_message}    This field should be

*** Test Cases ***
One of the org size options must be selected
    [Documentation]    INFUND-2643
    [Tags]    Organisation    Funding    Finance
    [Setup]    Guest user log-in    &{lead_applicant_credentials}
    # Given the user navigates to the page    ${newly_created_application_your_finances_url}
    Given the user clicks the button/link    link=${OPEN_COMPETITION_LINK}
    And the user clicks the button/link    link=Your finances
    And the applicant enters the funding level    50
    When the applicant chooses to save and return to application overview
    Then the 'your finances' section cannot be successfully saved with the message    ${no_org_selected_message}

Small organisation can't choose over 70% funding
    [Documentation]    INFUND-1100
    [Tags]    Organisation    Funding    Finance
    When the applicant enters organisation size details    ${small_org_option}    82
    Then the 'your finances' section cannot be successfully saved with the message    ${incorrect_funding_level_message}

Small organisation can choose up to 70% funding
    [Documentation]    INFUND-1100
    [Tags]    Organisation    Funding    Finance    HappyPath
    When the applicant enters organisation size details    ${small_org_option}    68
    Then the 'your finances' section can be successfully saved    ${small_org_option}    68
    [Teardown]    user closes the browser

Medium organisation can't choose over 60% funding
    [Documentation]    INFUND-1100
    [Tags]    Organisation    Funding    Finance
    [Setup]    Guest user log-in    &{lead_applicant_credentials}
    # Given the user navigates to the page    ${newly_created_application_your_finances_url)
    Given the user clicks the button/link    link=${OPEN_COMPETITION_LINK}
    And the user clicks the button/link    link=Your finances
    When the applicant enters organisation size details    ${medium_org_option}    68
    Then the 'your finances' section cannot be successfully saved with the message    ${incorrect_funding_level_message}

Medium organisation can choose up to 60% funding
    [Documentation]    INFUND-1100
    [Tags]    Organisation    Funding    Finance
    When the applicant enters organisation size details    ${medium_org_option}    53
    Then the 'your finances' section can be successfully saved    ${medium_org_option}    53
    [Teardown]    user closes the browser

Large organisation can't choose over 50% funding
    [Documentation]    INFUND-1100
    [Tags]    Organisation    Funding    Finance
    [Setup]    Guest user log-in    &{lead_applicant_credentials}
    # Given the user navigates to the page    ${newly_created_application_your_finances_url}
    Given the user clicks the button/link    link=${OPEN_COMPETITION_LINK}
    And the user clicks the button/link    link=Your finances
    When the applicant enters organisation size details    ${large_org_option}    54
    Then the 'your finances' section cannot be successfully saved with the message    ${incorrect_funding_level_message}

Large organisation can choose up to 50% funding
    [Documentation]    INFUND-1100
    [Tags]    Organisation    Funding    Finance
    When the applicant enters organisation size details    ${large_org_option}    43
    Then the 'your finances' section can be successfully saved    ${large_org_option}    43

*** Keywords ***
The applicant enters organisation size details
    [Arguments]    ${org_size_option}    ${funding_level}
    the applicant enters the organisation size    ${org_size_option}
    the applicant enters the funding level    ${funding_level}
    the applicant chooses to save and return to application overview

The 'your finances' section can be successfully saved
    [Arguments]    ${org_size_option}    ${funding_level}
    The user clicks the button/link    link=Your finances
    the applicant can see the correct organisation size has been selected    ${org_size_option}
    the applicant can see the correct funding level has been saved    ${funding_level}

The 'your finances' section cannot be successfully saved with the message
    [Arguments]    ${warning_message}
    the user should see the text in the page    Organisation Size
    the user should see the text in the page    ${warning_message}

The applicant enters the organisation size
    [Arguments]    ${org_size_option}
    Select Radio Button    financePosition-organisationSize    ${org_size_option}

The applicant enters the funding level
    [Arguments]    ${funding_level}
    Input Text    id=cost-financegrantclaim    ${funding_level}

The applicant chooses to save and return to application overview
    Click Button    Save and return to application overview

The applicant can see the correct organisation size has been selected
    [Arguments]    ${org_size_option}
    Radio Button Should Be Set To    financePosition-organisationSize    ${org_size_option}

The applicant can see the correct funding level has been saved
    [Arguments]    ${funding_level}
    Wait Until Element Is Visible    id=cost-financegrantclaim
    ${saved_funding_level} =    Get Element Attribute    id=cost-financegrantclaim@value
    Should Be Equal As Integers    ${saved_funding_level}    ${funding_level}

A new application is created to test the org size options
    Guest user log-in    &{lead_applicant_credentials}
    the user navigates to the page    ${competition_details_url}
    the user clicks the button/link    jQuery=.button:contains("Apply now")
    the user should be redirected to the correct page    ${ELIGIBILITY_INFO_URL}
    the user clicks the button/link    jQuery=.button:contains("Apply now")
    the user should be redirected to the correct page    ${speed_bump_url}
    the user selects the radio button    create-application    true
    the user clicks the button/link    jQuery=.button:contains("Continue")
    the user should see the text in the page    Inviting Contributors and Partners
    the user clicks the button/link    jQuery=.button:contains("Begin application")
    the user should see the text in the page    Application overview
