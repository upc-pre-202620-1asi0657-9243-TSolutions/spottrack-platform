@US12 @monitoring @wip
Feature: Availability push notifications
  As a frequent client
  I want to activate a notification bell on a machine
  So that I receive an alert on my phone when the machine I am waiting for is released

  Scenario: Free machine alert
    Given the client activated the notification bell on an occupied equipment
    When the IoT hardware registers that the previous user left the equipment
    Then the system sends a push notification to the client indicating the equipment is free

  Scenario: Automatic alert discard
    Given the system notified the client that the equipment is free
    When another user physically occupies the equipment before the notified client arrives
    Then the system cancels the tracking
    And the client is notified that the equipment is occupied again
