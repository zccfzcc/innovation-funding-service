*** Settings ***
Documentation     INFUND-7734 Competition Management: Assign to application dashboard in Closed competition
...
...               INFUND-7729 Competition management: Allocate application dashboard on Closed competition
...
...               INFUND-8061 Filter and pagination on Allocate Applications (Closed competition) and Manage applications (In assessment) dashboards
...
...               INFUND-8062 Filter and pagination on Assign to application (Closed competition) and Application progress dashboards
...
...               IFS-17 View list of accepted assessors - Closed state
Suite Setup       The user logs-in in new browser  &{Comp_admin1_credentials}
Suite Teardown    The user closes the browser
Force Tags        CompAdmin
Resource          ../../resources/defaultResources.robot

*** Test Cases ***
Search for applications
    [Documentation]    INFUND-8061
    [Tags]
    Given The user clicks the button/link      link=${CLOSED_COMPETITION_NAME}
    And the user clicks the button/Link        jQuery=a:contains("Manage assessments")
    And the user clicks the button/link        jQuery=a:contains("Allocate applications")
    When The user enters text to a text field  css=#filterSearch    ${CLOSED_COMPETITION_APPLICATION}
    and The user clicks the button/link        jQuery=button:contains(Filter)
    Then the user should see the element       jQuery=tr:nth-child(1) td:nth-child(1):contains("${CLOSED_COMPETITION_APPLICATION}")
    And The user clicks the button/link        link=Clear all filters
    then the user should not see the element   jQuery=tr:nth-child(1) td:nth-child(1):contains("137")

Filtering the Assessors in the Allocate Applications page
    [Documentation]    INFUND-7042  INFUND-7729  INFUND-8062
    [Tags]
    Given the user clicks the button/Link                     jQuery=tr:contains(Neural Industries) .no-margin
    And the user should see the element                       jQuery=h3:contains("Innovation area") ~ span:contains("Smart infrastructure")
    Then the user should see the element                      jQuery=tr:nth-child(1) td:contains("Benjamin Nixon")    #this check verfies that the list of assessors in alphabetical order
    When the user selects the option from the drop-down menu  Materials, process and manufacturing design technologies    id=filterInnovationArea
    And the user clicks the button/link                       jQuery=button:contains(Filter)
    Then the user should see the element                      jQuery=td:contains("Benjamin Nixon")
    And the user should see the element                       jQuery=td:contains("Paige Godfrey")
    And the user should not see the element                   jQuery=td:contains("Riley Butler")
    And the user clicks the button/link                       jQuery=a:contains("Clear all filters")
    And the user should see the element                       jQuery=td:contains("Riley Butler")

Filtering Assessors in the Assign assessors page
    [Documentation]    INFUND-8062
    [Tags]
    Given the user clicks the button/Link                     jQuery=tr:contains(Benjamin) .no-margin
    When the user selects the option from the drop-down menu  Materials, process and manufacturing design technologies    id=filterInnovationArea
    And the user clicks the button/link                       jQuery=button:contains(Filter)
    Then the user should see the element                      jQuery=td:contains("Paige Godfrey")
    And the user clicks the button/link                       jQuery=a:contains("Clear all filters")
    Then the user should see the element                      jQuery=td:contains("Riley Butler")
    [Teardown]  the user clicks the button/link               link=Allocate applications

Manage assessor list is correct
    [Documentation]    IFS-17
    [Tags]
    [Setup]  the user clicks the button/link  link=Manage assessments
    Given the user clicks the button/link     link=Allocate assessors
    Then the assessor list is correct before changes

Assessor link goes to the assessor profile
    [Documentation]  IFS-17
    [Tags]
    Given the user clicks the button/link        link=Madeleine Martin
    Then the user should see the element         jQuery=h1:contains("Assessor profile") ~ p:contains("Madeleine Martin")
    [Teardown]  the user clicks the button/link  link=Back

*** Keywords ***
the assessor list is correct before changes
    the user should see the element  jQuery=td:contains("Madeleine Martin") ~ td:contains("0") ~ td:contains("0")