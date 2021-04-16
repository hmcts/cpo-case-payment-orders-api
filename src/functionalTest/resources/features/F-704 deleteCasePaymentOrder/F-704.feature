Feature:

  Background:
    Given an appropriate test context as detailed in the test data source,
    And a user with [an active profile in CCD],
    And a successful call [to create a case payment order CP1] as in [Prerequisite_Create_CPO],
    And a successful call [to create a case payment order CP2] as in [Prerequisite_Create_CPO_2],

  @S-704.1
  Scenario: AC1: Successfully allow the delete of existing payment order(s) from the Case Payment Order database (Happy Path 1 - Ids supplied in parameters)
    When a request is prepared with appropriate values
    And the request [contains a set of ids from the case payment order just created above]
    And the request [contains all the mandatory parameters]
      And it is submitted to call the [deleteCasePaymentOrder] operation of [Case Payment Orders Microservice]
    Then a positive response is received
    And the response [contains a 204]
    And the response has all other details as expected
    And a call [to verify that the Case payment Orders CP1 And CP2 has been deleted from the database] will get the expected response as in [CPO_Deleted].

  @S-704.2
  Scenario: AC2: Successfully allow the delete of existing payment order(s) from the Case Payment Order database (Happy Path 2 - Case Ids supplied in parameters)
    When a request is prepared with appropriate values
    And the request [contains a set of case ids from the case payment order just created above]
    And the request [contains all the mandatory parameters]
    And it is submitted to call the [deleteCasePaymentOrder] operation of [Case Payment Orders Microservice]
    Then a positive response is received
    And the response [contains a 204]
    And the response has all other details as expected
    And a call [to verify that the Case payment Orders CP1 And CP2 has been deleted from the database] will get the expected response as in [CPO_Deleted].

  @S-704.3
  Scenario: AC3: Mandatory parameters missing from the request (Search Criteria Missing)
    When a request is prepared with appropriate values
    And the request [does not contain the search criteria parameters]
    And it is submitted to call the [deleteCasePaymentOrder] operation of [Case Payment Orders Microservice]
    Then a positive response is received
    And the response has all other details as expected

  @S-704.4
  Scenario: AC4: Mandatory parameters missing from the request (User token Missing)
    When a request is prepared with appropriate values
    And the request [does not contain the user token parameter]
    And the request [contains a set of ids from the case payment order just created above]

    And it is submitted to call the [deleteCasePaymentOrder] operation of [Case Payment Orders Microservice]
    Then a negative response is received
    And the response has all other details as expected
    And a call [to verify that the Case payment Orders CP1 And CP2 have not been deleted from the database] will get the expected response as in [CPO_Present].

  @S-704.5
  Scenario: AC5a: Request to delete non-existing case payment order records
    When a request is prepared with appropriate values
    And the request [contains an id that doesn't exist in the Case Payment Orders database]
    And it is submitted to call the [deleteCasePaymentOrder] operation of [Case Payment Orders Microservice]
    Then a negative response is received
    And the response has all other details as expected
    And a call [to verify that the Case payment Orders CP1 And CP2 have not been deleted from the database] will get the expected response as in [CPO_Present].

  @S-704.6
  Scenario: AC5b: Request to delete non-existing case payment order records
    When a request is prepared with appropriate values
    And the request [contains an case_ids that doesn't exist in the Case Payment Orders database]
    And it is submitted to call the [deleteCasePaymentOrder] operation of the [Case Payment Orders Microservice]
    Then a negative response is received
    And the response has all other details as expected
    And a call [to verify that the Case payment Orders CP1 And CP2 have not been deleted from the database] will get the expected response as in [CPO_Present].

