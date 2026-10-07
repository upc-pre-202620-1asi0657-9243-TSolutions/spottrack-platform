@US37 @gym
Feature: Access control through a DNI whitelist
  As an administrator
  I want to keep a whitelist of DNI numbers authorized to join my gym
  So that I restrict access only to clients who belong to my business

  Background:
    Given the administrator is signed in and manages the whitelist of their gym

  Scenario: Authorizing a new DNI
    When the administrator adds the DNI "71234567" to the whitelist
    Then the system registers the DNI as authorized
    And a client with DNI "71234567" is able to associate with the gym

  Scenario: Revoking access
    Given the DNI "71234567" is currently in the gym whitelist
    When the administrator removes the DNI "71234567" from the whitelist
    Then the system revokes the authorization
    And a client with DNI "71234567" is not able to associate with the gym
