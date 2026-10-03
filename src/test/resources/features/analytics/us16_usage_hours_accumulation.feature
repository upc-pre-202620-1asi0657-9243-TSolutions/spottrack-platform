@US16 @analytics
Feature: Automatic accumulation of usage hours
  As an administrator
  I want to see the sum of real usage hours of the machines
  So that I understand the real demand without supervising the facility

  Background:
    Given the administrator is signed in and the gym has equipment with registered usage sessions

  Scenario: Usage hours and operational impact
    When the system processes the sensor data of each equipment
    Then the activity report shows the accumulated usage hours per equipment
    And the activity report shows the calculated monetary loss per equipment

  Scenario: Filtering by date range
    When the administrator filters the usage hours by a specific period
    Then the total usage hours only include sessions within the selected period
