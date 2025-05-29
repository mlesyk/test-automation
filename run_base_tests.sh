#!/bin/bash
# Clean and compile
mvn clean compile

# Run the framework setup test
mvn test -Dtest=FrameworkSetupTest

# Or run the entire smoke suite
mvn test -DsuiteXmlFile=src/test/resources/test-suites/smoke-tests.xml