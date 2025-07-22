#=================================
@F-700 @Smoke
Feature: Healthcheck Operation
#=================================

  Background:
    Given an appropriate test context as detailed in the test data source

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  @S-700 @Smoke
  Scenario: must return a successful response from the Case Payment Orders Healthcheck Operation

     When a request is prepared with appropriate values,
      And it is submitted to call the [Healthcheck] operation of [Case Payment Orders API],

     Then a positive response is received,
      And the response [has the 200 OK code],
      And the response has all other details as expected.

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
