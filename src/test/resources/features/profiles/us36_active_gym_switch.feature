@US36 @profiles
Feature: Switching between associated gyms
  As a client associated with more than one gym
  I want to change my active gym
  So that I can check availability and reserve equipment at my preferred location

  Scenario: Successful active gym change
    Given the client is associated with the gyms "Spot Gym Miraflores" and "Spot Gym San Isidro"
    And "Spot Gym Miraflores" is the client's active gym
    When the client selects "Spot Gym San Isidro" as the active gym
    Then the system updates the client's active gym to "Spot Gym San Isidro"
    And the heatmap shows the equipment of "Spot Gym San Isidro"
