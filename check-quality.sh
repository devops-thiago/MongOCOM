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
    echo "⚠️  Continuing with other checks..."
else
    echo "✅ Code formatting is correct!"
fi
echo

echo "3️⃣ Running static analysis..."
mvn checkstyle:check pmd:check spotbugs:check
if [ $? -ne 0 ]; then
    echo "⚠️  Static analysis found some issues (warnings only)"
else
    echo "✅ Static analysis passed!"
fi
echo

echo "4️⃣ Building package..."
mvn package -DskipTests
if [ $? -ne 0 ]; then
    echo "❌ Package build failed!"
    exit 1
fi
echo "✅ Package built successfully!"
echo

echo "🎉 All quality checks completed!"
echo "📊 Coverage report available at: target/site/jacoco/index.html"
echo "📦 JAR file available at: target/mongocom-0.3-SNAPSHOT.jar"