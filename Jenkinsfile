pipeline {
    agent any

    environment {
        APP_DIR = 'java-expenses'
        IMAGE_NAME = 'expenses-api'
    }

    stages {
    
       stage('Build Java App') {
            agent {
                docker {
                    image 'maven:3.9.9-eclipse-temurin-17'
                    args '-v /var/run/docker.sock:/var/run/docker.sock'
                }
            }
    steps {
        dir("${APP_DIR}") {
            echo 'Building Spring Boot application...'
            sh 'chmod +x mvnw || true'
            sh './mvnw clean package -DskipTests || mvn clean package -DskipTests'
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
                 dir('terraform') {
                echo 'Checking running containers...'
                sh 'docker ps'
                echo 'App should be reachable at http://localhost:8080'
            }
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
