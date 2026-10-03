@US48 @maintenance
Feature: Maintenance technicians management
  As an administrator
  I want to register technicians in the Maintenance module and assign them to open tickets
  So that I distribute the repair workload among the available staff

  Background:
    Given the administrator is signed in to the Maintenance module

  Scenario: Technician registration
    When the administrator registers a new technician
    Then the technician is registered
    And the technician is available for ticket assignment

  Scenario: Assigning a technician to a ticket
    Given there is a technical ticket in "Pending" status and an available technician
    When the administrator assigns the technician to the ticket
    Then the technician is linked to the ticket
    And the ticket status changes to "Assigned"

  Scenario: Assigning a ticket that is already assigned
    Given there is a technical ticket already assigned to a technician
    When the administrator tries to assign another technician to the ticket
    Then the system rejects the assignment because the ticket is already assigned
