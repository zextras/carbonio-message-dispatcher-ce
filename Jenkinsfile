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
    FAILURE_EMAIL_RECIPIENTS='alberto.pontini@zextras.com'
  }
  stages {
    stage('Build setup') {
      steps {
        checkout scm
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
            sh 'sudo pacur build ubuntu-focal'
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
        stage('Rocky 8') {
          agent {
            node {
              label 'pacur-agent-rocky-8-v1'
            }
          }
          steps {
            unstash 'project'
            sh 'sudo pacur build rocky-8'
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
              archiveArtifacts artifacts: 'artifacts/*.rpm', fingerprint: true
            }
          }
        }
      }
    }
    stage('Upload To Playground') {
      when {
        anyOf {
          branch "main"
        }
      }
      steps {
        unstash 'artifacts-ubuntu-focal'
        unstash 'artifacts-rocky-8'
        script {
          def server = Artifactory.server 'zextras-artifactory'
          def buildInfo
          def uploadSpec
          buildInfo = Artifactory.newBuildInfo()
          uploadSpec = '''{
            "files": [
              {
                "pattern": "artifacts/*focal*.deb",
                "target": "ubuntu-playground/pool/",
                "props": "deb.distribution=focal;deb.component=main;deb.architecture=amd64"
              },{
                "pattern": "artifacts/(carbonio-messaging)-(*).rpm",
                "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
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
  subject: "[CARBONIO MESSAGING TRUNK FAILURE] Trunk ${step} step failure",
  to: FAILURE_EMAIL_RECIPIENTS
}