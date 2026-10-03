@US18 @analytics @wip
Feature: Facility stress peaks visualization
  As a business owner
  I want to see the hours where machine occupancy exceeds 90%
  So that I can identify daily bottlenecks

  Scenario: Highlighting critical time blocks
    Given the gym has hourly occupancy data
    When the administrator opens the reports of their branch
    Then the hours with occupancy above 90% are highlighted as stress peaks

  Scenario: Week over week comparison
    Given the business owner is viewing the stress peaks of the current week
    When the business owner selects the week over week comparison
    Then the system returns the occupancy trend of the current and previous week
