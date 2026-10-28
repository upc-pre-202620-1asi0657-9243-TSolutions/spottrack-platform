@US15 @reservation
Feature: Express reservation during peak hours
  As a frequent client
  I want to virtually hold a free machine for 10 minutes during peak hours
  So that I secure its use while I walk to it

  Background:
    Given the client is signed in and associated with a gym

  Scenario: Successful virtual hold
    Given it is peak time and the equipment "Squat Rack 01" is "Free"
    When the client reserves "Squat Rack 01"
    Then "Squat Rack 01" is shown as "Reserved" on the heatmap
    And a 10 minute reservation timer starts

  Scenario: Reservation expiration
    Given the client has an active reservation for "Squat Rack 01" with a 10 minute timer
    When the timer expires without the client physically occupying the equipment
    Then the system releases the reservation
    And "Squat Rack 01" is shown as "Free" on the heatmap

  Scenario: Reserving an equipment that is not free
    Given the equipment "Squat Rack 01" is "Occupied"
    When the client tries to reserve "Squat Rack 01"
    Then the system rejects the reservation

  Scenario: Reserving an equipment that is already reserved by another client
    Given the equipment "Squat Rack 01" is "Reserved" by another client
    When the client tries to reserve "Squat Rack 01"
    Then the system rejects the reservation with a conflict saying the equipment is already reserved

  Scenario: Reserving while holding another active reservation
    Given the client already has an active reservation
    When the client tries to reserve "Squat Rack 01"
    Then the system rejects the reservation with a conflict saying the client already has an active reservation

  Scenario: Reserving an equipment that is not available for use
    Given the equipment "Squat Rack 01" is in "Maintenance"
    When the client tries to reserve "Squat Rack 01"
    Then the system rejects the reservation saying the equipment is not available

  Scenario: Reserving without an active membership
    Given the client's gym membership is not active
    When the client tries to reserve "Squat Rack 01"
    Then the system denies access

  Scenario: Reserving an equipment of another gym
    Given the equipment "Squat Rack 01" belongs to a gym other than the client's active gym
    When the client tries to reserve "Squat Rack 01"
    Then the system denies access

  Scenario: Two clients race for the same free machine
    Given the equipment "Squat Rack 01" is "Free"
    When two clients reserve "Squat Rack 01" at the same time
    Then exactly one reservation is created
    And the other client is rejected with a conflict

  Scenario: Expired hold does not block a new reservation
    Given another client's hold on "Squat Rack 01" has elapsed but has not been swept yet
    When the client reserves "Squat Rack 01"
    Then the previous hold is expired
    And the client holds "Squat Rack 01" for 10 minutes

  Scenario: Checking in releases the hold into a running session
    Given the client has an active reservation for "Squat Rack 01"
    When the client checks in before the 10 minutes elapse
    Then "Squat Rack 01" is shown as "Occupied" on the heatmap
    And the reservation does not expire

  Scenario: Reservation window sent by the client is kept
    Given the equipment "Squat Rack 01" is "Free"
    When the client reserves "Squat Rack 01" with a usage window from "18:00" to "18:45"
    Then the reservation keeps the usage window from "18:00" to "18:45"
    And the hold still lasts 10 minutes

  Scenario: Reservation window is derived when not sent
    Given the equipment "Squat Rack 01" is "Free"
    When the client reserves "Squat Rack 01" without a usage window
    Then the reservation gets a usage window derived from the current time
    And the hold lasts 10 minutes
