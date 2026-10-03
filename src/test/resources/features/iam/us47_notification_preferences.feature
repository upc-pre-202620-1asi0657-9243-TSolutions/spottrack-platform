@US47 @iam
Feature: Notification preferences
  As a user
  I want to configure which types of notifications I receive
  So that I avoid noise from alerts I am not interested in

  Scenario: Default notification preferences for a new user
    Given a new user has not configured notification preferences
    When the user enters the platform for the first time
    Then all notification types are enabled for the user

  Scenario Outline: Disabling a notification type
    Given the user is signed in
    When the user disables the "<type>" notification type and saves the changes
    Then the system stops generating visible "<type>" alerts for the user

    Examples:
      | type        |
      | maintenance |
      | sensors     |
      | anomalies   |
      | membership  |
