@US34 @gym
Feature: First gym registration by the administrator
  As a newly registered administrator
  I want to create my first gym by entering its name
  So that I can start configuring branches, zones and equipment

  Scenario: Successful gym creation
    Given the administrator signed in for the first time without registered gyms
    When the administrator creates a gym named "Spot Gym Miraflores"
    Then the system registers the gym "Spot Gym Miraflores"
    And the gym is listed as the administrator's gym

  Scenario: Gym creation with an empty name
    Given the administrator signed in for the first time without registered gyms
    When the administrator tries to create a gym without a name
    Then the system rejects the creation with a validation error
