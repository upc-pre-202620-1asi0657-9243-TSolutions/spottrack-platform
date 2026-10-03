@US22 @maintenance
Feature: Technical ticket management in the Maintenance Center
  As an administrator
  I want to view and manage technical tickets from the Maintenance Center
  So that I start attending equipment with pending maintenance and follow its progress until closure

  Background:
    Given the administrator is signed in to the Maintenance Center

  Scenario: Starting a pending ticket
    Given there is a technical ticket in "Pending" status
    When the administrator starts the ticket
    Then the ticket status changes to "In Progress"
    And the start of the attention is registered

  Scenario: Closing a ticket in progress
    Given there is a technical ticket in "In Progress" status with an assigned technician
    When the technician completes the repair
    Then the ticket is marked as "Completed"
    And the equipment is released back to the public heatmap

  Scenario: Manual technical ticket creation
    Given the administrator detects an issue on "Elliptical 02" that did not generate an automatic alert
    When the administrator creates a ticket for "Elliptical 02" with a description and a "High" priority
    Then the ticket is registered in "Pending" status in the Maintenance Center
    And "Elliptical 02" is shown as "Under Maintenance" on the heatmap
