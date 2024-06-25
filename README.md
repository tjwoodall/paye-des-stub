# PAYE DES Stub

The PAYE DES Stub is a service to support stateful sandbox testing in the
External Test environment. It stubs the behaviour of DES in order that an API microservice
is able to implement only a single set of routes regardless of whether it is being called
in a test or production environment.

It is a semi-stateful test service - in order to use it, you need to request it to set up test
data for a specific taxpayer and tax year. It will then generate pre-defined test data for that
taxpayer and tax year.

The POST endpoints for setting up test data are exposed on the API Platform as the Individual PAYE
Test Support API. The GET endpoints are called by the relevant API microservices.

The API supports the Individual Benefits, Individual Employment, Individual Income and Individual Tax APIs


## What uses this service?

API microservices that make PAYE-related calls to DES which are deployed to the
External Test environment should be configured to connect to this stub instead of a real DES.

API microservices which this stubs behaviour for are:
* individual-benefits
* individual-employment
* individual-income
* individual-tax

## Running the tests

```bash
./run_all_tests.sh
```

## Running the service locally

First, make sure you have mongo running locally

To run the service locally on port `9689`:
```bash
./run_local.sh
```

To test the stub endpoints for Individual Benefits:
```
curl --header "Content-Type: application/json" \
  --header "Accept: application/vnd.hmrc.1.0+json" \
  --request POST \
  --data '{ "scenario": "HAPPY_PATH_1" }' \
  http://localhost:9689/sa/2234567890/benefits/annual-summary/2017-18
curl -X GET http://localhost:9689/self-assessment-prepop/individual/2234567890/benefits/tax-year/2017
```

To test the stub endpoint for Individual Employment:
```
curl --header "Content-Type: application/json" \
  --header "Accept: application/vnd.hmrc.1.0+json" \
  --request POST \
  --data '{ "scenario": "HAPPY_PATH_1" }' \
  http://localhost:9689/sa/2234567890/employments/annual-summary/2017-18
curl -X GET http://localhost:9689/self-assessment-prepop/individual/2234567890/employment-history/tax-year/2017
```

To test the stub endpoint for Individual Income:
```
curl --header "Content-Type: application/json" \
  --header "Accept: application/vnd.hmrc.1.0+json" \
  --request POST \
  --data '{ "scenario": "HAPPY_PATH_1" }' \
  http://localhost:9689/sa/2234567890/income/annual-summary/2017-18
curl -X GET http://localhost:9689/self-assessment-prepop/individual/2234567890/income-summary/tax-year/2017
```

To test the stub endpoint for Individual Tax:
```
curl --header "Content-Type: application/json" \
  --header "Accept: application/vnd.hmrc.1.0+json" \
  --request POST \
  --data '{ "scenario": "HAPPY_PATH_1" }' \
  http://localhost:9689/sa/2234567890/tax/annual-summary/2017-18
curl -X GET http://localhost:9689/self-assessment-prepop/individual/2234567890/tax-summary/tax-year/2017
```

## Viewing Documentation
### Locally
- Run PAYE DES stub and other required services with the script:

    ```bash
     ./run_local_preview_documentation.sh
    ```

- Navigate to the preview page at http://localhost:9680/api-documentation/docs/openapi/preview
- Enter the full URL path of the OpenAPI specification file with the appropriate port and version:

    ```
     http://localhost:9689/api/conf/1.0/application.yaml
    ```
- Ensure to uncomment the lines [here](https://github.com/hmrc/paye-des-stub/blob/main/conf/application.conf#L22-L25) in case of CORS errors

### On Developer Hub
Full documentation can be found on the [Developer Hub](https://developer.service.hmrc.gov.uk/api-documentation/docs/api/service/paye-des-stub).
