#!/bin/bash
# Run the new service tests
mvn clean test -DsuiteXmlFile=src/test/resources/test-suites/service-tests.xml

# Or run specific service tests
mvn test -Dtest=UserServiceTest
mvn test -Dtest=PostServiceTest
mvn test -Dtest=ServiceIntegrationTest