@US21 @gym
Feature: Predictive maintenance alert
  As an operations manager
  I want to receive automatic alerts when equipment exceeds its safe usage hours
  So that I can perform preventive maintenance before it fails

  Scenario: Alert triggered when the threshold is reached
    Given the system continuously accumulates the operating hours of the equipment
    And the equipment "Treadmill 01" has a configured maintenance threshold
    When the telemetry system detects that the equipment exceeds its threshold
    Then the system generates a "Preventive Maintenance Required" alert for "Treadmill 01"

  Scenario: Configuring the maintenance threshold for new equipment
    Given a new equipment is registered in the digital inventory
    When the manager defines a future maintenance threshold for that equipment
    Then the system stores the threshold for that specific equipment

  Scenario: Rejecting a maintenance threshold in the past
    Given an equipment is registered in the digital inventory
    When the manager defines a maintenance threshold with a past date
    Then the system rejects the threshold with a validation error
