@US35 @profiles
Feature: Client association to a gym
  As a client
  I want to associate with one or more available gyms
  So that I can access the heatmap and features of that location

  Background:
    Given the client is signed in
    And the client's DNI is authorized in the whitelist of the selected gym

  Scenario: First gym association
    Given the client is not associated with any gym
    When the client selects an available gym and confirms the association
    Then the system links the client to the gym
    And the gym becomes the client's active gym

  Scenario: Additional gym association
    Given the client is already associated with a gym
    When the client selects a second available gym
    Then the system adds the new association
    And the client's associations list shows both gyms
