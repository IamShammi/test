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

stage('Clean Old Containers') {
    steps {
        echo 'Cleaning up old containers...'
        sh 'docker ps -aq --filter "name=expenses" | xargs -r docker rm -f'
    }
}
stage('Push Image to DockerHub') {
            environment {
                DOCKERHUB_CREDENTIALS = credentials('dockerhub-creds')  // Jenkins credential ID
            }
            steps {
                echo '⬆️ Pushing image to DockerHub...'
                sh '''
                    echo "$DOCKERHUB_CREDENTIALS_PSW" | docker login -u "$DOCKERHUB_CREDENTIALS_USR" --password-stdin
                    docker tag ${IMAGE_NAME}:latest ${DOCKERHUB_REPO}:latest
                    docker push ${DOCKERHUB_REPO}:latest
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
     steps {
            dir("${TF_DIR}") {
                    echo '🚀 Deploying application to AWS EC2 using Terraform...'
                    sh '''
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
    }

        stage('Terraform Destroy (Cleanup)') {
            when {
                expression {
                    return params.DESTROY_INFRA == true
                }
            }
            steps {
                dir("${TF_DIR}") {
                    echo '🔥 Destroying AWS infrastructure...'
                    sh '''
                        terraform destroy -auto-approve
                    '''
                }
            }
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
