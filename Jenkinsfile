// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

library(
  identifier: 'jenkins-packages-build-library@1.0.4',
  retriever: modernSCM([
    $class: 'GitSCMSource',
    remote: 'git@github.com:zextras/jenkins-packages-build-library.git',
    credentialsId: 'jenkins-integration-with-github-account'
  ])
)

pipeline {
  agent {
    node {
      label 'zextras-v1'
    }
  }

  environment {
    FAILURE_EMAIL_RECIPIENTS = 'smokybeans@zextras.com'
  }

  options {
    buildDiscarder(logRotator(numToKeepStr: '5'))
    skipDefaultCheckout()
    timeout(time: 1, unit: 'HOURS')
  }

  parameters {
    booleanParam defaultValue: false,
      description: 'Whether to upload the packages in playground repository',
      name: 'PLAYGROUND'
  }

  tools {
    jfrog 'jfrog-cli'
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
        script {
          gitMetadata()
        }
      }
    }

    stage('Compiling') {
      steps {
        container('jdk-17') {
          sh '''
            mvn -Dmaven.repo.local=$(pwd)/m2 -T1C compile
            mvn package -Dmaven.main.skip -Dmaven.repo.local=$(pwd)/m2
            cp carbonio-message-dispatcher-auth/target/carbonio-message-dispatcher-auth-fatjar.jar package/
          '''
        }
      }
    }

    stage('Build and Publish Docker Image') {
      when {
        anyOf {
          branch 'devel'
          buildingTag()
          expression { params.PLAYGROUND == true }
        }
      }

      steps {
        container('dind') {
          withDockerRegistry(credentialsId: 'private-registry', url: 'https://registry.dev.zextras.com') {
            script {
              Set<String> imageTags = []

              if (env.BRANCH_NAME == 'devel') {
                imageTags.add('latest')
              } else if (buildingTag() && env.TAG_NAME?.trim()) {
                imageTags.add(env.TAG_NAME?.startsWith('v') ? env.TAG_NAME.substring(1) : env.TAG_NAME)
              } else if (params.PLAYGROUND == true) {
                imageTags.add(env.BRANCH_NAME.replaceAll('/', '-'))
              }

              dockerHelper.buildImage([
                imageName: 'registry.dev.zextras.com/dev/carbonio-message-dispatcher-ce',
                imageTags: imageTags,
                dockerfile: 'docker/Dockerfile',
                ocLabels: [
                  title: 'Carbonio Message Dispatcher Community Edition',
                  descriptionFile: 'docker/description.md',
                  version: imageTags[0]
                ]
              ])

              dockerHelper.buildImage([
                imageName: 'registry.dev.zextras.com/dev/carbonio-message-dispatcher-ce-db',
                imageTags: imageTags,
                dockerfile: 'docker/db/Dockerfile',
                ocLabels: [
                  title: 'Carbonio Message Dispatcher Community Edition DB',
                  descriptionFile: 'docker/db/description.md',
                  version: imageTags[0]
                ]
              ])
            }
          }
        }
      }
    }

    stage('Build deb/rpm') {
      steps {
        echo 'Building deb/rpm packages'
        withCredentials([
          usernamePassword(
            credentialsId: 'artifactory-jenkins-gradle-properties-splitted',
            passwordVariable: 'SECRET',
            usernameVariable: 'USERNAME'
          )
        ]) {
          script {
            env.REPO_ENV = env.GIT_TAG ? 'rc' : 'devel'
          }
          buildStage([
            prepare: true,
            overrides: [
              'ubuntu-jammy': [
                preBuildScript: '''
                  echo "machine zextras.jfrog.io" >> auth.conf
                  echo "login ''' + USERNAME + '''" >> auth.conf
                  echo "password ''' + SECRET + '''" >> auth.conf
                  mv auth.conf /etc/apt
                  echo "deb [trusted=yes] https://zextras.jfrog.io/artifactory/ubuntu-''' + env.REPO_ENV + ''' jammy main" \
                  > zextras.list
                  mv zextras.list /etc/apt/sources.list.d/
                '''
              ],
              'ubuntu-noble': [
                preBuildScript: '''
                  echo "machine zextras.jfrog.io" >> auth.conf
                  echo "login ''' + USERNAME + '''" >> auth.conf
                  echo "password ''' + SECRET + '''" >> auth.conf
                  mv auth.conf /etc/apt
                  echo "deb [trusted=yes] https://zextras.jfrog.io/artifactory/ubuntu-''' + env.REPO_ENV + ''' noble main" \
                  > zextras.list
                  mv zextras.list /etc/apt/sources.list.d/
                '''
              ],
              'rocky-8': [
                preBuildScript: '''
                  echo "[Zextras]" > zextras.repo
                  echo "name=Zextras" >> zextras.repo
                  echo "baseurl=https://''' + USERNAME + ':' + SECRET + '''@zextras.jfrog.io/artifactory/centos8-''' + env.REPO_ENV + '''/" >> zextras.repo
                  echo "enabled=1" >> zextras.repo
                  echo "gpgcheck=0" >> zextras.repo
                  echo "gpgkey=https://''' + USERNAME + ':' + SECRET + '''@zextras.jfrog.io/artifactory/centos8-''' + env.REPO_ENV + '''/repomd.xml.key" >> zextras.repo
                  mv zextras.repo /etc/yum.repos.d/zextras.repo
                ''',
              ],
              'rocky-9': [
                preBuildScript: '''
                  echo "[Zextras]" > zextras.repo
                  echo "name=Zextras" >> zextras.repo
                  echo "baseurl=https://''' + USERNAME + ':' + SECRET + '''@zextras.jfrog.io/artifactory/rhel9-''' + env.REPO_ENV + '''/" >> zextras.repo
                  echo "enabled=1" >> zextras.repo
                  echo "gpgcheck=0" >> zextras.repo
                  echo "gpgkey=https://''' + USERNAME + ':' + SECRET + '''@zextras.jfrog.io/artifactory/rhel9-''' + env.REPO_ENV + '''/repomd.xml.key" >> zextras.repo
                  mv zextras.repo /etc/yum.repos.d/zextras.repo
                ''',
              ],
            ]
          ])
        }
      }
    }

    stage('Upload artifacts')
    {
      steps {
        uploadStage(
          packages: yapHelper.getPackageNames()
        )
      }
    }
  }
}
