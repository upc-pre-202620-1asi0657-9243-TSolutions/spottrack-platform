@US23 @maintenance
Feature: Restoration notification to users
  As an administrator
  I want the system to notify clients when a reported equipment is repaired
  So that their perception of the service improves

  Scenario: Repaired equipment
    Given a technician finished the physical repair of a reported equipment
    When the technician changes the ticket status to "Resolved"
    Then the equipment is shown as "Free" on the heatmap
    And the affected clients are notified

  @wip
  Scenario: Ticket reopening
    Given a supposedly repaired equipment presents an operational failure again
    When the administrator marks the equipment as broken again
    Then the system reopens the original ticket
    And the issue is labeled with "Urgent" priority
