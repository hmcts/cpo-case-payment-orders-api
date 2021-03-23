#================================================
@F-705
Feature: Verify microservice based authorisation
#================================================


Background:
  Given an appropriate test context as detailed in the test data source

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
# CPO-10 / AC-1
@S-705.1
Scenario: Must successfully allow access of the AddCasePaymentOrder API for a CRUD whitelisted service

  Given [a new Case-Payment-Order microservice has been established],
    And [a CRUD whitelist exists for the invoking service] in the context of the scenario,

  When a request is prepared with appropriate values,
    And the request [intends to create a new payment order],
    And the request [contains all the mandatory parameters],
    And it is submitted to call the [AddCasePaymentOrder] operation of [Case Payment Order API],

  Then a positive response is received
    And the response has all the details as expected.

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
# CPO-10 / AC-2
@S-705.2
Scenario: Must successfully allow access of the UpdateCasePaymentOrder API for a CRUD whitelisted service

  Given [a new Case-Payment-Order microservice has been established],
    And a successful call [to create a new Case Payment Order] as in [F-705_Prerequisite_Case_Payment_Order_Creation],
    And [a CRUD whitelist exists for the invoking service] in the context of the scenario,

  When a request is prepared with appropriate values,
    And the request [intends to update a new payment order],
    And the request [contains all the mandatory parameters],
    And it is submitted to call the [UpdateCasePaymentOrder] operation of [Case Payment Order API],

  Then a positive response is received
    And the response has all the details as expected.

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
# CPO-10 / AC-3
@S-705.3
Scenario: Must successfully allow access of the DeleteCasePaymentOrder API for a CRUD whitelisted service

  Given [a new Case-Payment-Order microservice has been established],
    And a successful call [to create a new Case Payment Order] as in [F-705_Prerequisite_Case_Payment_Order_Creation],
    And [a CRUD whitelist exists for the invoking service] in the context of the scenario,

  When a request is prepared with appropriate values,
    And the request [intends to delete a new payment order],
    And the request [contains all the mandatory parameters],
    And it is submitted to call the [DeleteCasePaymentOrder] operation of [Case Payment Order API],

  Then a positive response is received
    And the response has all the details as expected.

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
# CPO-10 / AC-4
@S-705.4
Scenario: Must refuse access of the AddCasePaymentOrder API for a Non-CRUD whitelisted service

  Given [a new Case-Payment-Order microservice has been established],
    And [a CRUD whitelist doesn't exists for the invoking service] in the context of the scenario,

  When a request is prepared with appropriate values,
    And the request [intends to create a new payment order],
    And the request [contains all the mandatory parameters],
    And it is submitted to call the [AddCasePaymentOrder] operation of [Case Payment Order API],

  Then a negative response is received,
    And the response has all the details as expected.

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
# CPO-10 / AC-5
@S-705.5
Scenario: Must refuse access of the UpdateCasePaymentOrder API for a CRUD whitelisted service

  Given [a new Case-Payment-Order microservice has been established],
    And a successful call [to create a new Case Payment Order] as in [F-705_Prerequisite_Case_Payment_Order_Creation],
    And [a CRUD whitelist doesn't exists for the invoking service] in the context of the scenario,

  When a request is prepared with appropriate values,
    And the request [intends to update a payment order],
    And the request [contains all the mandatory parameters],
    And it is submitted to call the [UpdateCasePaymentOrder] operation of [Case Payment Order API],

  Then a negative response is received,
    And the response has all the details as expected.

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
# CPO-10 / AC-6
@S-705.6
Scenario: Must refuse access of the DeleteCasePaymentOrder API for a Non-CRUD whitelisted service

  Given [a new Case-Payment-Order microservice has been established],
    And a successful call [to create a new Case Payment Order] as in [F-705_Prerequisite_Case_Payment_Order_Creation],
    And [a CRUD whitelist doesn't exists for the invoking service] in the context of the scenario,

  When a request is prepared with appropriate values,
    And the request [intends to delete a payment order],
    And the request [contains all the mandatory parameters],
    And it is submitted to call the [DeleteCasePaymentOrder] operation of [Case Payment Order API],

  Then a negative response is received,
    And the response has all the details as expected.

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
# CPO-10 / AC-7
@S-705.7
Scenario: Must successfully allow READ access of the GetCasePaymentOrder API for a R permissions service (ExUI)

  Given [a new Case-Payment-Order microservice has been established],
    And a successful call [to create a new Case Payment Order] as in [F-705_Prerequisite_Case_Payment_Order_Creation],
    And [a Read permission whitelist exists for the invoking service] in the context of the scenario,

  When a request is prepared with appropriate values,
    And the request [intends to get payment orders],
    And the request [contains all the mandatory parameters],
    And it is submitted to call the [GetCasePaymentOrder] operation of [Case Payment Order API],

  Then a positive response is received
    And the response has all the details as expected.
