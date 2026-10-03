@US32 @membership
Feature: Membership lifecycle management
  As an administrator
  I want to cancel, reactivate, upgrade, downgrade or pay the debt of my membership
  So that my gym keeps continuous access to the platform according to my business needs

  Background:
    Given the administrator is signed in to the Membership module

  Scenario: Scheduled cancellation
    Given the administrator has an "Active" membership
    When the administrator requests the cancellation
    Then the cancellation is scheduled for the end of the current billing period
    And the administrator keeps access until that date

  Scenario: Undoing a scheduled cancellation
    Given the administrator has an "Active" membership with a scheduled cancellation
    When the administrator undoes the cancellation
    Then the membership remains "Active" without a scheduled cancellation

  Scenario: Debt settlement
    Given the administrator's membership is "Suspended" due to lack of payment
    When the administrator pays the debt through Stripe Checkout
    And Stripe confirms the payment
    Then the membership is reactivated

  Scenario: Upgrading the plan
    Given the administrator has an "Active" membership with the "Basic" plan
    When the administrator upgrades the membership to the "Mid" plan
    Then the membership plan is "Mid"

  Scenario: Downgrading to a plan that is not lower
    Given the administrator has an "Active" membership with the "Mid" plan
    When the administrator requests a downgrade to the "Platinum" plan
    Then the system rejects the downgrade because the plan is not a lower tier
