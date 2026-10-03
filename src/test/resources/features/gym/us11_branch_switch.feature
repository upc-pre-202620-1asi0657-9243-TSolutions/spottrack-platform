@US11 @gym
Feature: Branch switch to check occupancy
  As a frequent client
  I want to select other branches in the app
  So that I can check the layout and occupancy of alternate branches before leaving home

  Scenario: Checking an alternate branch with a multi-branch membership
    Given the client has a multi-branch membership
    When the client selects another branch in the branch selector
    Then the system returns the layout and heatmap of the selected branch

  Scenario: Branch outside the client's plan
    Given the client has a single-branch plan
    When the client selects a branch that is not included in their plan
    Then the system blocks access to that branch
    And the system suggests upgrading the membership
