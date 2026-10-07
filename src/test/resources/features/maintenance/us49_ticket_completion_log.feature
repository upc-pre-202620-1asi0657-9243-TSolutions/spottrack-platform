@US49 @maintenance
Feature: Completion log and cost of a technical ticket
  As a technician
  I want to register the cost and notes when closing a technical ticket
  So that I keep a history of maintenance expenses and interventions

  Background:
    Given the technician is signed in and has finished the repair of an assigned ticket

  Scenario: Registering the completion log
    When the technician completes the ticket with a cost of 150.00 and the note "Belt replaced"
    Then the system registers the completion log of the ticket
    And the ticket is closed

  Scenario Outline: Invalid cost
    When the technician tries to save the completion log with the cost "<cost>"
    Then the system rejects the completion log
    And a valid cost value is requested

    Examples:
      | cost   |
      | -50.00 |
      | abc    |
