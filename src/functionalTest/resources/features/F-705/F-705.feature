#================================================
@F-705
Feature: Verify microservice based authorisation
#================================================


Background:
  Given an appropriate test context as detailed in the test data source

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
# CPO-10 / AC-1
@S-705.1
Scenario: Must successfully allow access of the CreateCasePaymentOrder API for a CRUD whitelisted service

  Given a user with [an active profile in CCD]
    And [a new Case-Payment-Order microservice has been established] in the context,
    And [a CRUD whitelist exists for the invoking service] in the context of the scenario,

  When a request is prepared with appropriate values,
    And the request [intends to Create a case payment order],
    And the request [contains all the mandatory parameters],
    And it is submitted to call the [createCasePaymentOrder] operation of [Case Payment Orders Microservice],

  Then a positive response is received
    And the response has all the details as expected.

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
# CPO-10 / AC-2
@S-705.2
Scenario: Must successfully allow access of the UpdateCasePaymentOrder API for a CRUD whitelisted service

  Given a user with [an active profile in CCD]
    And [a new Case-Payment-Order microservice has been established] in the context,
    And a successful call [to create a case payment order for the created case] as in [Prerequisite_Create_CPO],
    And [a CRUD whitelist exists for the invoking service] in the context of the scenario,

  When a request is prepared with appropriate values,
    And the request [intends to update a new payment order],
    And the request [contains all the mandatory parameters],
    And it is submitted to call the [updateCasePaymentOrder] operation of [Case Payment Orders Microservice],

  Then a positive response is received
    And the response has all the details as expected.

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
# CPO-10 / AC-3
@S-705.3
Scenario: Must successfully allow access of the DeleteCasePaymentOrder API for a CRUD whitelisted service

  Given a user with [an active profile in CCD]
    And [a new Case-Payment-Order microservice has been established] in the context,
    And a successful call [to create a case payment order for the created case] as in [Prerequisite_Create_CPO],
    And [a CRUD whitelist exists for the invoking service] in the context of the scenario,

  When a request is prepared with appropriate values,
    And the request [intends to delete a new payment order],
    And the request [contains all the mandatory parameters],
    And it is submitted to call the [deleteCasePaymentOrder] operation of [Case Payment Orders Microservice],

  Then a positive response is received
    And the response has all the details as expected.

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
# CPO-10 / AC-4
@S-705.4
Scenario: Must refuse access of the CreateCasePaymentOrder API for a Non-CRUD whitelisted service

  Given a user with [an active profile in CCD]
    And [a new Case-Payment-Order microservice has been established] in the context,
    And [a CRUD whitelist doesn't exist for the invoking service] in the context of the scenario,

  When a request is prepared with appropriate values,
    And the request [intends to Create a case payment order],
    And the request [contains all the mandatory parameters],
    And it is submitted to call the [createCasePaymentOrder] operation of [Case Payment Orders Microservice],

  Then a negative response is received,
    And the response has all the details as expected.

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
# CPO-10 / AC-5
@S-705.5
Scenario: Must refuse access of the UpdateCasePaymentOrder API for a CRUD whitelisted service

  Given a user with [an active profile in CCD]
    And [a new Case-Payment-Order microservice has been established] in the context,
    And a successful call [to create a case payment order for the created case] as in [Prerequisite_Create_CPO],
    And [a CRUD whitelist doesn't exist for the invoking service] in the context of the scenario,

  When a request is prepared with appropriate values,
    And the request [intends to update a previously created payment order],
    And the request [contains all the mandatory parameters],
    And it is submitted to call the [updateCasePaymentOrder] operation of [Case Payment Orders Microservice],

  Then a negative response is received,
    And the response has all the details as expected.

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
# CPO-10 / AC-6
@S-705.6
Scenario: Must refuse access of the DeleteCasePaymentOrder API for a Non-CRUD whitelisted service

  Given a user with [an active profile in CCD]
    And [a new Case-Payment-Order microservice has been established] in the context,
    And a successful call [to create a case payment order for the created case] as in [Prerequisite_Create_CPO],
    And [a CRUD whitelist doesn't exist for the invoking service] in the context of the scenario,

  When a request is prepared with appropriate values,
    And the request [intends to delete a payment order],
    And the request [contains all the mandatory parameters],
    And it is submitted to call the [deleteCasePaymentOrder] operation of [Case Payment Orders Microservice],

  Then a negative response is received,
    And the response has all the details as expected.

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
# CPO-10 / AC-7
@S-705.7
Scenario: Must successfully allow READ access of the GetCasePaymentOrder API for a R permissions service (ExUI)

  Given a user with [an active profile in CCD]
    And [a new Case-Payment-Order microservice has been established] in the context,
    And a successful call [to create a case payment order for the created case] as in [Prerequisite_Create_CPO],
    And [a Read permission whitelist exists for the invoking service] in the context of the scenario,

  When a request is prepared with appropriate values,
    And the request [intends to get payment orders],
    And the request [contains all the mandatory parameters],
    And it is submitted to call the [getCasePaymentOrder] operation of [Case Payment Orders Microservice],

  Then a positive response is received
    And the response has all the details as expected.
