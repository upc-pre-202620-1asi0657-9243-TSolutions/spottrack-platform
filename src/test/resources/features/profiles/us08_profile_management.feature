@US08 @profiles
Feature: Profile and preferences management
  As a user
  I want to update my personal information
  So that I keep my data up to date

  Scenario: Personal data update
    Given the user is signed in and has a profile
    When the user changes their phone number and saves the profile
    Then the system stores the updated phone number
    And a success confirmation is returned

  # Scenario 2 (language switch) is a user interface behavior validated in the frontend test suite
