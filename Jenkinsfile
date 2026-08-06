// SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

library(
    identifier: 'jenkins-lib-common@v4.5.0',
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
    reuse: [projectType: 'CE'],
    flywayGuard: [
        migrationPaths: ['carbonio-message-dispatcher-auth/src/main/resources/db/migration'],
    ],
)
