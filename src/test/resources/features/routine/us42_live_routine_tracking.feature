@US42 @routine
Feature: Live routine execution tracking
  As a client
  I want to start a training session of my routine and mark each exercise block as completed
  So that I keep a real record of my progress during training

  Background:
    Given the client is signed in and has a routine with 3 exercise blocks

  Scenario: Starting and progressing through a session
    When the client starts a session of the routine
    And the client marks the first exercise block as completed
    Then the first exercise block is shown as completed
    And the session keeps its progress as active

  Scenario: Completing the session
    Given the client has an active session with all exercise blocks completed
    When the client confirms the end of the session
    Then the routine session is marked as completed

  Scenario: Missed session
    Given the client has an active session that they cannot continue
    When the client marks the session as missed
    Then the routine session is registered as not completed
    And the client's routine history is not affected
