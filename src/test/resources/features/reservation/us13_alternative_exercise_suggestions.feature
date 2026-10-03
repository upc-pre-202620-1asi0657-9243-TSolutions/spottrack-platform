@US13 @reservation
Feature: Alternative exercise suggestion engine
  As a gym client
  I want to receive alternative exercise recommendations when my machine is occupied
  So that I do not lose my training rhythm

  Scenario: Suggestion due to unavailability
    Given the client selects an occupied equipment in their digital routine
    And there is a similar equipment in "Free" status
    When the client searches for an alternative
    Then the system suggests a similar exercise using the free equipment

  @wip
  Scenario: No alternatives available
    Given every equipment in the weights area is occupied
    When the client searches for an alternative
    Then the system suggests a bodyweight or stretching exercise
