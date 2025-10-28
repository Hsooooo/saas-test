#!/bin/bash
# shellcheck disable=SC2154
# shellcheck disable=SC1091
APP_NAME="em-saas-rest-api"
APP_NAME_OLD="${APP_NAME}-old"
ACTIVE="dev"
PORT="10350"
NETWORK="database-network"
source ./yaml.sh
source ../documents/em-saas-rest-api/em-saas-key
source ../documents/em-saas-rest-api/em-saas-environments

# 시놀로지 웹훅 배포 시작 알림
curl -X POST 'https://illunex.synology.me:52582/webapi/entry.cgi?api=SYNO.Chat.External&method=incoming&version=2&token='${SYNOLOGY_HOOK_TOKEN} \
-H 'Content-Type: application/x-www-form-urlencoded' \
-d 'payload={"text":"@channel [EM-SAAS] ('${APP_NAME}') 배포 시작"}'

# Execute
create_variables ./src/main/resources/application-dev.yml
echo "---------- [Deploy Version] : ${server_version}"

#################################################################################
# 배포 순서
# 1. docker rename 명령어로 컨테이너 이름을 바꾼다.
# 2. docker tag 명령어로 이미지 이름을 바꾼다.
# 3. gradle 명령어로 빌드하여 jar파일을 뽑는다.
# 4. docker build 명령어로 이미지를 생성한다.
# 5. docker stop 명령어로 1번에서 바꾼 이름으로된 컨테이너를 중지시킨다.
# 6. docker rm 명령어로 5번에서 중지시킨 컨테이너를 삭제한다.
# 7. docker rmi 명령어로 2번에서 바꾼 이름으로된 이미지를 삭제한다.
# 8. docker run 명령어로 4번에서 생성한 이미지로 컨테이너를 구동한다.
#################################################################################

# 1. Change the current docker container name to old
echo "---------- [Deploy Step - 1] : Rename Current Docker Container"
docker rename ${APP_NAME} ${APP_NAME_OLD}
# 2. Change the current docker images name to old
echo "---------- [Deploy Step - 2] : Rename Current Docker Image"
docker tag ${APP_NAME}:${server_version} ${APP_NAME_OLD}:${server_version}
# 3. Build the jar using gradle & java 17 version change
echo "---------- [Deploy Step - 3] : Gradle Build & java 17 version change"
export JAVA_HOME=/usr/lib/jvm/java-1.17.0-openjdk-amd64
sudo update-java-alternatives --set /usr/lib/jvm/java-1.17.0-openjdk-amd64
bash gradlew build --exclude-task test
sudo update-java-alternatives --set /usr/lib/jvm/java-1.11.0-openjdk-amd64
# 4. Build the docker image
echo "---------- [Deploy Step - 4] : Build New Docker Image"
docker build -t ${APP_NAME}:${server_version} .
# 5. Stop the old docker container
echo "---------- [Deploy Step - 5] : Stop Old Docker Container"
docker stop ${APP_NAME_OLD}
# 6. Remove the old docker container
echo "---------- [Deploy Step - 6] : Remove Old Docker Container"
docker rm ${APP_NAME_OLD}
# 7. Remove the old docker image
echo "---------- [Deploy Step - 7] : Remove Old Docker Image"
docker rmi ${APP_NAME_OLD}:${server_version}
# 8. Run new docker container
echo "---------- [Deploy Step - 8] : Run New Docker Container"
docker run -d -p ${PORT}:${PORT} \
    -v /etc/localtime:/etc/localtime:ro -e TZ=Asia/Seoul \
    -e "spring.profiles.active=${ACTIVE}" \
    -e "server.cors-list=${CORS_LIST}" \
    -e "server.encrypt-key=${AES_ENCRYPT_KEY}" \
    -e "webhook.synology.url=${SYNOLOGY_HOOK_URL}" \
    -e "webhook.synology.api=${SYNOLOGY_HOOK_API}" \
    -e "webhook.synology.method=${SYNOLOGY_HOOK_METHOD}" \
    -e "webhook.synology.version=${SYNOLOGY_HOOK_VERSION}" \
    -e "webhook.synology.token=${SYNOLOGY_HOOK_TOKEN}" \
    -e "webhook.synology.scrap.token=${SYNOLOGY_HOOK_SCRAP_TOKEN}" \
    -e "spring.cloud.aws.credentials.access-key=${ACCESS_KEY}" \
    -e "spring.cloud.aws.credentials.secret-key=${SECRET_KEY}" \
    -e "spring.cloud.aws.s3.bucket=${BUCKET}" \
    -e "spring.cloud.aws.s3.region=${REGION}" \
    -e "spring.cloud.aws.region.static=${REGION}" \
    -e "spring.cloud.aws.ses.region=${REGION}" \
    -e "spring.cloud.aws.ses.manager-email=${MANAGER_EMAIL}" \
    -e "TOSS_SECRET_KEY=${TOSS_SECRET_KEY}" \
    -e "TOSS_CLIENT_KEY=${TOSS_CLIENT_KEY}" \
    -e "ai.url=${AI_URL}" \
    -e "front-end.url=${FRONT_URL}" \
    -e "debug.mode=${DEBUG_MODE}" \
    --restart unless-stopped \
    --network ${NETWORK} \
    --name ${APP_NAME} \
    ${APP_NAME}:${server_version}

# 시놀로지 웹훅 배포 완료 알림
curl -X POST 'https://illunex.synology.me:52582/webapi/entry.cgi?api=SYNO.Chat.External&method=incoming&version=2&token='${SYNOLOGY_HOOK_TOKEN} \
-H 'Content-Type: application/x-www-form-urlencoded' \
-d 'payload={"text":"@channel [EM-SAAS] ('${APP_NAME}') 배포 완료"}'

# docker logs show
docker logs -f ${APP_NAME}
