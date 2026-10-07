@US44 @monitoring
Feature: Motion and camera sensor monitoring
  As an administrator
  I want to see the connection status of the motion and camera sensors installed in my gym
  So that I detect disconnected sensors before they affect the telemetry service

  Scenario: Sensor status overview
    Given the gym has registered motion sensors and camera sensors
    When the administrator opens the IoT monitoring module
    Then each sensor is listed with its status "Connected" or "Disconnected"
    And each sensor shows its last received signal

  Scenario: Automatic disconnection detection
    Given a motion sensor stopped sending signals for longer than the configured period
    When the connectivity check evaluates the sensors' status
    Then the motion sensor is marked as "Disconnected"
    And an alert is generated in the alert center
