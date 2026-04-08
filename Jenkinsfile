// SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

library(
    identifier: 'jenkins-dt3-lib@v1.2.0',
    retriever: modernSCM([
        $class: 'GitSCMSource',
        remote: 'git@github.com:zextras/jenkins-dt3-lib.git',
        credentialsId: 'jenkins-integration-with-github-account'
    ])
)

library(
    identifier: 'jenkins-lib-common@1.5.0',
    retriever: modernSCM([
        $class: 'GitSCMSource',
        credentialsId: 'jenkins-integration-with-github-account',
        remote: 'git@github.com:zextras/jenkins-lib-common.git'
    ])
)

properties(defaultPipelineProperties())

pipeline {
    agent {
        node {
            label 'zextras-v1'
        }
    }

    environment {
        JAVA_OPTS = '-Dfile.encoding=UTF8'
        LC_ALL = 'C.UTF-8'
        jenkins_build = 'true'
        MVN_OPTS = '-B'
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '25'))
        skipDefaultCheckout()
        timeout(time: 2, unit: 'HOURS')
    }

    parameters {
        booleanParam(
            name: 'PREPARE_RELEASE',
            defaultValue: false,
            description: 'Check this to prepare a new release (creates pre-release branch and PR)'
        )
    }

    tools {
        jfrog 'jfrog-cli'
    }

    stages {
        stage('Checkout') {
            steps {
                script {
                    checkoutWithMetadata()
                }
            }
        }

        stage('Build jar') {
            steps {
                script {
                    def profile = '-P dev'
                    if (env.TAG_NAME) {
                        profile = '-P prod'
                    }
                    container('jdk-21') {
                        sh """
                            mvn ${MVN_OPTS} clean package ${profile}
                            cp carbonio-message-dispatcher-auth/target/carbonio-message-dispatcher-auth-*-fatjar.jar package/carbonio-message-dispatcher-auth.jar
                            cp carbonio-message-dispatcher-auth/target/carbonio-message-dispatcher-auth-*-fatjar.jar docker/carbonio-message-dispatcher-auth.jar
                        """
                    }
                }
            }
        }

        stage('Build deb/rpm') {
            steps {
                buildPackages([
                    buildStageConfig: [
                        addCarbonioRepos: true,
                        buildFlags: ' -ds ',
                        prepare: true,
                    ]
                ])
            }
        }

        stage('Upload artifacts') {
            steps {
                uploadStage(
                    packages: yapHelper.getPackageNames()
                )
            }
        }

        stage('Prepare Release') {
            agent {
                node {
                    label 'nodejs-v1'
                }
            }
            when {
                allOf {
                    branch 'devel'
                    expression { params.PREPARE_RELEASE == true }
                    not {
                        expression {
                            return env.GIT_COMMIT_MSG.contains('[skip ci]') ||
                                   env.GIT_COMMIT_MSG.contains('chore(release):')
                        }
                    }
                }
            }
            steps {
                script {
                    container('nodejs-20') {
                        prepareRelease(
                            repoName: 'carbonio-message-dispatcher-ce'
                        )
                    }
                }
            }
        }

        stage('Tag for release') {
            when {
                allOf {
                    branch 'devel'
                    expression {
                        return env.GIT_COMMIT_MSG.contains('chore(release):') &&
                               env.GIT_COMMIT_MSG.contains('[skip ci]')
                    }
                }
            }
            steps {
                script {
                    tagRelease()
                }
            }
        }

        stage('Build and Publish Docker Image') {
            when {
                not {
                    expression { env.BRANCH_NAME.startsWith('PR-') }
                }
            }
            steps {
                container('dind') {
                    sh './docker/build_mongoose_docker_img.sh'
                        withDockerRegistry(credentialsId: 'private-registry', url: 'https://registry.dev.zextras.com') {
                            sh '''
                                docker tag mongooseim:latest registry.dev.zextras.com/dev/mongooseim-ce:latest
                                docker push registry.dev.zextras.com/dev/mongooseim-ce:latest
                            '''
                        buildAndPublishDockerImage(
                            projectName: 'carbonio-message-dispatcher-ce',
                            dockerfile: 'docker/Dockerfile',
                            imageTitle: 'Carbonio Message Dispatcher',
                            imageDescription: 'Carbonio Message Dispatcher Service'
                        )
                    }
                }
            }
        }
    }
}
