#!/bin/bash
set -e

ENDPOINT="http://localhost:4566"
STACK_NAME="patient-management"
TEMPLATE="./cdk.out/localstack.template.json"

echo "Deleting existing stack..."
aws --endpoint-url="$ENDPOINT" cloudformation delete-stack \
    --stack-name "$STACK_NAME" || true

echo "Waiting for stack deletion..."
aws --endpoint-url="$ENDPOINT" cloudformation wait stack-delete-complete \
    --stack-name "$STACK_NAME" || true

echo "Deploying stack..."
aws --endpoint-url="$ENDPOINT" cloudformation deploy \
    --stack-name "$STACK_NAME" \
    --template-file "$TEMPLATE"

echo "Deployment complete."