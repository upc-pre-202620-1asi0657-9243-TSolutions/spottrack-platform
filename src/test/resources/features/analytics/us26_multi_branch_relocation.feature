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

  Scenario: Comparing branch demand
    Given a gym with several branches and recorded usage in the selected period
    When the administrator requests the relocation insights of the gym
    Then each branch shows its equipment, total minutes of use and utilization rate
    And each branch is classified as low demand, balanced or saturated

  Scenario: Suggesting machines to move to a saturated branch
    Given one branch has low demand and another is saturated
    When the administrator requests the relocation insights of the gym
    Then the least-used machines of the low demand branch are suggested for the saturated branch
    And each suggestion explains the figures behind it

  Scenario: A gym with a single branch
    Given the gym has only one branch
    When the administrator requests the relocation insights of the gym
    Then the branch is classified but no relocation is suggested

  Scenario: A branch never gives away more than half of its machines
    Given a low demand branch whose machines are all under-used
    When the administrator requests the relocation insights of the gym
    Then at most half of the machines of that branch are suggested

  Scenario: Only the owner administrator can see the insights
    Given a user who is not the administrator that owns the gym
    When the user requests the relocation insights of that gym
    Then the request is rejected as forbidden

  Scenario: Invalid period
    Given a period that ends before it starts or spans more than 366 days
    When the administrator requests the relocation insights of the gym
    Then the request is rejected as a bad request

  # Executing a suggested transfer reuses PATCH /api/v1/equipments/{equipmentId}/relocate (see "Executing the transfer").
  # Usage is attributed to the branch where a machine is now, so the history of a relocated machine moves with it.
  # There is no Cucumber runner in this project; the scenarios are covered by unit tests.
