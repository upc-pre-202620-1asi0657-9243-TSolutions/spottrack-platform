@US22 @maintenance
Feature: Technical ticket management in the Maintenance Center
  As an administrator
  I want to view and manage technical tickets from the Maintenance Center
  So that I start attending equipment with pending maintenance and follow its progress until closure

  Background:
    Given the administrator is signed in to the Maintenance Center

  Scenario: Starting an open ticket by assigning a technician
    Given there is a technical ticket in "OPEN" status
    When the administrator assigns a technician to the ticket
    Then the ticket status changes to "IN_PROGRESS"
    And the start of the attention is registered in "startedAt"

  Scenario: Starting an open ticket by changing its status
    Given there is a technical ticket in "OPEN" status with an assigned technician
    When the administrator changes the ticket status to "IN_PROGRESS"
    Then the ticket status changes to "IN_PROGRESS"
    And the start of the attention is registered in "startedAt"

  Scenario: The start of the attention is registered only once
    Given there is a technical ticket in "IN_PROGRESS" status with "startedAt" registered
    When the administrator changes the ticket status to "IN_PROGRESS" again
    Then "startedAt" keeps its original value

  Scenario: Closing a ticket in progress
    Given there is a technical ticket in "IN_PROGRESS" status with an assigned technician
    When the administrator completes the ticket
    Then the ticket status changes to "RESOLVED"
    And the equipment is released back to the public heatmap as "AVAILABLE"

  Scenario: Technical ticket creation from a maintenance request
    Given a maintenance request exists for "Elliptical 02" with a description
    When the administrator creates a ticket for the request with a "HIGH" priority
    Then the ticket is registered in "OPEN" status in the Maintenance Center
    And "Elliptical 02" is shown as "MAINTENANCE" on the heatmap
