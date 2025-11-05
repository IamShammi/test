terraform {
  required_version = ">= 1.5"
  
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = "us-east-1"
}

# 🔒 Security Group (allows SSH, HTTP, HTTPS, app ports)
resource "aws_security_group" "app_sg" {
  name        = "expenses-api-sg"
  description = "Security group for expenses API"
  
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 5000
    to_port     = 5000
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 3000
    to_port     = 3000
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "expenses-api-sg"
  }
}

# ✅ Use dynamic AMI lookup instead of hardcoding retired one
data "aws_ami" "amazon_linux" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-2023*-x86_64"]
  }
}

# 🖥️ EC2 instance
resource "aws_instance" "app_server" {
  ami           = data.aws_ami.amazon_linux.id
  instance_type = "t3.micro"

  key_name = "test-keypair" # <- Make sure this key pair exists in AWS EC2 (us-east-1)

  vpc_security_group_ids = [aws_security_group.app_sg.id]

  user_data = <<-EOF
              #!/bin/bash
              set -ex
              
              # Update system
              yum update -y

              # Install Docker
              yum install -y docker
              systemctl start docker
              systemctl enable docker
              usermod -aG docker ec2-user

              # Install Docker Compose (v2)
              curl -L "https://github.com/docker/compose/releases/download/v2.29.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
              chmod +x /usr/local/bin/docker-compose

              # Install Java 21
              yum install -y java-21-amazon-corretto git

              # Clone your repo
              cd /home/ec2-user
              git clone https://github.com/IamShammi/test.git
              cd test

              # Build & run using Docker Compose
              docker-compose up -d
              EOF

  tags = {
    Name = "expenses-api-server"
  }
}

output "instance_ip" {
  value = aws_instance.app_server.public_ip
}

output "instance_id" {
  value = aws_instance.app_server.id
}
