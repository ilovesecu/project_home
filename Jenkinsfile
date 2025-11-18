// Jenkinsfile
pipeline {
    agent none
    //Jenkinsfile에서 agent any를 사용하면, 빌드 작업은 Jenkins 메인 컨트롤러(현재 jenkins/jenkins:lts 컨테이너)에서 실행됩니다. 하지만 이 기본 이미지에는 Jenkins만 들어있고 Docker CLI 도구는 포함되어 있지 않습니다.
    //agent any
    //빌드가 시작될 때마다 docker 명령어가 이미 설치된 docker:latest 같은 이미지를 새로 띄우고, 그 컨테이너 안에서 빌드 작업을 수행
    // [수정] 'agent any' 대신, docker 명령어가 있는
    // 'docker:latest' 이미지를 빌드 에이전트로 사용합니다.
    /* agent {
        docker {
            image 'docker:latest'
            // [중요] 이 에이전트에게도 호스트의 docker.sock을 연결(DooD)
            args '-v /var/run/docker.sock:/var/run/docker.sock'
        }
    } */

    // 1단계: Jenkins Credential에 등록한 비밀 정보들을 변수로 불러옵니다.
    environment {
        // (1) Jasypt 설정:
        // 'jasypt-password'라는 ID의 Secret text를 JASYPT_KEY 변수에 할당
        JASYPT_KEY = credentials('jasypt-password')

        // (2) Docker 이미지/컨테이너 이름 설정:
        IMAGE_NAME = "home_project_app"
        CONTAINER_NAME = "homeProjectContainer"
    }

    stages {
        // 2단계: Git Checkout (등록한 GitHub 인증 사용)
        stage('Git Checkout') {
            agent any
            steps {
                // (3) GitHub 설정:
                // 1-A에서 등록한 'github-credentials' ID를 사용해 Git에 접근
                git branch: 'master',
                    url: 'https://github.com/ilovesecu/project_home.git', // (본인 Git 주소)
                    credentialsId: 'homeproject' // (1-A에서 만든 ID)
            }
        }

        // 3단계: Spring Boot 빌드 --> [JAVA] 컨테이너 사용
        stage('Spring Boot Build') {
            agent {
                docker {
                    image 'eclipse-temurin:21'
                }
            }
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

        // 4단계: Docker 이미지 빌드 -> [Docker 컨테이너] 사용
        /*
            Jenkins는 똑똑해서, 앞 단계(Java 컨테이너)에서 빌드한 결과물(.jar 파일)을 다음 단계(Docker 컨테이너)에서도 볼 수 있도록 자동으로 작업 공간을 공유해 줍니다.
        */
        stage('Docker Build & Deploy') {
            agent {
                docker {
                    // [수정] -u root 옵션 추가 (이 컨테이너를 root 권한으로 실행)
                    image 'docker:latest'
                    args '-u root -v /var/run/docker.sock:/var/run/docker.sock'
                }
            }
            steps {
                // ---빌드 부분---
                sh "docker build -t ${IMAGE_NAME} ."
                // --- 배포 부분 ---
                sh "docker stop ${CONTAINER_NAME} || true"
                sh "docker rm ${CONTAINER_NAME} || true"

                withEnv(["JASYPT_ENCRYPTOR_PASSWORD=${JASYPT_KEY}"]) {
                    sh "docker run -d --name ${CONTAINER_NAME} \
                       -p 9495:9495 \
                       -e JASYPT_ENCRYPTOR_PASSWORD=${JASYPT_KEY} \
                       -e SPRING_PROFILES_ACTIVE=real \
                       --network=home-project-network \
                       ${IMAGE_NAME}"
                }
            }
        }
    }
}