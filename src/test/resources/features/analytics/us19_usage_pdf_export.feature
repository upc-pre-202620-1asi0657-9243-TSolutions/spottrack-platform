@US19 @analytics
Feature: Usage charts PDF export
  As an operations manager (the administrator who owns the gym)
  I want to export the usage charts to a PDF document
  So that I can present formal performance reports

  Scenario: Exporting usage data to a formatted PDF
    Given the gym has recorded usage sessions in the selected period
    When the administrator exports the usage charts to PDF
    Then a PDF document is downloaded
    And the report shows the gym, the period, the occupancy indicators, the occupancy by hour of the day and the usage of each equipment

  Scenario: Exporting a single branch
    Given the gym has several branches with recorded usage
    When the administrator exports the usage charts of one branch to PDF
    Then the report only covers the equipment of that branch and names the branch

  Scenario: Exporting a period without usage
    Given the gym has no recorded usage sessions in the selected period
    When the administrator exports the usage charts to PDF
    Then the report is still generated and states that no usage was recorded

  Scenario: Only the owner administrator can export
    Given a user who is not the administrator that owns the gym
    When the user requests the PDF report of that gym
    Then the request is rejected as forbidden

  # The PDF layout is validated by unit tests on the generated document; there is no Cucumber runner in this project.
