#!/bin/bash

# SPDX-FileCopyrightText: 2022 2021 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: AGPL-3.0-only

function build-all-artifacts() {
  no_docker=$1

  build-ubuntu-artifact
  build-rocky-8-artifact

  print-banner "success"

}

function build-ubuntu-artifact() {
  if [ "$no_docker" = true ]; then
    pacur build ubuntu
  else
    docker run \
      --rm --entrypoint "" \
      -v "$(pwd)":/tmp/messaging \
      registry.dev.zextras.com/jenkins/pacur/ubuntu-20.04:v1 /bin/bash -c 'pacur build ubuntu /tmp/messaging'
  fi
}

function build-rocky-8-artifact() {
  if [ "$no_docker" = true ]; then
    pacur build rocky-8
  else
    docker run \
      --rm --entrypoint "" \
      -v "$(pwd)":/tmp/messaging \
      registry.dev.zextras.com/jenkins/pacur/rocky-8:v1 /bin/bash -c 'pacur build rocky-8 /tmp/messaging'
  fi
}

function print-banner() {
  string_to_print=$1
  banner_string=""
  if [ ${#string_to_print} -lt 60 ]; then
    start_spaces=$((60 - ${#string_to_print}))
    start_spaces=$((start_spaces / 2))
    index=0
    while [ $index -lt $start_spaces ]; do
      banner_string="$banner_string "
      index=$((index + 1))
    done
    banner_string="$banner_string$string_to_print"
    index=$((index + ${#string_to_print}))
    while [ $index -lt 60 ]; do
      banner_string="$banner_string "
      index=$((index + 1))
    done
  else
    banner_string="$string_to_print"
  fi
  index=0
  border_string=""
  while [ $index -lt 72 ]; do
    border_string="$border_string*"
    index=$((index + 1))
  done
  echo ""
  echo "$border_string"
  echo "****  $banner_string  ****" | tr a-z A-Z
  echo "$border_string"
}

build-all-artifacts "$1"
