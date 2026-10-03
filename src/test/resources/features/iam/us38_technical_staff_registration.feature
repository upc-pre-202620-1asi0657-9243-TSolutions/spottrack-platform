@US38 @iam
Feature: Technical staff registration by the administrator
  As an administrator
  I want to register technical staff accounts from the admin panel
  So that I can delegate maintenance tickets without exposing a public sign up for that role

  Scenario: Successful technician account creation
    Given the administrator is signed in to the staff management module
    When the administrator registers a new technician with their personal data and role
    Then the system creates the technician account
    And the technician is able to sign in with the assigned permissions

  Scenario: Non-administrator tries to register technical staff
    Given a client is signed in
    When the client tries to register a technician account
    Then the system rejects the operation due to insufficient permissions
