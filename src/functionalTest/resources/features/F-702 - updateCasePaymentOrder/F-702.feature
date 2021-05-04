@F-702
Feature: UPDATE Case Payment Order Endpoint

  Background:
    Given an appropriate test context as detailed in the test data source,
    And a user with [an active profile in CCD],
    And a successful call [to create a case payment order for the created case] as in [Prerequisite_Create_CPO],

  @S-702.1
  Scenario: AC1: Successfully allow the update of a previously created payment order in the Case Payment Order database
    When a request is prepared with appropriate values
    And the request [intends to update a previously created payment order]
    And the request [contains all the mandatory parameters]
    And it is submitted to call the [updateCasePaymentOrder] operation of [Case Payment Orders Microservice]
    Then a positive response is received
    And the response [contains a 202 success code]
    And the response [contains a valid UUID of the case order record]
    And the response has all other details as expected
    And a call [to verify that the Case payment Order has been updated] will get the expected response as in [getCasePaymentOrder_cpo_updated].

  Scenario Outline: AC2- Must return error if one or more of the mandatory parameters have not been provided (Please refer to the mandatory parameter list in the description)**
    When a request is prepared with appropriate values
    And the request [intends to update a previously created payment order]
    And the request [does not contain one or more of the mandatory parameters]
    And the request [does not contain the <parameter> parameter]
    And it is submitted to call the [updateCasePaymentOrder] operation of [Case Payment Orders Microservice]
    Then a negative response is received
    And the response has all other details as expected
    And a call [to verify that the Case payment Order has not been updated] will get the expected response as in [getCasePaymentOrder_cpo_not_updated].


  @S-702.2.2
    Examples:
      | parameter        |
      | case_id          |

  @S-702.2.3
    Examples:
      | parameter        |
      | action           |


  @S-702.2.5
    Examples:
      | parameter        |
      | responsible_party|

  @S-702.2.6
    Examples:
      | parameter        |
      | order_reference  |

  Scenario Outline: AC3 - Must return an error if the request contains an invalid mandatory parameter
    When a request is prepared with appropriate values
    And the request [intends to update a previously created payment order]
    And the request [contains an <parameter>]
    And it is submitted to call the [updateCasePaymentOrder] operation of [Case Payment Orders Microservice]
    Then a negative response is received
    And the response has all other details as expected

  @S-702.3.1
    Examples:
      | parameter               |
      | incorrect caseid format |

  @S-702.3.2
    Examples:
      | parameter               |
      | invalid datetime format |


  @S-702.4
  Scenario: AC4- Must return error if order_reference for a case_id is non-unique
    And a successful call [to create a case payment order for the created case] as in [Prerequisite_Create_CPO_2],
    When a request is prepared with appropriate values
    And the request [intends to update a previously created payment order]
    And the request [contains all the mandatory parameters]
    And the request [contains the same order_reference as used in a previously created CPO for the same case id]
    And it is submitted to call the [updateCasePaymentOrder] operation of [Case Payment Orders Microservice]
    Then a negative response is received
    And the response has all other details as expected
    And a call [to verify that a Case payment Order has not been updated in the database] will get the expected response as in [S-702.4_CP1_CP2_not_updated].


  @S-702.5
  Scenario: AC5: Must return error if the request contains a non extant record for the Given ID
    When a request is prepared with appropriate values
    And the request [intends to update a previously created payment order]
    And the request [contains a non extant record for the Given ID]
    And it is submitted to call the [updateCasePaymentOrder] operation of [Case Payment Orders Microservice]
    Then a negative response is received
    And the response has all other details as expected
    And a call [to verify that the Case payment Order has not been updated] will get the expected response as in [getCasePaymentOrder_cpo_not_updated].
