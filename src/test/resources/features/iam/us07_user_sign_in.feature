@US07 @iam
Feature: User sign in with JWT validation
  As a user
  I want to sign in securely by generating a token
  So that I can access my corresponding dashboard or mobile application

  Scenario Outline: Successful sign in by role
    Given a registered user with the role "<role>" exists
    When the user signs in with valid credentials
    Then the system issues a valid JWT token for the user
    And the user is granted access to the "<destination>"

    Examples:
      | role        | destination          |
      | ROLE_ADMIN  | management dashboard |
      | ROLE_CLIENT | live heatmap         |

  Scenario: Sign in with invalid credentials
    Given a registered user exists
    When the user signs in with an incorrect email or password
    Then the system denies access
    And an invalid credentials error message is returned
