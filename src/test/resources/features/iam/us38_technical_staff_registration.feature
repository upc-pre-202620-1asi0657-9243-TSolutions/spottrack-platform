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

  Scenario: Technician account is created with the technician role only
    Given the administrator is signed in to the staff management module
    When the administrator registers a technician with email, password, first name and last name
    Then the account is created with the role technician and is linked to the maintenance roster of that administrator
    And the response includes the technician identifier to assign tickets

  Scenario: Registration with an email that is already in use
    Given an account already exists with the email "tecnico@gym.com"
    When the administrator registers a technician with the email "tecnico@gym.com"
    Then the system rejects the operation with a conflict
    And no technician is added to the maintenance roster

  Scenario Outline: Registration with invalid data
    Given the administrator is signed in to the staff management module
    When the administrator registers a technician with email "<email>" and password "<password>"
    Then the system rejects the operation with a validation error

    Examples:
      | email           | password  |
      | not-an-email    | secret123 |
      |                 | secret123 |
      | tecnico@gym.com | short     |

  Scenario Outline: Public or generic sign up cannot create technicians
    Given a caller tries to create an account with the role "ROLE_TECHNICIAN" through "<endpoint>"
    When the request is processed
    Then the system rejects the operation because the technician role is restricted

    Examples:
      | endpoint                              |
      | /api/v1/authentication/sign-up-staff  |
      | /api/v1/users/sign-up                 |

  Scenario: Technician sees only the tickets assigned to them
    Given a technician account exists and one ticket is assigned to it
    And another ticket is assigned to a different technician
    When the technician requests the tickets assigned to them
    Then only the ticket assigned to that technician is returned

  Scenario: Technician manages their own ticket without public registration
    Given a ticket is assigned to the technician
    When the technician updates the status or completes that ticket
    Then the system applies the change

  Scenario: Technician tries to manage a ticket that is not theirs
    Given a ticket is assigned to a different technician
    When the technician tries to update the status of that ticket
    Then the system rejects the operation due to insufficient permissions

  Scenario: Administrator cannot assign a technician from another administrator
    Given a technician belongs to another administrator
    When the administrator tries to assign a ticket to that technician
    Then the system rejects the operation due to insufficient permissions
