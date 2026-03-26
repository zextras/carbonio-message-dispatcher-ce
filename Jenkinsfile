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
    identifier: 'jenkins-lib-common@1.4.0',
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
                    buildPackages([
                        buildStageConfig: [
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
                                    '''
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
                                    '''
                                ],
                            ]
                        ]
                    ])
                }
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
                            projectName: 'carbonio-message-dispatcher',
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
