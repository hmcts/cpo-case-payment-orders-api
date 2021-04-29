@F-703
Feature: GET Case Payment Order Endpoint

  Background:
    Given an appropriate test context as detailed in the test data source,
    And a user with [an active profile in CCD],

  @S-703.1
  Scenario: AC1: Successfully allow the collection of existing payment order(s) from the Case Payment Order database (Happy Path 1 - Ids supplied in parameters)
    And a successful call [to create a case payment order CP1] as in [Prerequisite_Create_CPO],
    And a successful call [to create a case payment order CP2] as in [Prerequisite_Create_CPO_2],
    When a request is prepared with appropriate values
    And the request [contains a set of ids from the case payment orders previously created]
    And the request [contains all the mandatory parameters]
    And the request [does not contain defined optional parameters for page_size And page_number]
    And it is submitted to call the [getCasePaymentOrder] operation of [Case Payment Orders Microservice]
    Then a positive response is received
    And the response [contains a 200 success OK code]
    And the response [case payment order CP1, CP2]
    And the response [has page_size default 20 and page_number default 1]
    And the response has all other details as expected

  @S-703.2
  Scenario: AC2: Successfully allow the collection of existing payment order(s) from the Case Payment Order database (Happy Path 2 - Case Ids supplied in parameters)
    And a successful call [to create a case payment order CP1] as in [Prerequisite_Create_CPO],
    And a successful call [to create a case payment order CP2] as in [Prerequisite_Create_CPO_2],
    When a request is prepared with appropriate values
    And the request [contains a set of case ids from the case payment orders previously created]
    And the request [contains all the mandatory parameters]
    And the request [does not contain defined optional parameters for page_size And page_number]
    And it is submitted to call the [getCasePaymentOrder] operation of [Case Payment Orders Microservice]
    Then a positive response is received
    And the response [contains a 200 success OK code]
    And the response [case payment order CP1, CP2]
    And the response [has page_size default 20 and page_number default 1]
    And the response has all other details as expected

  @S-703.3
  Scenario: AC3: Mandatory parameters missing from the request (Search Criteria Missing)
    And a successful call [to create a case payment order CP1] as in [Prerequisite_Create_CPO],
    And a successful call [to create a case payment order CP2] as in [Prerequisite_Create_CPO_2],
    When a request is prepared with appropriate values
    And the request [does not contain the search criteria parameters]
    And it is submitted to call the [getCasePaymentOrder] operation of [Case Payment Orders Microservice]
    Then a positive response is received
    And the response [contains relevant error message]
    And the response has all other details as expected

  @S-703.4
  Scenario: AC4: Mandatory parameters missing from the request (User token Missing)
    And a successful call [to create a case payment order CP1] as in [Prerequisite_Create_CPO],
    And a successful call [to create a case payment order CP2] as in [Prerequisite_Create_CPO_2],
    When a request is prepared with appropriate values
    And the request [does not contain the user token parameter]
    And it is submitted to call the [getCasePaymentOrder] operation of [Case Payment Orders Microservice]
    Then a negative response is received
    And the response [contains relevant error message]
    And the response has all other details as expected

  @S-703.5
  Scenario: AC5a: Request to get non-existing case payment order records
    When a request is prepared with appropriate values
    And the request [contains a set of ids that don't exist in the Case Payment Orders database]
    And the request [contains all the mandatory parameters]
    And it is submitted to call the [getCasePaymentOrder] operation of [Case Payment Orders Microservice]
    Then a negative response is received
    And the response [contains relevant error message]
    And the response has all other details as expected

  @S-703.6
  Scenario: AC5b: Request to get non-existing case payment order records
    When a request is prepared with appropriate values
    And the request [contains a set of case_ids that don't exist in the Case Payment Orders database]
    And the request [contains all the mandatory parameters]
    And it is submitted to call the [getCasePaymentOrder] operation of [Case Payment Orders Microservice]
    Then a negative response is received
    And the response [contains relevant error message]
    And the response has all other details as expected

  @S-703.9
  Scenario: Using paging only return result that should be on page 1
    And a successful call [to create a case payment order CP1] as in [Prerequisite_Create_CPO],
    And a successful call [to create a case payment order CP2] as in [Prerequisite_Create_CPO_2],
    And a successful call [to create a case payment order CP3] as in [Prerequisite_Create_CPO_3],
    When a request is prepared with appropriate values
    And the request [contains a set of case ids from the case payment order just created above]
    And the request [contains all the mandatory parameters]
    And the request [contains a defined optional parameter for page_size 2]
    And the request [contains a defined optional parameter for page_number 1]
    And it is submitted to call the [getCasePaymentOrder] operation of [Case Payment Orders Microservice]
    Then a positive response is received
    And the response [contains a 200 success OK code]
    And the response has all other details as expected
    And the response [contains case payment order CP1, CP2]
    And the response [does not contain case payment order CP3]
    And the response [has a page_size parameter value 2]
    And the response [has a page_number parameter value of 1]

  @S-703.10
  Scenario: Using paging only return result that should be on page 2
    And a successful call [to create a case payment order CP1] as in [Prerequisite_Create_CPO],
    And a successful call [to create a case payment order CP2] as in [Prerequisite_Create_CPO_2],
    And a successful call [to create a case payment order CP3] as in [Prerequisite_Create_CPO_3],
    When a request is prepared with appropriate values
    And the request [contains a set of case ids from the case payment order just created above]
    And the request [contains all the mandatory parameters]
    And the request [contains a defined optional parameter for page_size 2]
    And the request [contains a defined optional parameter for page_number 2]
    And it is submitted to call the [getCasePaymentOrder] operation of [Case Payment Orders Microservice]
    Then a positive response is received
    And the response [contains a 200 success OK code]
    And the response has all other details as expected
    And the response [contains case payment order CP3]
    And the response [does not contain case payment order CP1, CP2]
    And the response [has a page_size parameter value 2]
    And the response [has a page_number parameter value of 2]

  @S-703.8
  Scenario: AC8: Both the Id And Case Ids supplied in the request
    And a successful call [to create a case payment order CP1] as in [Prerequisite_Create_CPO],
    And a successful call [to create a case payment order CP2] as in [Prerequisite_Create_CPO_2],
    When a request is prepared with appropriate values
    And the request [contains both the Ids And Case Ids query parameters]
    And it is submitted to call the [getCasePaymentOrder] operation of [Case Payment Orders Microservice]
    Then a negative response is received
    And the response [contains relevant error message]
    And the response has all other details as expected
