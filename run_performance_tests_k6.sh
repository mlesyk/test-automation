#!/bin/bash

# Run performance tests
mvn clean test -DsuiteXmlFile=src/test/resources/test-suites/performance-tests.xml

# Run quick performance tests (for CI/CD)
mvn clean test -DsuiteXmlFile=src/test/resources/test-suites/quick-performance-tests.xml

# Run with specific environment
mvn clean test -Denvironment=ci -DsuiteXmlFile=src/test/resources/test-suites/performance-tests.xml

# Run load tests only
mvn test -Dtest=LoadTestSuite

# Run stress tests only
mvn test -Dtest=StressTestSuite

# Run spike tests only
mvn test -Dtest=SpikeTestSuite

# Run performance integration tests
mvn test -Dtest=PerformanceIntegrationTest