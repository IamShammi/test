# Terraform AWS EC2 Deployment

## Overview
This Terraform configuration deploys the expenses API to an AWS EC2 instance.

## Prerequisites
- AWS CLI configured with credentials
- Terraform 1.5+
- SSH key pair created in AWS

## Usage
1. Update `terraform.tfvars` with your values
2. Initialize: `terraform init`
3. Plan: `terraform plan`
4. Apply: `terraform apply`

## What Gets Deployed
- EC2 instance (t3.micro)
- Security group allowing HTTP/HTTPS/SSH
- PostgreSQL and Redis via Docker
- Application code
