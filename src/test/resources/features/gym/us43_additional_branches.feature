@US43 @gym
Feature: Additional branches according to the plan limit
  As an administrator with my first gym already created
  I want to add additional branches to my gym
  So that I can expand my operation respecting the branch limit of my membership plan

  Scenario: Adding a branch within the plan limit
    Given the administrator has a "Mid" plan that allows up to 3 branches
    And the gym has 2 registered branches
    When the administrator adds a third branch
    Then the system registers the branch and enables it to operate

  Scenario: Branch limit reached
    Given the administrator has a "Basic" plan that allows up to 1 branch
    And the gym has 1 registered branch
    When the administrator tries to add a second branch
    Then the system rejects the operation with a plan limit error
    And the system suggests upgrading the membership
