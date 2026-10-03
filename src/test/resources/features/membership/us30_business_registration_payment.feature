@US30 @membership
Feature: Plan selection and initial payment on business registration
  As a business owner
  I want to register my gym choosing a membership plan and pay through Stripe Checkout
  So that my administrator account is activated with immediate access to the platform

  Scenario Outline: Registration with plan selection
    Given a visitor fills in the business registration form with personal and company data
    When the visitor selects the "<plan>" plan and confirms the registration
    Then the system creates a pending registration
    And the system returns a Stripe Checkout session for the "<plan>" plan amount

    Examples:
      | plan     |
      | Basic    |
      | Mid      |
      | Platinum |

  Scenario: Activation after successful payment
    Given the visitor has a pending registration and completed the payment in Stripe Checkout
    When Stripe confirms the transaction through the payment webhook
    Then the administrator account is activated
    And the gym and the membership are created with "Active" status
