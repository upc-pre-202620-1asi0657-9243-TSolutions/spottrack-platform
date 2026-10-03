@US27 @maintenance @wip
Feature: Automated spare parts stock management
  As an administrator
  I want to control the inventory of key parts and receive restock alerts
  So that technicians always have supplies available

  Scenario: Inventory deduction
    Given a technician repairs an equipment using a specific spare part
    When the technician registers the part usage in the ticket
    Then the system deducts one unit from the branch spare parts inventory

  Scenario: Restock alert
    Given a critical spare part inventory is deducted due to usage
    When the inventory reaches the minimum safety level
    Then the system sends a restock alert to the purchasing department
