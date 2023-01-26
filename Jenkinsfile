// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

pipeline {
  options {
    skipDefaultCheckout()
    buildDiscarder(logRotator(numToKeepStr: '5'))
    timeout(time: 1, unit: 'HOURS')
  }
  agent {
    node {
      label 'openjdk11-agent-v1'
    }
  }
  environment {
    NETWORK_OPTS = '--network ci_agent'
    FAILURE_EMAIL_RECIPIENTS='luca.gasparini@zextras.com, noman.alishaukat@zextras.com, perla.polito@zextras.com'
  }

  stages {
    stage('Build setup') {
      steps {
        checkout([
          $class: 'GitSCM',
          branches: scm.branches,
          extensions: [[
            $class: 'CloneOption',
            shallow: true,
            depth:   2,
            timeout: 30
          ]],
          userRemoteConfigs: scm.userRemoteConfigs
        ])
        withCredentials([file(credentialsId: 'jenkins-maven-settings.xml', variable: 'SETTINGS_PATH')]) {
          sh 'cp $SETTINGS_PATH settings-jenkins.xml'
          sh 'mvn -Dmaven.repo.local=$(pwd)/m2 -N wrapper:wrapper'
        }
      }
    }
    stage('Compiling') {
      steps {
        sh './mvnw -Dmaven.repo.local=$(pwd)/m2 -T1C -B -q --settings settings-jenkins.xml compile'
      }
      post {
        failure {
          script {
            if ("main".equals(env.BRANCH_NAME)) {
              sendFailureEmail(STAGE_NAME)
            }
          }
        }
      }
    }
    stage('Stashing for packaging') {
      steps {
        stash includes: '**', name: 'project', useDefaultExcludes: false
      }
    }
    stage('Building packages') {
      parallel {
        stage('Ubuntu 20') {
          agent {
            node {
              label 'pacur-agent-ubuntu-20.04-v1'
            }
          }
          steps {
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
sudo echo "deb https://zextras.jfrog.io/artifactory/ubuntu-playground focal main" > zextras.list
sudo mv zextras.list /etc/apt/sources.list.d/
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys 52FD40243E584A21
'''
            sh '''
              ./mvnw package -Dmaven.main.skip -Dmaven.repo.local=$(pwd)/m2
              mkdir /tmp/messaging
              mv * /tmp/messaging
              sudo pacur build ubuntu-focal /tmp/messaging
            '''
            stash includes: 'artifacts/', name: 'artifacts-ubuntu-focal'
          }
          post {
            failure {
              script {
                if (env.BRANCH_NAME.equals("main")) {
                  sendFailureEmail(STAGE_NAME)
                }
              }
            }
            always {
              archiveArtifacts artifacts: 'artifacts/*.deb', fingerprint: true
            }
          }
        }
        /* stage('Rocky 8') {
          agent {
            node {
              label 'pacur-agent-rocky-8-v1'
            }
          }
          steps {
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
            }
            sh '''
              ./mvnw package -Dmaven.main.skip -Dmaven.repo.local=$(pwd)/m2
              mkdir /tmp/messaging
              mv * /tmp/messaging
              sudo pacur build rocky-8 /tmp/messaging
            '''
            stash includes: 'artifacts/', name: 'artifacts-rocky-8'
          }
          post {
            failure {
              script {
                if (env.BRANCH_NAME.equals("main")) {
                  sendFailureEmail(STAGE_NAME)
                }
              }
            }
            always {
              archiveArtifacts artifacts: 'artifacts *//*.rpm', fingerprint: true
            }
          }
        } */
      }
    }
    stage('Upload To Devel') {
      when {
        branch "main"
      }
      steps {
        unstash 'artifacts-ubuntu-focal'
        script {
          def server = Artifactory.server 'zextras-artifactory'
          def buildInfo
          def uploadSpec
          buildInfo = Artifactory.newBuildInfo()
          uploadSpec = '''{
            "files": [
              {
                "pattern": "artifacts/*.deb",
                "target": "ubuntu-devel/pool/",
                "props": "deb.distribution=focal;deb.component=main;deb.architecture=amd64"
              }
            ]
          }'''
          server.upload spec: uploadSpec, buildInfo: buildInfo, failNoOp: false
        }
      }
      post {
        failure {
          script {
            if (env.BRANCH_NAME.equals("main")) {
              sendFailureEmail(STAGE_NAME)
            }
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
  subject: "[CARBONIO CHATS MESSAGING TRUNK FAILURE] Trunk ${step} step failure",
  to: FAILURE_EMAIL_RECIPIENTS
}
