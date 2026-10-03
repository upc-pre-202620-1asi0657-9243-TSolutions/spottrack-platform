@US29 @analytics
Feature: Predictive purchase and investment analytics
  As a business owner
  I want usage-based reports to project investments and simulate ROI
  So that I decide which equipment to acquire or discard

  Scenario: Purchase suggestion due to saturation
    Given an equipment stays above the maximum occupancy limit
    When the manager opens the investment projection
    Then the system recommends acquiring an additional unit to cover the unmet demand

  Scenario: Return on investment calculation
    Given the manager simulates the purchase of a new equipment
    When the manager enters an estimated acquisition cost of 8000.00
    Then the system returns the estimated payback time in months
