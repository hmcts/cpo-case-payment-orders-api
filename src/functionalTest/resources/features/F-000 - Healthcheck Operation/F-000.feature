
@F-000 @Smoke
Feature: Healthcheck Operation


  @S-000 @Smoke
  Scenario: must return a successful response from the Case Payment Orders Healthcheck Operation
    Given an appropriate test context as detailed in the test data source

    When a request is prepared with appropriate values,
    And it is submitted to call the [Healthcheck] operation of [Case Payment Orders API],
    
    Then a positive response is received,
    And the response [has the 200 OK code],
    And the response has all other details as expected.
