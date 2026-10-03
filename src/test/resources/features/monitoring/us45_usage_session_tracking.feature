@US45 @monitoring
Feature: Automatic usage session tracking via IoT sensor
  As a client
  I want the system to automatically register the start and end of my machine usage session
  So that usage statistics are fed without me starting or marking anything manually

  Background:
    Given the equipment "Stationary Bike 03" has an active motion sensor

  Scenario: Automatic session start
    Given the client starts using "Stationary Bike 03"
    When the motion sensor detects continuous activity on the equipment
    Then the system creates a usage session for the equipment
    And the usage session is marked as active

  Scenario: Automatic session end due to inactivity
    Given a usage session is active for "Stationary Bike 03"
    And the motion sensor stops detecting activity
    When the configured inactivity time limit elapses
    Then the system ends the usage session
    And the session duration is calculated and registered for analytics
