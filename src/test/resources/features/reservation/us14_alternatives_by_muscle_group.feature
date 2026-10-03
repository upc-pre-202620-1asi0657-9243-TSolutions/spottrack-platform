@US14 @reservation @wip
Feature: Alternatives filtered by muscle group
  As a frequent client
  I want suggested routines to respect my target muscle group and skip broken machines
  So that I get truly useful options

  Scenario: Respecting the training focus
    Given the client is doing a routine focused on "Chest"
    When the client requests alternatives for the current routine
    Then the system only suggests exercises for the "Chest" muscle group

  Scenario: Excluding broken machines
    Given there is a similar equipment with an open technical ticket
    When the client requests a replacement recommendation
    Then the system does not suggest equipment with an open technical ticket
