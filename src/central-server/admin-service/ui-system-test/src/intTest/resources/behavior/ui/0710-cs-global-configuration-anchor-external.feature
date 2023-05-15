@CentralServer
@ConfigurationAnchor
@LoadingTesting
Feature: CS: Global configuration: External configuration: Anchor

  Background:
    Given CentralServer login page is open
    And User xrd logs in to CentralServer with password secret
    And Global configuration tab is selected
    And External configuration sub-tab is selected

  Scenario: User can recreate anchor
    When Configuration anchor is recreated
    Then Updated anchor information is displayed

  Scenario: User can download anchor
    When User clicks configuration anchor download button
    Then Configuration anchor is successfully downloaded