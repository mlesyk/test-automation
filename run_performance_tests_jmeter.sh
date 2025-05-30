#!/bin/bash
# Run all performance tests (including JMeter)
mvn clean test -DsuiteXmlFile=src/test/resources/test-suites/performance-with-jmeter.xml

# Run only JMeter tests
mvn test -Dtest=JMeterTestSuite

# Run JMeter vs k6 comparison
mvn test -Dtest=JMeterVsK6ComparisonTest

# Run with custom JMeter home
mvn test -Dtest=JMeterTestSuite -Djmeter.home=/path/to/jmeter

# Run JMeter Maven plugin directly
mvn jmeter:jmeter -Dbase.url=https://api.example.com -Dusers=20 -Dduration=120