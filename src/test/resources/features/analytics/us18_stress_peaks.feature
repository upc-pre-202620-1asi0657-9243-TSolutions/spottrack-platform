@US18 @analytics
Feature: Facility stress peaks visualization
  As a business owner
  I want to see the hours where machine occupancy exceeds 90%
  So that I can identify daily bottlenecks and plan staff and maintenance

  Scenario: Highlighting critical time blocks
    Given the gym has hourly occupancy data
    When the administrator opens the reports of their branch
    Then the hours with occupancy above 90% are highlighted as stress peaks

  Scenario: Occupancy distribution by day and hour
    Given the gym has occupancy data for several days of the week
    When the administrator opens the reports of their branch
    Then the system shows the occupancy of every opening hour on every weekday
    And the time slots with occupancy above 90% are highlighted as stress peaks
