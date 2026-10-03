@US17 @analytics
Feature: Underused equipment identification
  As an administrator
  I want the system to highlight which machines have an exceptionally low usage rate
  So that I can evaluate their relocation or removal

  Scenario: Operational inefficiency report
    Given the gym has equipment with occupancy statistics
    And the base usage rate parameter is configured
    When the system processes the occupancy statistics
    Then the equipment with a usage rate below the base parameter is highlighted as underused

  # Scenario 2 (CSV export) is validated in the frontend test suite
