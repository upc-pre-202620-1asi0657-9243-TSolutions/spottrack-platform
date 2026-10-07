@US25 @gym
Feature: Physical asset management and registration
  As an administrator
  I want to register or decommission equipment linked to an IoT sensor
  So that I keep the digital inventory and heatmap up to date

  Background:
    Given the administrator is signed in and owns a gym with a registered zone

  Scenario: Registering new equipment
    Given the gym acquires a new physical equipment
    When the administrator registers the equipment linked to an IoT sensor
    Then the equipment appears in the gym's digital inventory
    And the equipment is shown on the branch heatmap

  Scenario: Decommissioning obsolete equipment
    Given an old equipment must be permanently removed from the facilities
    When the administrator decommissions the equipment
    Then the system unlinks its IoT sensor and archives its history
    And the equipment is removed from the public heatmap

  Scenario: Decommissioned equipment cannot change status
    Given an equipment has been decommissioned
    When the administrator tries to change its status to "Available"
    Then the system rejects the change because decommissioning is final
