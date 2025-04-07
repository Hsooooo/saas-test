#!/bin/bash
APP_NAME="em-stock-rest-api"

# move to project folder
#cd ~/back-end/${APP_NAME}
cd ~/projects/${APP_NAME}
git reset
git checkout .
git clean -fdx
git pull
# execute docker build
/bin/bash build-docker-prod.sh