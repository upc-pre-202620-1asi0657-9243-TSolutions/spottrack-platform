@US11 @gym
Feature: Branch switch to check occupancy
  As a frequent client
  I want to select other branches in the app
  So that I can check the layout and occupancy of alternate branches before leaving home

  Scenario: Checking an alternate gym the client is associated with
    Given the client is associated with the gyms "Spot Gym Miraflores" and "Spot Gym San Isidro"
    And "Spot Gym Miraflores" is the client's active gym
    And the client's DNI is still in the whitelist of "Spot Gym San Isidro"
    When the client selects "Spot Gym San Isidro" in the branch selector
    Then the system returns the branches, zones and equipment of "Spot Gym San Isidro"
    And the system returns the usage heatmap of "Spot Gym San Isidro"
    And "Spot Gym Miraflores" remains the client's active gym

  Scenario: Gym the client is not associated with
    Given the client is associated only with "Spot Gym Miraflores"
    When the client selects "Spot Gym San Isidro", which is not in their associations
    Then the system blocks access to that gym with a 403 FORBIDDEN error
