pipeline {
    agent any

    environment {
        DOCKER_IMAGE_NAME = 'village1031/gandalp-api'
        DOCKER_CREDENTIALS_ID = 'docker-access'
        EC2_IP = '13.209.96.152'
        EC2_USER = 'ec2-user'
        CONTAINER_NAME = 'gandalpContaioner'
        AWS_REGION = 'ap-northeast-2'
        ASG_NAME = 'gandalp-asg'
        APP_SECRET_YML = credentials('application-secret.yml') // 시크릿 등록한 ID
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'develop', credentialsId: 'github-token', url: 'git@github.com:beyond-sw-camp/be13-fin-SolitaryDevelopers-Gandalp-BE.git'
            }
        }

        stage('Inject Secret') {
            steps {
                writeFile file: 'application-secret.yml', text: "${APP_SECRET_YML}"
            }
        }

        stage('Build JAR') {
            steps {
                sh 'chmod +x ./gradlew'
                sh './gradlew build'
                sh 'ls -al ./build/libs'
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    dockerImage = docker.build("${DOCKER_IMAGE_NAME}")
                }
            }
        }

        stage('Push to Docker Hub') {
            steps {
                script {
                    docker.withRegistry('https://index.docker.io/v1/', "${DOCKER_CREDENTIALS_ID}") {
                        dockerImage.push("${env.BUILD_NUMBER}")
                        dockerImage.push('latest')
                    }
                }
            }
        }

        stage('Trigger ASG Rolling Update') {
            steps {
                sh """
                aws autoscaling start-instance-refresh \
                --auto-scaling-group-name $ASG_NAME \
                --region $AWS_REGION \
                --strategy Rolling
                """
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}
