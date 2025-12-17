#!/bin/bash

# Local Quality Checks Script
# This script runs all the same quality checks that are performed in CI

echo "🚀 Running local quality checks for MongOCOM..."
echo

echo "1️⃣ Running tests with coverage..."
mvn clean test
if [ $? -ne 0 ]; then
    echo "❌ Tests failed!"
    exit 1
fi
echo "✅ Tests passed!"
echo

echo "2️⃣ Checking code formatting..."
mvn spotless:check
if [ $? -ne 0 ]; then
    echo "❌ Code formatting issues found. Run 'mvn spotless:apply' to fix them."
    exit 1
fi
echo "✅ Code formatting is correct!"
echo

echo "3️⃣ Running static analysis..."
echo "   - Running Checkstyle..."
mvn checkstyle:check
if [ $? -ne 0 ]; then
    echo "❌ Checkstyle found violations!"
    exit 1
fi
echo "   ✅ Checkstyle passed!"

echo "   - Running PMD..."
mvn pmd:check
if [ $? -ne 0 ]; then
    echo "❌ PMD found violations!"
    exit 1
fi
echo "   ✅ PMD passed!"

echo "   - Running SpotBugs..."
mvn spotbugs:check
if [ $? -ne 0 ]; then
    echo "❌ SpotBugs found violations!"
    exit 1
fi
echo "   ✅ SpotBugs passed!"

echo "✅ All static analysis checks passed!"
echo

echo "4️⃣ Building package..."
mvn package -DskipTests=false
if [ $? -ne 0 ]; then
    echo "❌ Package build failed!"
    exit 1
fi
echo "✅ Package built successfully!"
echo

echo "🎉 All quality checks completed!"
echo "📊 Coverage report available at: target/site/jacoco/index.html"
echo "📦 JAR file available at: target/mongocom-0.3-SNAPSHOT.jar"