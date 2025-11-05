properties([
    parameters([
        booleanParam(name: 'DESTROY_INFRA', defaultValue: false, description: 'Destroy AWS infra after run?')
    ])
])

pipeline {
    agent any

    environment {
        APP_DIR = 'java-expenses'
        IMAGE_NAME = 'expenses-api'
        DOCKERHUB_REPO = 'shammisepala/expenses-api'
        TF_DIR = 'terraform'
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
             sh "docker build -t ${DOCKERHUB_REPO}:latest ${APP_DIR}"
            }
        }

        stage('Clean Old Containers') {
            steps {
                echo 'Cleaning up old containers...'
                sh 'docker ps -aq --filter "name=expenses" | xargs -r docker rm -f'
            }
        }

      stage('Push Image to DockerHub') {
    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-creds')
    }
    steps {
        echo '⬆️ Pushing image to DockerHub...'
        sh '''
            echo "$DOCKERHUB_CREDENTIALS_PSW" | docker login -u "$DOCKERHUB_CREDENTIALS_USR" --password-stdin
            docker tag expenses-api:latest shammisepala/expenses-api:latest
            docker push shammisepala/expenses-api:latest
        '''
    }
}

        stage('Run Docker Compose (Local Verification)') {
            steps {
                echo '🧩 Running app locally for verification...'
                sh '''
                    docker-compose down || true
                    docker-compose up -d --build
                    sleep 10
                    docker ps
                '''
            }
        }

        stage('Terraform Deploy to AWS') {
            environment {
                AWS_CREDENTIALS = credentials('aws-creds')
            }
            steps {
                dir("${TF_DIR}") {
                    echo '🚀 Deploying app to AWS EC2 using Terraform...'
                    sh '''
                        export AWS_ACCESS_KEY_ID=$AWS_CREDENTIALS_USR
                        export AWS_SECRET_ACCESS_KEY=$AWS_CREDENTIALS_PSW
                        export AWS_DEFAULT_REGION=us-east-1

                        terraform init -input=false
                        terraform plan -out=tfplan -input=false
                        terraform apply -auto-approve tfplan
                    '''
                }
            }
        }

        stage('Post-Deployment Info') {
            steps {
                dir("${TF_DIR}") {
                    echo '🌍 Fetching deployment details...'
                    sh 'terraform output'
                }
            }
        }

        stage('Terraform Destroy (Cleanup)') {
            when {
                expression { return params.DESTROY_INFRA == true }
            }
            environment {
                AWS_CREDENTIALS = credentials('aws-creds')
            }
            steps {
                dir("${TF_DIR}") {
                    echo '🔥 Destroying AWS infrastructure...'
                    sh '''
                        export AWS_ACCESS_KEY_ID=$AWS_CREDENTIALS_USR
                        export AWS_SECRET_ACCESS_KEY=$AWS_CREDENTIALS_PSW
                        export AWS_DEFAULT_REGION=us-east-1

                        terraform destroy -auto-approve
                    '''
                }
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
