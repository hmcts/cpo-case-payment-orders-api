#================================================
@F-703
Feature: GET Case Payment Order Endpoint
#================================================


  Background:
    Given an appropriate test context as detailed in the test data source,
      And a user with [an active profile in CCD],


#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
# CPO-7 / AC-1 (CPO-33 / AC-1)
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


#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
# CPO-7 / AC-2
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


#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
# CPO-7 / AC-3
  @S-703.3
  Scenario: AC3: Mandatory parameters missing from the request (Search Criteria Missing)

    When a request is prepared with appropriate values
      And the request [does not contain the search criteria parameters]
      And it is submitted to call the [getCasePaymentOrder] operation of [Case Payment Orders Microservice]

    Then a positive response is received
      And the response [contains relevant error message]
      And the response has all other details as expected


#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
# CPO-7 / AC-4
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


#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
# CPO-7 / AC-5
  @S-703.5
  Scenario: AC5a: Request to get non-existing case payment order records

    When a request is prepared with appropriate values
      And the request [contains a set of ids that don't exist in the Case Payment Orders database]
      And the request [contains all the mandatory parameters]
      And it is submitted to call the [getCasePaymentOrder] operation of [Case Payment Orders Microservice]

    Then a negative response is received
      And the response [contains relevant error message]
      And the response has all other details as expected


#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
# CPO-7 / AC-6
  @S-703.6
  Scenario: AC5b: Request to get non-existing case payment order records

    When a request is prepared with appropriate values
      And the request [contains a set of case_ids that don't exist in the Case Payment Orders database]
      And the request [contains all the mandatory parameters]
      And it is submitted to call the [getCasePaymentOrder] operation of [Case Payment Orders Microservice]

    Then a negative response is received
      And the response [contains relevant error message]
      And the response has all other details as expected


#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
# CPO-7 / AC-9
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


#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
# CPO-7 / AC-10
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


#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
# CPO-7 / AC-8
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


#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
# CPO-33 / AC-2
  @S-703.11
  Scenario: Mandatory parameters missing from the request (IDAM token missing)
      And [a new Case-Payment-Order microservice has been established] in the context,
      And a successful call [to create a case payment order CP1] as in [Prerequisite_Create_CPO],

    When a request is prepared with appropriate values,
      And the request [contains a set of ids from the case payment orders previously created]
      And the request [contain NO IDAM token],
      And it is submitted to call the [getCasePaymentOrder] operation of [Case Payment Orders Microservice],

    Then a negative response is received,
      And the response [contains relevant error message].


#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
# CPO-33 / AC-3
  @S-703.12
  Scenario: Mandatory parameters missing from the request (IDAM token invalid)
      And [a new Case-Payment-Order microservice has been established] in the context,
      And a successful call [to create a case payment order CP1] as in [Prerequisite_Create_CPO],

    When a request is prepared with appropriate values,
      And the request [contains a set of ids from the case payment orders previously created]
      And the request [contain the invalid IDAM token parameter],
      And it is submitted to call the [getCasePaymentOrder] operation of [Case Payment Orders Microservice],

    Then a negative response is received,
      And the response [contains relevant error message].


#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
# CPO-33 / AC-4
  @S-703.13
  Scenario: Mandatory parameters missing from the request (S2S token missing)
      And [a new Case-Payment-Order microservice has been established] in the context,
      And a successful call [to create a case payment order CP1] as in [Prerequisite_Create_CPO],

    When a request is prepared with appropriate values,
      And the request [contains a set of ids from the case payment orders previously created]
      And the request [contain NO S2S token],
      And it is submitted to call the [getCasePaymentOrder] operation of [Case Payment Orders Microservice],

    Then a negative response is received,
      And the response [contains relevant error message].


#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
# CPO-33 / AC-5
  @S-703.14
  Scenario: Mandatory parameters missing from the request (S2S token invalid)
      And [a new Case-Payment-Order microservice has been established] in the context,
      And a successful call [to create a case payment order CP1] as in [Prerequisite_Create_CPO],

    When a request is prepared with appropriate values,
      And the request [contains a set of ids from the case payment orders previously created]
      And the request [contain the invalid S2S token parameter],
      And it is submitted to call the [getCasePaymentOrder] operation of [Case Payment Orders Microservice],

    Then a negative response is received,
      And the response [contains relevant error message].

