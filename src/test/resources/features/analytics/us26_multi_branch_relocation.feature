@US26 @analytics
Feature: Multi-branch relocation statistics
  As a gym chain administrator
  I want cross-branch statistics
  So that I can move machines from low demand branches to the most saturated ones

  Scenario: Transfer recommendation
    Given the system stores occupancy statistics of multiple branches
    And the equipment "Treadmill" has high demand in one branch and low demand in another
    When the system analyzes the cross-branch demand
    Then the system generates a recommendation to transfer the equipment to the saturated branch

  Scenario: Executing the transfer
    Given the manager approved the suggested transfer between branches
    When the manager relocates the equipment to a zone of the new branch
    Then the equipment is assigned to the new branch
