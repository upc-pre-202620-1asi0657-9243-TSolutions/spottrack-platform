@US39 @iam
Feature: Two-step password recovery
  As a user who forgot my password
  I want to request a reset with my email and verify my identity with my DNI
  So that I can recover access to my account without depending on an administrator

  Scenario Outline: Password reset request does not reveal account existence
    Given the user is on the password recovery step
    When the user submits the email "<email>"
    Then the system returns a generic message inviting the user to continue with verification

    Examples:
      | email                  |
      | registered@gym.com     |
      | not-registered@gym.com |

  Scenario: Identity verification and new password
    Given the user has requested a password reset for their registered email
    When the user submits their matching DNI together with a new password
    Then the system updates the user's password
    And the user can sign in with the new password

  Scenario: Identity verification with a non-matching DNI
    Given the user has requested a password reset for their registered email
    When the user submits a DNI that does not match the account
    Then the system rejects the password change
