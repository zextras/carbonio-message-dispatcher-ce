// SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

library(
    identifier: 'jenkins-lib-common@v4.11.0',
    retriever: modernSCM([
        $class: 'GitSCMSource',
        credentialsId: 'jenkins-integration-with-github-account',
        remote: 'git@github.com:zextras/jenkins-lib-common.git'
    ])
)

dt3_pipeline(
    repoName: 'carbonio-message-dispatcher-ce',
    appModule: 'carbonio-message-dispatcher-auth',
    mavenPublish: [],
    packaging: [
        prepare: true,
        addCarbonioRepos: true,
        preBuildScript: '''
            cp carbonio-message-dispatcher-auth/target/carbonio-message-dispatcher-auth-*-fatjar.jar package/carbonio-message-dispatcher-auth.jar
        ''',
    ],
    docker: [
        [
            dockerfile: 'docker/auth-sidecar/Dockerfile',
            imageName: 'carbonio-message-dispatcher-ce-auth-sidecar',
            platforms: ['linux/amd64', 'linux/arm64'] as Set,
            title: 'Carbonio Message Dispatcher CE Auth Sidecar',
            description: 'Carbonio Message Dispatcher Community Edition Auth Sidecar',
        ],
        [
            dockerfile: 'docker/http-sidecar/Dockerfile',
            imageName: 'carbonio-message-dispatcher-ce-http-sidecar',
            platforms: ['linux/amd64', 'linux/arm64'] as Set,
            title: 'Carbonio Message Dispatcher CE HTTP Sidecar',
            description: 'Carbonio Message Dispatcher Community Edition HTTP Sidecar',
        ],
        [
            dockerfile: 'docker/xmpp-sidecar/Dockerfile',
            imageName: 'carbonio-message-dispatcher-ce-xmpp-sidecar',
            platforms: ['linux/amd64', 'linux/arm64'] as Set,
            title: 'Carbonio Message Dispatcher CE XMPP Sidecar',
            description: 'Carbonio Message Dispatcher Community Edition XMPP Sidecar',
        ],
    ],
    reuse: [projectType: 'CE'],
    flywayGuard: [
        migrationPaths: ['carbonio-message-dispatcher-auth/src/main/resources/db/migration'],
    ],
)
