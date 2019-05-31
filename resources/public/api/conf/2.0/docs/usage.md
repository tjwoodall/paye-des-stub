For general information on testing see [testing in the sandbox](https://developer.service.hmrc.gov.uk/api-documentation/docs/testing).
To set up test data using this API, follow these steps:

**Step 1:** [Create a test user](https://developer.service.hmrc.gov.uk/api-documentation/docs/testing/test-users-test-data-stateful-behaviour).

**Step 2:** Using the test user’s National Insurance number, call the [Create PAYE test data endpoint](https://developer.service.hmrc.gov.uk/api-documentation/docs/api/service/paye-des-stub/2.0#_create-tax-history-test-data_post_accordion) to create test data.

**Step 3:** Complete the [authorisation journey](https://developer.service.hmrc.gov.uk/api-documentation/docs/authorisation/user-restricted-endpoints) with your test user to get an OAuth token.

**Step 4:** Call the [Get PAYE annual summary endpoint on the Individual PAYE API](https://developer.service.hmrc.gov.uk/api-documentation/docs/api/service/individuals-paye/1.0#_get-paye-annual-summary_get_accordion) to confirm the full details of the test data you created in step 2.

**Step 5:** When testing your application, use the same National Insurance number and tax year to check it shows the test data correctly.