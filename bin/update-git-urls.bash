#!/usr/bin/env bash
# set -eux
set -eu

GIT_DIRS=$(find .  -maxdepth 2 -name '.git' -type d)

for p in $GIT_DIRS
do
  PROJECT=${p%/*}
  cd $PROJECT
  echo $PROJECT
  REMOTE_URL=$(git config --get remote.origin.url)
  echo Current remote url is: $REMOTE_URL
  if [[ $REMOTE_URL == *assembla* ]]
  then
    GITHUB_URL=${REMOTE_URL/git.assembla.com:/github.com:relaynetwork/}
    git remote set-url origin $GITHUB_URL
  fi
  cd ~/projects
done
