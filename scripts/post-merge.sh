#!/bin/bash
set -e

echo "=== Post-merge setup ==="

echo "--- Installing frontend dependencies ---"
cd frontend && npm install --legacy-peer-deps
cd ..

echo "--- Verifying backend compiles ---"
cd backend && ./mvnw -q -DskipTests compile
cd ..

echo "=== Post-merge setup complete ==="
