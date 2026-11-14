@US20 @monitoring
Feature: Edge IoT hardware status monitoring
  As an administrator
  I want to review the network health and the status of the IoT nodes
  So that I can detect if a sensor has been disconnected

  Background:
    Given the equipment "Rowing Machine 01" is registered in the gym
    And the equipment has a motion sensor registered

  Scenario: Connection loss detection
    Given the motion sensor of "Rowing Machine 01" is "Online"
    And the administrator is auditing the network health
    When the central system loses communication with the motion sensor
    Then the motion sensor is marked as "Disconnected"
    And a disconnection alert is shown to the administrator

  Scenario: Successful reconnection
    Given the motion sensor of "Rowing Machine 01" is "Disconnected"
    When the IoT node restores its network connection
    Then the motion sensor is marked as "Online"

  Scenario: Rejected sensor signal is recorded
    Given the motion sensor of "Rowing Machine 01" uses the "default" payload profile
    When the motion sensor publishes a payload that no translator can interpret
    Then the signal is discarded
    And the rejection is logged with the sensor's device id and payload profile
    And the rejected signals counter of the "default" profile goes up in the network health
