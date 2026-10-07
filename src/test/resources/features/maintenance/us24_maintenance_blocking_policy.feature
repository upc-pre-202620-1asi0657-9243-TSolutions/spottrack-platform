@US24 @maintenance @wip
Feature: Maintenance scheduling policy in off-peak hours
  As an administrator
  I want the system to prevent scheduling preventive maintenance during high demand time slots
  So that equipment remains available during peak attendance

  Scenario: Scheduling attempt during peak hours is blocked
    Given the administrator selects a high demand time slot for maintenance
    When the administrator confirms the scheduling
    Then the system rejects the scheduling with a high demand warning
    And the system suggests alternative low demand time slots

  Scenario: Successful scheduling during off-peak hours
    Given the administrator selects a low demand time slot for the maintenance of an equipment
    When the administrator confirms the scheduling
    Then the system registers the maintenance block in the calendar
    And the equipment is marked as unavailable during that interval
