@US41 @monitoring
Feature: Structured equipment anomaly report
  As a client
  I want to report an equipment anomaly indicating the associated reservation, the zone and a description
  So that I formally alert the gym about an issue detected during my workout

  Scenario: Successful anomaly report
    Given the client has an active reservation for the equipment "Leg Press 01"
    When the client reports an anomaly with the reservation, the equipment, the zone and the description "Cable is frayed"
    Then the system registers the anomaly report
    And the report is associated with the client's reservation
