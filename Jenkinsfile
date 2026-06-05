// SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

library(
    identifier: 'jenkins-lib-common@dt3-pipeline',
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
        pkgbuildPath: 'package/PKGBUILD',
        prepare: true,
        addCarbonioRepos: true,
        ubuntuSinglePkg: false,
        rockySinglePkg: false,
        overrides: [
            ubuntu: [
                preBuildScript: '''
                    cp carbonio-message-dispatcher-auth/target/carbonio-message-dispatcher-auth-*-fatjar.jar package/carbonio-message-dispatcher-auth.jar
                ''',
            ],
            rocky: [
                preBuildScript: '''
                    cp carbonio-message-dispatcher-auth/target/carbonio-message-dispatcher-auth-*-fatjar.jar package/carbonio-message-dispatcher-auth.jar
                ''',
            ],
        ],
    ],
    sonarqube: true,
    reuse: [projectType: 'CE'],
    gitleaks: true
)
