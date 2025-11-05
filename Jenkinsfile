pipeline {
    agent any

    environment {
        APP_DIR = 'java-expenses'
        IMAGE_NAME = 'expenses-api'
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Cloning repository...'
                checkout scm
            }
        }

        stage('Build Java App') {
            steps {
                dir("${APP_DIR}") {
                    echo 'Building Spring Boot application...'
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                echo 'Building Docker image...'
                sh "docker build -t ${IMAGE_NAME} ${APP_DIR}"
            }
        }

        stage('Run Docker Compose') {
            steps {
                echo 'Starting all containers using Docker Compose...'
                sh 'docker-compose up -d --build'
            }
        }

        stage('Verify Application') {
            steps {
                echo 'Checking running containers...'
                sh 'docker ps'
                echo 'App should be reachable at http://localhost:8080'
            }
        }
    }

    post {
        always {
            echo 'Cleaning up workspace...'
            cleanWs()
        }
    }
}