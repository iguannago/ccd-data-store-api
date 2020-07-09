@F-109
Feature: Add support in CCD role based authorisation for caseworker-caa

  Background: Load test data for the scenario
    Given an appropriate test context as detailed in the test data source

    @S-942
  Scenario: Must return /searchCases values from Datastore for all jurisdictions for the given case type (1/2)
    Given a user [Richard - with access to create cases for various jurisdictions eg Divorce & Probate]
    And a case that has just been created as in [Case_Creation_Using_Caseworker1_Role]
#      And a case that has just been created as in [Befta_Jurisdiction2_Default_Full_Case_Creation_Data]
    And a wait time of 5 seconds [to allow for Logstash to index the case just created]
    And a user [Jamal - a client with only the 'caseworker-caa' role which is configured with the required CRUD permissions for the case types of C1 & C2 on the definition file]
    When a request is prepared with appropriate values
    And the request [is made by Jamal via the new Manage Case Assignment (MCA) microservice]
    And it is submitted to call the [/searchCases] operation of [CCD Data Store api]
    And the request [contains the case type of C1]
    Then a positive response is received
    And the response [includes data for the given case type for C1]
    And the response has all the details as expected



#  Scenario 2 - Must return /searchCases values from Datastore for all jurisdictions for the given case type (2/2)
#    Given an appropriate test context as detailed in the test data source
#    And a user [Richard - with access to create cases for various jurisdictions eg Divorce & Probate]
#    And a successful call [by Richard to create a case (C1) for a jurisdiction eg Divorce] as in [Prerequisite Case Creation Call for Case Assignment]
#    And a successful call [by Richard to create another case (C2) for a different jurisdiction eg Probate] as in [Prerequisite Case Creation Call for Case Assignment]
#    And a user [Jamal - a client with only the 'caseworker-caa' role which is configured with the required CRUD permissions for the case types of C1 & C2 on the definition file]
#    When a request is prepared with appropriate values
#    And the request [is made by Jamal via the new Manage Case Assignment (MCA) microservice]
#    And the request [contains the case type of C2]
#    And it is submitted to call the [/searchCases] operation of [CCD Data Store api]
#    Then a positive response is received
#    And the response [includes data for the given case type for C2]
#    And the response has all the details as expected


#  Scenario 3 - Must return a positive response when required CRUD permissions have not been configured for the caseworker-caa for the case type (/searchCases)
#  Given an appropriate test context as detailed in the test data source
#
#  And a user [Richard - with access to create cases for various jurisdictions eg Divorce & Probate]
#
#  And a successful call [by Richard to create a case (C1) for a jurisdiction eg Divorce] as in [Prerequisite Case Creation Call for Case Assignment]
#
#  And a successful call [by Richard to create another case (C2) for a different jurisdiction eg Probate] as in [Prerequisite Case Creation Call for Case Assignment]
#
#  And a user [Jamal - a client with only the 'caseworker-caa' role which has not been configured with the required CRUD permissions for the case types of C1 or C2 on the definition file]
#
#  When a request is prepared with appropriate values
#
#  And the request [is made by Jamal via the new Manage Case Assignment (MCA) microservice]
#
#  And the request [contains the case type of either C1 or C2]
#
#  And it is submitted to call the [/searchCases] operation of [CCD Data Store api]
#
#  Then a positive response is received
#
#  And the response [only includes data relating to the case types for which Jamal has the required CRUD permissions for - ie no results are returned in this example]
#
#  And the response has all the details as expected
#
#
#
#
#
#  Scenario 4 - Must return /internal/searchCases values from Datastore for all jurisdictions for the given case type (1/2)
#  Given an appropriate test context as detailed in the test data source
#
#  And a user [Richard - with access to create cases for various jurisdictions eg Divorce & Probate]
#
#  And a successful call [by Richard to create a case (C1) for a jurisdiction eg Divorce] as in [Prerequisite Case Creation Call for Case Assignment]
#
#  And a successful call [by Richard to create another case (C2) for a different jurisdiction eg Probate] as in [Prerequisite Case Creation Call for Case Assignment]
#
#  And a user [Jamal - a client with only the 'caseworker-caa' role which is configured with the required CRUD permissions for the case types of C1 & C2 on the definition file]
#
#  When a request is prepared with appropriate values
#
#  And the request [is made by Jamal via the new Manage Case Assignment (MCA) microservice]
#
#  And the request [contains the case type of C1]
#
#  And it is submitted to call the [/internal/searchCases] operation of [CCD Data Store api]
#
#  Then a positive response is received
#
#  And the response [includes data for the given case type for C1]
#
#  And the response has all the details as expected
#
#
#
#
#
#  Scenario 5 - Must return /internal/searchCases values from Datastore for all jurisdictions for the given case type (2/2)
#  Given an appropriate test context as detailed in the test data source
#
#  And a user [Richard - with access to create cases for various jurisdictions eg Divorce & Probate]
#
#  And a successful call [by Richard to create a case (C1) for a jurisdiction eg Divorce] as in [Prerequisite Case Creation Call for Case Assignment]
#
#  And a successful call [by Richard to create another case (C2) for a different jurisdiction eg Probate] as in [Prerequisite Case Creation Call for Case Assignment]
#
#  And a user [Jamal - a client with only the 'caseworker-caa' role which is configured with the required CRUD permissions for the case types of C1 & C2 on the definition file]
#
#  When a request is prepared with appropriate values
#
#  And the request [is made by Jamal via the new Manage Case Assignment (MCA) microservice]
#
#  And the request [contains the case type of C2]
#
#  And it is submitted to call the [/internal/searchCases] operation of [CCD Data Store api]
#
#  Then a positive response is received
#
#  And the response [includes data for the given case type for C2]
#
#  And the response has all the details as expected
#
#
#  Scenario 6 - Must return a positive response when required CRUD permissions have not been configured for the caseworker-caa for the case type (/internal/searchCases)
#  Given an appropriate test context as detailed in the test data source
#
#  And a user [Richard - with access to create cases for various jurisdictions eg Divorce & Probate]
#
#  And a successful call [by Richard to create a case (C1) for a jurisdiction eg Divorce] as in [Prerequisite Case Creation Call for Case Assignment]
#
#  And a successful call [by Richard to create another case (C2) for a different jurisdiction eg Probate] as in [Prerequisite Case Creation Call for Case Assignment]
#
#  And a user [Jamal - a client with only the 'caseworker-caa' role which has not been configured with the required CRUD permissions for the case types of C1 or C2 on the definition file]
#
#  When a request is prepared with appropriate values
#
#  And the request [is made by Jamal via the new Manage Case Assignment (MCA) microservice]
#
#  And the request [contains the case type of either C1 or C2]
#
#  And it is submitted to call the [/internal/searchCases] operation of [CCD Data Store api]
#
#  Then a positive response is received
#
#  And the response [only includes data relating to the case types for which Jamal has the required CRUD permissions for - ie no results are returned in this example]
#
#  And the response has all the details as expected
#
#
#
#
#
#  Scenario 7 - Must return /case-users values from Datastore for all jurisdictions for the given case type (1/2)
#  Given an appropriate test context as detailed in the test data source
#
#  And a user [Richard - with access to create cases for various jurisdictions eg Divorce & Probate]
#
#  And a successful call [by Richard to create a case (C1) for a jurisdiction eg Divorce] as in [Prerequisite Case Creation Call for Case Assignment]
#
#  And a successful call [by Richard to create another case (C2) for a different jurisdiction eg Probate] as in [Prerequisite Case Creation Call for Case Assignment]
#
#  And a user [Jamal - a client with only the 'caseworker-caa' role which is configured with the required CRUD permissions for the case types of C1 & C2 on the definition file]
#
#  When a request is prepared with appropriate values
#
#  And the request [is made by Jamal via the new Manage Case Assignment (MCA) microservice]
#
#  And the request [contains the case type of C1]
#
#  And it is submitted to call the [/case-users] operation of [CCD Data Store api]
#
#  Then a positive response is received
#
#  And the response [includes data for the given case type for C1]
#
#  And the response has all the details as expected
#
#
#
#
#
#  Scenario 8 - Must return /case-users values from Datastore for all jurisdictions for the given case type (2/2)
#  Given an appropriate test context as detailed in the test data source
#
#  And a user [Richard - with access to create cases for various jurisdictions eg Divorce & Probate]
#
#  And a successful call [by Richard to create a case (C1) for a jurisdiction eg Divorce] as in [Prerequisite Case Creation Call for Case Assignment]
#
#  And a successful call [by Richard to create another case (C2) for a different jurisdiction eg Probate] as in [Prerequisite Case Creation Call for Case Assignment]
#
#  And a user [Jamal - a client with only the 'caseworker-caa' role which is configured with the required CRUD permissions for the case types of C1 & C2 on the definition file]
#
#  When a request is prepared with appropriate values
#
#  And the request [is made by Jamal via the new Manage Case Assignment (MCA) microservice]
#
#  And the request [contains the case type of C2]
#
#  And it is submitted to call the [/case-users] operation of [CCD Data Store api]
#
#  Then a positive response is received
#
#  And the response [includes data for the given case type for C2]
#
#  And the response has all the details as expected
#
#
#  Scenario 9 - Must return a positive response when required CRUD permissions have not been configured for the caseworker-caa for the case type (/case-users)
#  Given an appropriate test context as detailed in the test data source
#
#  And a user [Richard - with access to create cases for various jurisdictions eg Divorce & Probate]
#
#  And a successful call [by Richard to create a case (C1) for a jurisdiction eg Divorce] as in [Prerequisite Case Creation Call for Case Assignment]
#
#  And a successful call [by Richard to create another case (C2) for a different jurisdiction eg Probate] as in [Prerequisite Case Creation Call for Case Assignment]
#
#  And a user [Jamal - a client with only the 'caseworker-caa' role which has not been configured with the required CRUD permissions for the case types of C1 or C2 on the definition file]
#
#  When a request is prepared with appropriate values
#
#  And the request [is made by Jamal via the new Manage Case Assignment (MCA) microservice]
#
#  And the request [contains the case type of either C1 or C2]
#
#  And it is submitted to call the [/case-users] operation of [CCD Data Store api]
#
#  Then a positive response is received
#
#  And the response [only includes data relating to the case types for which Jamal has the required CRUD permissions for - ie no results are returned in this example]
#
#  And the response has all the details as expected
#
#
#
#
#
#
#
#  Scenario 10 - Must return /cases/{caseId}/supplementary-data values from Datastore for all jurisdictions and all case types
#  Given an appropriate test context as detailed in the test data source
#
#  And a user [Richard - with access to create cases for various jurisdictions eg Divorce & Probate]
#
#  And a successful call [by Richard to create a case (C1) for a jurisdiction eg Divorce] as in [Prerequisite Case Creation Call for Case Assignment]
#
#  And a successful call [by Richard to create another case (C2) for a different jurisdiction eg Probate] as in [Prerequisite Case Creation Call for Case Assignment]
#
#  And a user [Jamal - a client with only the 'caseworker-caa' role which is configured with the required CRUD permissions for the case types of C1 & C2 on the definition file]
#
#  When a request is prepared with appropriate values
#
#  And the request [is made by Jamal via the new Manage Case Assignment (MCA) microservice]
#
#  And the request [does not contain any specific case type]
#
#  And it is submitted to call the [/cases/
#
#  {caseId}/supplementary-data] operation of [CCD Data Store api]
#
#  Then a positive response is received
#
#  And the response [includes data for the case type of both C1 & C2]
#
#  And the response has all the details as expected
#
#
#
#
#
#  Scenario 11 - Must return a positive response when required CRUD permissions have not been configured for the caseworker-caa for a given case type (/cases/{caseId}/supplementary-data)
#  Given an appropriate test context as detailed in the test data source
#
#  And a user [Richard - with access to create cases for various jurisdictions eg Divorce & Probate]
#
#  And a successful call [by Richard to create a case (C1) for a jurisdiction eg Divorce] as in [Prerequisite Case Creation Call for Case Assignment]
#
#  And a successful call [by Richard to create another case (C2) for a different jurisdiction eg Probate] as in [Prerequisite Case Creation Call for Case Assignment]
#
#  And a user [Jamal - a client with only the 'caseworker-caa' role which has not been configured with the required CRUD permissions for the case types of C1 and/or C2 on the definition file]
#
#  When a request is prepared with appropriate values
#
#  And the request [is made by Jamal via the new Manage Case Assignment (MCA) microservice]
#
#  And the request [does not contain any specific case type]
#
#  And it is submitted to call the [/cases/{caseId}/supplementary-data] operation of [CCD Data Store api]
#
#  Then a positive response is received
#
#  And the response [only includes data relating to the case types for which Jamal has the required CRUD permissions for - ie no results are returned in this example]
#
#  And the response has all the details as expected
