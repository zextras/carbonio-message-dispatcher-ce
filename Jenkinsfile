// SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

library(
    identifier: 'jenkins-lib-common@v4.13.0',
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
        parallelBuilds: false,
        aarch64: true,
        artifactsForDocker: true,
        preBuildScript: '''
            cp -a carbonio-message-dispatcher-auth/target/carbonio-message-dispatcher-auth-*-fatjar.jar package/carbonio-message-dispatcher-auth.jar
            cp -a carbonio-message-dispatcher-auth/target/carbonio-message-dispatcher-auth-*-fatjar.jar docker/carbonio-message-dispatcher-auth.jar
        ''',
    ],
    // Runs in the buildah-only container; artifacts/ already holds the packaging stage's .debs.
    dockerPreScript: '''
        cp -a carbonio-message-dispatcher-auth/target/carbonio-message-dispatcher-auth-*-fatjar.jar docker/carbonio-message-dispatcher-auth.jar
        for a in amd64 arm64; do
            ls artifacts/carbonio-message-dispatcher-ce_*jammy*_$a.deb >/dev/null 2>&1 || {
                echo "ERROR: no artifacts/carbonio-message-dispatcher-ce_*jammy*_$a.deb — the packaging.artifactsForDocker unstash produced no jammy $a package. The buildah build below builds both arches, so a missing one fails later inside the Dockerfile with a bare 'ls: cannot access'." >&2
                exit 1
            }
        done
        STORAGE_DRIVER=overlay buildah build --platform linux/amd64,linux/arm64 -f docker/mongooseim-base/Dockerfile --manifest mongooseim-ce:6.6.0-2 .
        STORAGE_DRIVER=overlay buildah manifest push --all mongooseim-ce:6.6.0-2 docker://registry.dev.zextras.com/dev/mongooseim-ce:6.6.0-2
        STORAGE_DRIVER=overlay buildah manifest push --all mongooseim-ce:6.6.0-2 docker://registry.dev.zextras.com/dev/mongooseim-ce:latest
    ''',
    docker: [
        [
            dockerfile: 'docker/Dockerfile',
            imageName: 'carbonio-message-dispatcher-ce',
            platforms: ['linux/amd64', 'linux/arm64'] as Set,
            title: 'Carbonio Message Dispatcher CE',
            description: 'Carbonio Message Dispatcher CE Service',
        ],
        [
            dockerfile: 'docker/auth-sidecar/Dockerfile',
            imageName: 'carbonio-message-dispatcher-ce-auth-sidecar',
            platforms: ['linux/amd64', 'linux/arm64'] as Set,
            title: 'Carbonio Message Dispatcher CE Auth Sidecar',
            description: 'Carbonio Message Dispatcher CE Auth Sidecar',
        ],
        [
            dockerfile: 'docker/http-sidecar/Dockerfile',
            imageName: 'carbonio-message-dispatcher-ce-http-sidecar',
            platforms: ['linux/amd64', 'linux/arm64'] as Set,
            title: 'Carbonio Message Dispatcher CE HTTP Sidecar',
            description: 'Carbonio Message Dispatcher CE HTTP Sidecar',
        ],
        [
            dockerfile: 'docker/xmpp-sidecar/Dockerfile',
            imageName: 'carbonio-message-dispatcher-ce-xmpp-sidecar',
            platforms: ['linux/amd64', 'linux/arm64'] as Set,
            title: 'Carbonio Message Dispatcher CE XMPP Sidecar',
            description: 'Carbonio Message Dispatcher CE XMPP Sidecar',
        ],
        [
            dockerfile: 'docker/auth/Dockerfile',
            imageName: 'carbonio-message-dispatcher-ce-auth',
            platforms: ['linux/amd64', 'linux/arm64'] as Set,
            title: 'Carbonio Message Dispatcher CE Auth',
            description: 'Carbonio Message Dispatcher CE Auth Service',
        ],
        [
            dockerfile: 'docker/mongoose/Dockerfile',
            imageName: 'carbonio-message-dispatcher-ce-mongoose',
            platforms: ['linux/amd64', 'linux/arm64'] as Set,
            title: 'Carbonio Message Dispatcher CE Mongoose',
            description: 'Carbonio Message Dispatcher CE MongooseIM Service',
        ],
    ],
    reuse: [projectType: 'CE'],
    flywayGuard: [
        migrationPaths: ['carbonio-message-dispatcher-auth/src/main/resources/db/migration'],
    ],
)
