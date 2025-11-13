FROM eclipse-temurin:21

# 2. (중요) 빌드된 Spring Boot의 .jar 파일이 위치할 경로를 지정
#    build.gradle을 쓴다면 이 경로
ARG JAR_FILE=build/libs/*.jar

# 3. 위 경로의 .jar 파일을 컨테이너 내부의 app.jar라는 이름으로 복사
COPY ${JAR_FILE} app.jar

# 4. 컨테이너가 시작될 때 이 명령어를 실행
ENTRYPOINT ["java","-jar","/app.jar"]