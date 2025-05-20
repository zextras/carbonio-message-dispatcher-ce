// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

pipeline {
  parameters {
    booleanParam defaultValue: false,
    description: 'Whether to upload the packages in playground repository',
    name: 'PLAYGROUND'
  }
  options {
    skipDefaultCheckout()
    buildDiscarder(logRotator(numToKeepStr: '5'))
    timeout(time: 1, unit: 'HOURS')
  }
  agent {
    node {
      label 'zextras-v1'
    }
  }
  environment {
    FAILURE_EMAIL_RECIPIENTS='smokybeans@zextras.com'
  }
  stages {
    stage('Checkout') {
      steps {
        checkout scm
        withCredentials([file(credentialsId: 'jenkins-maven-settings.xml', variable: 'SETTINGS_PATH')]) {
          sh 'cp $SETTINGS_PATH settings-jenkins.xml'
        }
        script {
          env.GIT_COMMIT = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
        }
      }
    }
    stage('Compiling') {
      steps {
        container('jdk-17') {
          sh 'mvn -Dmaven.repo.local=$(pwd)/m2 -T1C -B -s settings-jenkins.xml compile'
          sh 'mvn package -Dmaven.main.skip -Dmaven.repo.local=$(pwd)/m2'
          sh 'mkdir dispatcher'
          sh 'cp -r package yap.json dispatcher'
          sh 'cp carbonio-message-dispatcher-auth/target/carbonio-message-dispatcher-auth-fatjar.jar dispatcher/package'
          stash includes: 'dispatcher/**', name: 'project'
        }
      }
      post {
        failure {
          script {
            if ("main".equals(env.BRANCH_NAME) || "devel".equals(env.BRANCH_NAME)) {
              sendFailureEmail(STAGE_NAME)
            }
          }
        }
      }
    }
    stage('Building packages') {
      parallel {
        stage('Ubuntu 20') {
          agent {
            node {
              label 'yap-ubuntu-20-v1'
            }
          }
          steps {
            container('yap') {
              unstash 'project'
              withCredentials([usernamePassword(credentialsId: 'artifactory-jenkins-gradle-properties-splitted',
                passwordVariable: 'SECRET',
                usernameVariable: 'USERNAME')]) {
                  sh 'echo "machine zextras.jfrog.io" >> auth.conf'
                  sh 'echo "login $USERNAME" >> auth.conf'
                  sh 'echo "password $SECRET" >> auth.conf'
                  sh 'sudo mv auth.conf /etc/apt'
              }
              sh '''
                sudo echo "deb [trusted=yes] https://zextras.jfrog.io/artifactory/ubuntu-devel focal main" > zextras.list
                sudo mv zextras.list /etc/apt/sources.list.d/
                sudo yap prepare ubuntu
              '''
              script {
                if (BRANCH_NAME == 'devel') {
                  def timestamp = new Date().format('yyyyMMddHHmmss')
                  sh "sudo yap build ubuntu-focal dispatcher -r ${timestamp}"
                } else {
                  sh 'sudo yap build ubuntu-focal dispatcher'
                }
              }
              stash includes: 'artifacts/*focal*.deb', name: 'artifacts-ubuntu-focal'
            }
          }
          post {
            failure {
              script {
                if ("main".equals(env.BRANCH_NAME) || "devel".equals(env.BRANCH_NAME)) {
                  sendFailureEmail(STAGE_NAME)
                }
              }
            }
            always {
              archiveArtifacts artifacts: 'artifacts/*focal*.deb', fingerprint: true
            }
          }
        }
        stage('Ubuntu 22') {
          agent {
            node {
              label 'yap-ubuntu-22-v1'
            }
          }
          steps {
            container('yap') {
              unstash 'project'
              withCredentials([usernamePassword(credentialsId: 'artifactory-jenkins-gradle-properties-splitted',
                passwordVariable: 'SECRET',
                usernameVariable: 'USERNAME')]) {
                  sh 'echo "machine zextras.jfrog.io" >> auth.conf'
                  sh 'echo "login $USERNAME" >> auth.conf'
                  sh 'echo "password $SECRET" >> auth.conf'
                  sh 'sudo mv auth.conf /etc/apt'
              }
              sh '''
                sudo echo "deb [trusted=yes] https://zextras.jfrog.io/artifactory/ubuntu-devel jammy main" > zextras.list
                sudo mv zextras.list /etc/apt/sources.list.d/
                sudo yap prepare ubuntu
              '''
              script {
                if (BRANCH_NAME == 'devel') {
                  def timestamp = new Date().format('yyyyMMddHHmmss')
                  sh "sudo yap build ubuntu-jammy dispatcher -r ${timestamp}"
                } else {
                  sh 'sudo yap build ubuntu-jammy dispatcher'
                }
              }
              stash includes: 'artifacts/*jammy*.deb', name: 'artifacts-ubuntu-jammy'
            }
          }
          post {
            failure {
              script {
                if ("main".equals(env.BRANCH_NAME) || "devel".equals(env.BRANCH_NAME)) {
                  sendFailureEmail(STAGE_NAME)
                }
              }
            }
            always {
              archiveArtifacts artifacts: 'artifacts/*jammy*.deb', fingerprint: true
            }
          }
        }
        stage('Ubuntu 24') {
          agent {
            node {
              label 'yap-ubuntu-24-v1'
            }
          }
          steps {
            container('yap') {
              unstash 'project'
              withCredentials([usernamePassword(credentialsId: 'artifactory-jenkins-gradle-properties-splitted',
                passwordVariable: 'SECRET',
                usernameVariable: 'USERNAME')]) {
                  sh 'echo "machine zextras.jfrog.io" >> auth.conf'
                  sh 'echo "login $USERNAME" >> auth.conf'
                  sh 'echo "password $SECRET" >> auth.conf'
                  sh 'sudo mv auth.conf /etc/apt'
              }
              sh '''
                sudo echo "deb [trusted=yes] https://zextras.jfrog.io/artifactory/ubuntu-devel noble main" > zextras.list
                sudo mv zextras.list /etc/apt/sources.list.d/
                sudo yap prepare ubuntu
              '''
              script {
                if (BRANCH_NAME == 'devel') {
                  def timestamp = new Date().format('yyyyMMddHHmmss')
                  sh "sudo yap build ubuntu-noble dispatcher -r ${timestamp}"
                } else {
                  sh 'sudo yap build ubuntu-noble dispatcher'
                }
              }
              stash includes: 'artifacts/*noble*.deb', name: 'artifacts-ubuntu-noble'
            }
          }
          post {
            failure {
              script {
                if ("main".equals(env.BRANCH_NAME) || "devel".equals(env.BRANCH_NAME)) {
                  sendFailureEmail(STAGE_NAME)
                }
              }
            }
            always {
              archiveArtifacts artifacts: 'artifacts/*noble*.deb', fingerprint: true
            }
          }
        }
        stage('Rocky 8') {
          agent {
            node {
              label 'yap-rocky-8-v1'
            }
          }
          steps {
            container('yap') {
              unstash 'project'
              withCredentials([usernamePassword(credentialsId: 'artifactory-jenkins-gradle-properties-splitted',
                passwordVariable: 'SECRET',
                usernameVariable: 'USERNAME')]) {
                  sh 'echo "[Zextras]" > zextras.repo'
                  sh 'echo "baseurl=https://$USERNAME:$SECRET@zextras.jfrog.io/artifactory/centos8-devel/" >> zextras.repo'
                  sh 'echo "enabled=1" >> zextras.repo'
                  sh 'echo "gpgcheck=0" >> zextras.repo'
                  sh 'echo "gpgkey=https://$USERNAME:$SECRET@zextras.jfrog.io/artifactory/centos8-devel/repomd.xml.key" >> zextras.repo'
                  sh 'sudo mv zextras.repo /etc/yum.repos.d/zextras.repo'
                  sh 'sudo yap prepare rocky'
              }
              script {
                if (BRANCH_NAME == 'devel') {
                  def timestamp = new Date().format('yyyyMMddHHmmss')
                  sh "sudo yap build rocky-8 dispatcher -r ${timestamp}"
                } else {
                  sh 'sudo yap build rocky-8 dispatcher'
                }
              }
              stash includes: 'artifacts/*el8*.rpm', name: 'artifacts-rocky-8'
            }
          }
          post {
            failure {
              script {
                if ("main".equals(env.BRANCH_NAME) || "devel".equals(env.BRANCH_NAME)) {
                  sendFailureEmail(STAGE_NAME)
                }
              }
            }
            always {
              archiveArtifacts artifacts: 'artifacts/*el8*.rpm', fingerprint: true
            }
          }
        }
        stage('Rocky 9') {
          agent {
            node {
              label 'yap-rocky-9-v1'
            }
          }
          steps {
            container('yap') {
              unstash 'project'
              withCredentials([usernamePassword(credentialsId: 'artifactory-jenkins-gradle-properties-splitted',
                passwordVariable: 'SECRET',
                usernameVariable: 'USERNAME')]) {
                  sh 'echo "[Zextras]" > zextras.repo'
                  sh 'echo "baseurl=https://$USERNAME:$SECRET@zextras.jfrog.io/artifactory/rhel9-devel/" >> zextras.repo'
                  sh 'echo "enabled=1" >> zextras.repo'
                  sh 'echo "gpgcheck=0" >> zextras.repo'
                  sh 'echo "gpgkey=https://$USERNAME:$SECRET@zextras.jfrog.io/artifactory/rhel9-devel/repomd.xml.key" >> zextras.repo'
                  sh 'sudo mv zextras.repo /etc/yum.repos.d/zextras.repo'
                  sh 'sudo yap prepare rocky'
              }
              script {
                if (BRANCH_NAME == 'devel') {
                  def timestamp = new Date().format('yyyyMMddHHmmss')
                  sh "sudo yap build rocky-9 dispatcher -r ${timestamp}"
                } else {
                  sh 'sudo yap build rocky-9 dispatcher'
                }
              }
              stash includes: 'artifacts/*el9*.rpm', name: 'artifacts-rocky-9'
            }
          }
          post {
            failure {
              script {
                if ("main".equals(env.BRANCH_NAME) || "devel".equals(env.BRANCH_NAME)) {
                  sendFailureEmail(STAGE_NAME)
                }
              }
            }
            always {
              archiveArtifacts artifacts: 'artifacts/*el9*.rpm', fingerprint: true
            }
          }
        }
      }
    }
    stage('Upload To Playground') {
      when {
        expression { params.PLAYGROUND == true }
      }
      steps {
        unstash 'artifacts-ubuntu-focal'
        unstash 'artifacts-ubuntu-jammy'
        unstash 'artifacts-ubuntu-noble'
        unstash 'artifacts-rocky-8'
        unstash 'artifacts-rocky-9'

        script {
          def server = Artifactory.server 'zextras-artifactory'
          def buildInfo
          def uploadSpec
          buildInfo = Artifactory.newBuildInfo()
          uploadSpec = """{
            "files": [
              {
                "pattern": "artifacts/*focal*.deb",
                "target": "ubuntu-playground/pool/",
                "props": "deb.distribution=focal;deb.component=main;deb.architecture=amd64;vcs.revision=${env.GIT_COMMIT}"
              },
              {
                "pattern": "artifacts/*jammy*.deb",
                "target": "ubuntu-playground/pool/",
                "props": "deb.distribution=jammy;deb.component=main;deb.architecture=amd64;vcs.revision=${env.GIT_COMMIT}"
              },
              {
                "pattern": "artifacts/*noble*.deb",
                "target": "ubuntu-playground/pool/",
                "props": "deb.distribution=noble;deb.component=main;deb.architecture=amd64;vcs.revision=${env.GIT_COMMIT}"
              },
              {
                "pattern": "artifacts/(carbonio-message-dispatcher-ce)-(*).el8.x86_64.rpm",
                "target": "centos8-playground/zextras/{1}/{1}-{2}.el8.x86_64.rpm",
                "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras;vcs.revision=${env.GIT_COMMIT}"
              },
              {
                "pattern": "artifacts/(carbonio-message-dispatcher-ce)-(*).el9.x86_64.rpm",
                "target": "rhel9-playground/zextras/{1}/{1}-{2}.el9.x86_64.rpm",
                "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras;vcs.revision=${env.GIT_COMMIT}"
              }
            ]
          }"""
          server.upload spec: uploadSpec, buildInfo: buildInfo, failNoOp: false
        }
      }
    }
    stage('Upload To Devel') {
      when {
        branch "devel"
      }
      steps {
        unstash 'artifacts-ubuntu-focal'
        unstash 'artifacts-ubuntu-jammy'
        unstash 'artifacts-ubuntu-noble'
        unstash 'artifacts-rocky-8'
        unstash 'artifacts-rocky-9'

        script {
          def server = Artifactory.server 'zextras-artifactory'
          def buildInfo
          def uploadSpec
          buildInfo = Artifactory.newBuildInfo()
          uploadSpec = """{
            "files": [
              {
                "pattern": "artifacts/*focal*.deb",
                "target": "ubuntu-devel/pool/",
                "props": "deb.distribution=focal;deb.component=main;deb.architecture=amd64;vcs.revision=${env.GIT_COMMIT}"
              },
              {
                "pattern": "artifacts/*jammy*.deb",
                "target": "ubuntu-devel/pool/",
                "props": "deb.distribution=jammy;deb.component=main;deb.architecture=amd64;vcs.revision=${env.GIT_COMMIT}"
              },
              {
                "pattern": "artifacts/*noble*.deb",
                "target": "ubuntu-devel/pool/",
                "props": "deb.distribution=noble;deb.component=main;deb.architecture=amd64;vcs.revision=${env.GIT_COMMIT}"
              },
              {
                "pattern": "artifacts/(carbonio-message-dispatcher-ce)-(*).el8.x86_64.rpm",
                "target": "centos8-devel/zextras/{1}/{1}-{2}.el8.x86_64.rpm",
                "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras;vcs.revision=${env.GIT_COMMIT}"
              },
              {
                "pattern": "artifacts/(carbonio-message-dispatcher-ce)-(*).el9.x86_64.rpm",
                "target": "rhel9-devel/zextras/{1}/{1}-{2}.el9.x86_64.rpm",
                "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras;vcs.revision=${env.GIT_COMMIT}"
              }
            ]
          }"""
          server.upload spec: uploadSpec, buildInfo: buildInfo, failNoOp: false
        }
      }
      post {
        failure {
          script {
            sendFailureEmail(STAGE_NAME)
          }
        }
      }
    }
    stage('Upload & Promotion Config') {
      when {
        buildingTag()
      }
      steps {
        unstash 'artifacts-ubuntu-focal'
        unstash 'artifacts-ubuntu-jammy'
        unstash 'artifacts-ubuntu-noble'
        unstash 'artifacts-rocky-8'
        unstash 'artifacts-rocky-9'

        script {
          def server = Artifactory.server 'zextras-artifactory'
          def buildInfo
          def uploadSpec
          def config

          //ubuntu
          buildInfo = Artifactory.newBuildInfo()
          buildInfo.name += '-ubuntu'
          uploadSpec = """{
            "files": [
              {
                "pattern": "artifacts/*focal*.deb",
                "target": "ubuntu-rc/pool/",
                "props": "deb.distribution=focal;deb.component=main;deb.architecture=amd64;vcs.revision=${env.GIT_COMMIT}"
              },
              {
                "pattern": "artifacts/*jammy*.deb",
                "target": "ubuntu-rc/pool/",
                "props": "deb.distribution=jammy;deb.component=main;deb.architecture=amd64;vcs.revision=${env.GIT_COMMIT}"
              },
              {
                "pattern": "artifacts/*noble*.deb",
                "target": "ubuntu-rc/pool/",
                "props": "deb.distribution=noble;deb.component=main;deb.architecture=amd64;vcs.revision=${env.GIT_COMMIT}"
              }
            ]
          }"""
          server.upload spec: uploadSpec, buildInfo: buildInfo, failNoOp: false
          config = [
             'buildName'          : buildInfo.name,
             'buildNumber'        : buildInfo.number,
             'sourceRepo'         : 'ubuntu-rc',
             'targetRepo'         : 'ubuntu-release',
             'comment'            : 'Do not change anything! Just press the button',
             'status'             : 'Released',
             'includeDependencies': false,
             'copy'               : true,
             'failFast'           : true
          ]
          Artifactory.addInteractivePromotion server: server,
          promotionConfig: config,
          displayName: 'Ubuntu Promotion to Release'
          server.publishBuildInfo buildInfo

          //rhel8
          buildInfo = Artifactory.newBuildInfo()
          buildInfo.name += "-centos8"
          uploadSpec = """{
            "files": [
              {
                "pattern": "artifacts/(carbonio-message-dispatcher-ce)-(*).el8.x86_64.rpm",
                "target": "centos8-rc/zextras/{1}/{1}-{2}.el8.x86_64.rpm",
                "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras;vcs.revision=${env.GIT_COMMIT}"
              }
            ]
          }"""
          server.upload spec: uploadSpec, buildInfo: buildInfo, failNoOp: false
          config = [
             'buildName'          : buildInfo.name,
             'buildNumber'        : buildInfo.number,
             'sourceRepo'         : 'centos8-rc',
             'targetRepo'         : 'centos8-release',
             'comment'            : 'Do not change anything! Just press the button',
             'status'             : 'Released',
             'includeDependencies': false,
             'copy'               : true,
             'failFast'           : true
          ]
          Artifactory.addInteractivePromotion server: server,
          promotionConfig: config,
          displayName: 'RHEL8 Promotion to Release'
          server.publishBuildInfo buildInfo

          //rhel9
          buildInfo = Artifactory.newBuildInfo()
          buildInfo.name += "-rhel9"
          uploadSpec = """{
            "files": [
              {
                "pattern": "artifacts/(carbonio-message-dispatcher-ce)-(*).el9.x86_64.rpm",
                "target": "rhel9-rc/zextras/{1}/{1}-{2}.el9.x86_64.rpm",
                "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras;vcs.revision=${env.GIT_COMMIT}"
              }
            ]
          }"""
          server.upload spec: uploadSpec, buildInfo: buildInfo, failNoOp: false
          config = [
             'buildName'          : buildInfo.name,
             'buildNumber'        : buildInfo.number,
             'sourceRepo'         : 'rhel9-rc',
             'targetRepo'         : 'rhel9-release',
             'comment'            : 'Do not change anything! Just press the button',
             'status'             : 'Released',
             'includeDependencies': false,
             'copy'               : true,
             'failFast'           : true
          ]
          Artifactory.addInteractivePromotion server: server,
          promotionConfig: config,
          displayName: 'RHEL9 Promotion to Release'
          server.publishBuildInfo buildInfo
        }
      }
      post {
        failure {
          script {
            sendFailureEmail(STAGE_NAME)
          }
        }
      }
    }
  }
}

void sendFailureEmail(String step) {
  def commitInfo =sh(
     script: 'git log -1 --pretty=tformat:\'<ul><li>Revision: %H</li><li>Title: %s</li><li>Author: %ae</li></ul>\'',
     returnStdout: true
  )
  emailext body: """\
    <b>${step.capitalize()}</b> step has failed on trunk.<br /><br />
    Last commit info: <br />
    ${commitInfo}<br /><br />
    Check the failing build at the <a href=\"${BUILD_URL}\">following link</a><br />
  """,
  subject: "[MESSAGE DISPATCHER TRUNK FAILURE] Trunk ${step} step failure",
  to: FAILURE_EMAIL_RECIPIENTS
}
