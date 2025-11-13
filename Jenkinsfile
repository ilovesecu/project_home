// Jenkinsfile
pipeline {
    agent any

    // 1단계: Jenkins Credential에 등록한 비밀 정보들을 변수로 불러옵니다.
    environment {
        // (1) Jasypt 설정:
        // 'jasypt-password'라는 ID의 Secret text를 JASYPT_KEY 변수에 할당
        JASYPT_KEY = credentials('jasypt-password')

        // (2) Docker 이미지/컨테이너 이름 설정:
        IMAGE_NAME = "homeProjectApp"
        CONTAINER_NAME = "homeProjectContainer"
    }

    stages {
        // 2단계: Git Checkout (등록한 GitHub 인증 사용)
        stage('Git Checkout') {
            steps {
                // (3) GitHub 설정:
                // 1-A에서 등록한 'github-credentials' ID를 사용해 Git에 접근
                git branch: 'master',
                    url: 'https://github.com/ilovesecu/project_home.git', // (본인 Git 주소)
                    credentialsId: 'homeproject' // (1-A에서 만든 ID)
            }
        }

        // 3단계: Spring Boot 빌드
        stage('Spring Boot Build') {
            steps {
                sh "chmod +x ./gradlew"
                // (수정) Jenkins의 JASYPT_KEY 변수를
                // JASYPT_ENCRYPTOR_PASSWORD라는 이름의 환경 변수로 주입하여 빌드 실행
                // withEnv 블록으로 감싸서 환경변수를 안전하게 주입
                withEnv(["JASYPT_ENCRYPTOR_PASSWORD=${JASYPT_KEY}"]) {

                    // 여기에 빌드 명령어를 넣습니다.
                    // (방법 1 선택 시)
                    //sh "./gradlew build"

                    // (방법 2 선택 시)
                    sh "./gradlew build -x test"
                }
            }
        }

        // 4단계: Docker 이미지 빌드
        stage('Docker Build') {
            steps {
                sh "docker build -t ${IMAGE_NAME} ."
            }
        }

        // 5단계: Docker 배포 (Jasypt 키 주입)
        stage('Deploy to Docker') {
            steps {
                sh "docker stop ${CONTAINER_NAME} || true"
                sh "docker rm ${CONTAINER_NAME} || true"

                // (4) Jasypt 키 주입:
                // Spring Boot는 JASYPT_ENCRYPTOR_PASSWORD 환경변수를
                // Djasypt.encryptor.password=... 보다 우선하여 자동으로 인식합니다.
                // -e 옵션으로 Jenkins 변수(${JASYPT_KEY})를 Docker 컨테이너의 환경변수로 전달합니다.
                sh "docker run -d --name ${CONTAINER_NAME} \
                   -p 9495:9495 \
                   -e JASYPT_ENCRYPTOR_PASSWORD=${JASYPT_KEY} \
                   -e SPRING_PROFILES_ACTIVE=real \
                   ${IMAGE_NAME}"
            }
        }
    }
}