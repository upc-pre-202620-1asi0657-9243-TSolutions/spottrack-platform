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

  Scenario: Starting a session of another client's routine is rejected
    Given a routine that belongs to another client
    When the client tries to start a session of that routine
    Then the request is rejected as forbidden

  Scenario: Starting a second session of a routine already in progress is rejected
    Given the client has an active session of the routine
    When the client tries to start another session of the same routine
    Then the request is rejected as a conflict

  Scenario: Marking an exercise block that does not belong to the routine is rejected
    Given the client has an active session of the routine
    When the client marks an exercise block from a different routine
    Then the request is rejected as invalid

  Scenario: Marking an already completed exercise block keeps it completed
    Given the client has an active session with the first exercise block completed
    When the client marks the first exercise block as completed again
    Then the first exercise block remains completed

  Scenario: Exercise blocks cannot be marked in a finished session
    Given the client has a completed session
    When the client tries to mark an exercise block as completed
    Then the request is rejected as a conflict

  Scenario: Another client cannot mark blocks of the session
    Given a session that belongs to another client
    When the client tries to mark an exercise block of that session
    Then the request is rejected as forbidden
