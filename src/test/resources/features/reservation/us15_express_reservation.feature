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
