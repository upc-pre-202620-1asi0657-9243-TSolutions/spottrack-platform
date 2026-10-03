@US40 @reservation
Feature: QR code check-in to activate an express reservation
  As a client
  I want to scan the physical QR code of a machine with my phone camera or by uploading a photo
  So that I activate my express reservation without searching for the equipment manually

  Scenario Outline: Check-in by QR code
    Given the client opens the QR scanner from the reservation screen
    And the equipment "Cable Crossover 01" is "Free"
    When the client reads the QR code of "Cable Crossover 01" using the "<source>"
    Then the system identifies the equipment "Cable Crossover 01"
    And the express reservation for "Cable Crossover 01" is activated

    Examples:
      | source         |
      | phone camera   |
      | uploaded photo |
