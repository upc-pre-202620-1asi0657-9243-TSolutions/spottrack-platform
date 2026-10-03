@US46 @shared
Feature: Unified alert center
  As a platform user
  I want to view, resolve and clear in one place the alerts from maintenance, IoT sensors, anomalies and expired reservations
  So that I act quickly on critical events

  Background:
    Given the user is signed in

  Scenario: Resolving an alert
    Given the user has an active alert in the alert center
    When the user marks the alert as resolved
    Then the alert status changes to "Resolved"
    And the alert is no longer counted as pending

  @wip
  Scenario: Clearing all resolved alerts
    Given the user has multiple resolved alerts
    When the user clears all alerts
    Then the resolved alerts for the user's role are removed from the alert center
