@F-701
Feature: blah


  @S-701.1
  Scenario: Successfully allow the creation of a case payment order in the Case Payment Order database
    Given an appropriate test context as detailed in the test data source,
    Given a user with [an active profile in CCD],
    When a request is prepared with appropriate values
    And the request [intends to Create a case payment order]
    And the request [contains all the mandatory parameters]
    And it is submitted to call the [createCasePaymentOrder] operation of [Case Payment Orders Microservice],
    Then a positive response is received
    And the response [contains a 200 success OK code]
    And the response [contains a valid UUID of the case order record]
    And the response has all other details as expected
#    And a call [to verify that a Case payment Order has been created] will get the expected response as in [getCasePaymentOrder_cpo_created].

  Scenario Outline: AC2- Must return error if one or more of the mandatory parameters have not been provided (Please refer to the mandatory parameter list in the description)
    When a request is prepared with appropriate values
    And the request [intends to Create a case payment order]
    And the request [does not contain one or more of the mandatory parameters]
    And the request [does not contain the <parameter> parameter]
    And it is submitted to call the [createCasePaymentOrder] operation of [Case Payment Orders Microservice],
    Then a negative response is received
    And the response has all other details as expected
#    And a call [to verify that a Case payment Order has not been created in the database] will get the expected response as in [getCasePaymentOrder_cpo_not_created].

  @S-701.2.1
  Examples:
    | parameter        |
    | effective_from   |

  @S-701.2.2
  Examples:
    | parameter        |
    | case_id          |

  @S-701.2.3
  Examples:
    | parameter        |
    | case_type_id     |

  @S-701.2.4
  Examples:
    | parameter        |
    | action           |

  @S-701.2.5
  Examples:
    | parameter        |
    | responsible_party|

  @S-701.2.6
  Examples:
    | parameter        |
    | order_reference  |




  Scenario Outline: AC3 - Must return an error if the request contains an invalid mandatory parameter
    When a request is prepared with appropriate values
    And the request [intends to Create a case payment order]
    And the request contains [an invalid mandatory parameter]
    And the request contains [an <parameter>]
    And it is submitted to call the [createCasePaymentOrder] operation of [Case Payment Orders Microservice],
    Then a negative response is received
    And the response has all other details as expected
#    And a call [to verify that a Case payment Order has not been created in the database] will get the expected response as in [CPO-5_getCasePaymentOrder_cpo_not_created].

    @S-701.3.1
    Examples:
      | parameter               |
      | incorrect caseid format |

    @S-701.3.2
    Examples:
      | parameter               |
      | invalid datetime format |


    @S-701.4
  Scenario: AC4a- Must return error if order_reference for a case_id is non-unique
    And a successful call [to create a case payment order for the created case] as in [Prerequisite_Create_CPO],
    When a request is prepared with appropriate values
    And the request [intends to Create a case payment order]
    And the request contains [all the mandatory parameters]
    And the request contains [the same order_reference as used in the previously created CPO for the same case id]
    And it is submitted to call the [createCasePaymentOrder] operation of [Case Payment Orders Microservice],
    Then a negative response is received
    And the response has all other details as expected
#    And a call [to verify that a Case payment Order has not been created in the database which can be accessed or queried again at a later date] will get the expected response as in [CPO-5_getCasePaymentOrder_cpo_not_created].
