@US09 @monitoring
Feature: Live heatmap visualization
  As a frequent client
  I want to see machine availability in real time (green/red)
  So that I avoid crowds and do not waste time looking for available machines

  Background:
    Given the client is signed in and associated with a gym

  Scenario: Availability indicators
    Given the gym has the equipment "Bench Press 01" free and "Treadmill 02" occupied
    When the client opens the heatmap of their branch
    Then "Bench Press 01" is shown as "Free"
    And "Treadmill 02" is shown as "Occupied"

  Scenario: Real-time update
    Given the client is viewing the heatmap of their branch
    And "Treadmill 02" is shown as "Occupied"
    When the IoT system signals that "Treadmill 02" has been vacated
    Then the client receives the update of "Treadmill 02" as "Free" without reloading the heatmap
