## [](https://github.com/zextras/carbonio-message-dispatcher-ce/compare/v0.16.0...v) (2026-01-09)

### Bug Fixes

* replace use of non existent function ([#71](https://github.com/zextras/carbonio-message-dispatcher-ce/issues/71)) ([0cec49d](https://github.com/zextras/carbonio-message-dispatcher-ce/commit/0cec49d5db152096429efe5a38d5fe7165e92e21))
## [0.16.0](https://github.com/zextras/carbonio-message-dispatcher-ce/compare/0.14.1...v0.16.0) (2025-11-17)

### Features

* add ubuntu 24.04 (ubuntu-noble) support ([#34](https://github.com/zextras/carbonio-message-dispatcher-ce/issues/34)) ([c608f31](https://github.com/zextras/carbonio-message-dispatcher-ce/commit/c608f31398efdfd71c7bd80258932a8f97d3a5c5))
* create docker compose and Docker files for mongooseIm and db ([#44](https://github.com/zextras/carbonio-message-dispatcher-ce/issues/44)) ([ab47713](https://github.com/zextras/carbonio-message-dispatcher-ce/commit/ab477136ca74900cdda977e18ba10a707f0f658b))
* **WSC-1237:** enable mod_ping module ([#31](https://github.com/zextras/carbonio-message-dispatcher-ce/issues/31)) ([3838283](https://github.com/zextras/carbonio-message-dispatcher-ce/commit/38382835b009264032d421a126ec716f78f7a147))

### Bug Fixes

* apply proper privileges to erlang log folder ([#41](https://github.com/zextras/carbonio-message-dispatcher-ce/issues/41)) ([9d0b84c](https://github.com/zextras/carbonio-message-dispatcher-ce/commit/9d0b84c1e1de7c106ca61a72f20bb80c308e85da))
* ci: rhel8 build stage ([258dad0](https://github.com/zextras/carbonio-message-dispatcher-ce/commit/258dad08ba15d40e363f240027f540f635af12b1))
* log path is not properly used ([#38](https://github.com/zextras/carbonio-message-dispatcher-ce/issues/38)) ([1e26159](https://github.com/zextras/carbonio-message-dispatcher-ce/commit/1e261593de9e8614908005caf4cfee74203939a7))
* move jar from /usr/bin to /usr/share to follow the FHS standard ([#33](https://github.com/zextras/carbonio-message-dispatcher-ce/issues/33)) ([82d2c23](https://github.com/zextras/carbonio-message-dispatcher-ce/commit/82d2c230d4d776adaf8c9eac748ca8a3826957c5))
* revert WantedBy for compatibility with older systems ([#47](https://github.com/zextras/carbonio-message-dispatcher-ce/issues/47)) ([2a574e1](https://github.com/zextras/carbonio-message-dispatcher-ce/commit/2a574e11c2ff11c2227ec3129836160c3b6f9ca3))
## [0.14.1](https://github.com/zextras/carbonio-message-dispatcher-ce/compare/0.14.0...0.14.1) (2024-06-21)
## [0.14.0](https://github.com/zextras/carbonio-message-dispatcher-ce/compare/0.13.0...0.14.0) (2024-05-28)

### Features

* add patch to let mongoose send push http requests for groupchat ([#24](https://github.com/zextras/carbonio-message-dispatcher-ce/issues/24)) ([d8dcadd](https://github.com/zextras/carbonio-message-dispatcher-ce/commit/d8dcaddee0de98369c5e22c9ecaab4d9db357ebf))
## [0.13.0](https://github.com/zextras/carbonio-message-dispatcher-ce/compare/0.12.0...0.13.0) (2024-02-14)

### Features

* move to yap agent and add rhel9 support ([#19](https://github.com/zextras/carbonio-message-dispatcher-ce/issues/19)) ([c04532a](https://github.com/zextras/carbonio-message-dispatcher-ce/commit/c04532a7d5871a744dd2db49ed0feeef047ea5b3))

### Bug Fixes

* *.hcl: apply corrections to validate with hclfmt ([#23](https://github.com/zextras/carbonio-message-dispatcher-ce/issues/23)) ([ef97b3f](https://github.com/zextras/carbonio-message-dispatcher-ce/commit/ef97b3f8a34d07384f5cbda1c24b7b2d019969a1))
* ci: typo on promotion target repo ([2829b98](https://github.com/zextras/carbonio-message-dispatcher-ce/commit/2829b989e4af69634e8a26533ee08291e78cc301))
## [0.12.0](https://github.com/zextras/carbonio-message-dispatcher-ce/compare/857341625d5ec29d4213dab91199da5d934a3677...0.12.0) (2023-10-02)

### Features

* [CHATS-836] Improve mongooseim performance changing config values ([#13](https://github.com/zextras/carbonio-message-dispatcher-ce/issues/13)) ([066ad25](https://github.com/zextras/carbonio-message-dispatcher-ce/commit/066ad2516f9e7c073d3084f18b411a61b0887572))
* CHATS-764 Add message dispatcher auth on consul ([#12](https://github.com/zextras/carbonio-message-dispatcher-ce/issues/12)) ([769e51d](https://github.com/zextras/carbonio-message-dispatcher-ce/commit/769e51db824644724072900941f80df90fe1c21b))
* WSC-1003 update pre-commit-config yaml ([#15](https://github.com/zextras/carbonio-message-dispatcher-ce/issues/15)) ([3211221](https://github.com/zextras/carbonio-message-dispatcher-ce/commit/3211221659b8851fbf6a4e051a18434798e2efeb))

### Bug Fixes

* archiveArtifacts for Rocky8 package build ([#10](https://github.com/zextras/carbonio-message-dispatcher-ce/issues/10)) ([8573416](https://github.com/zextras/carbonio-message-dispatcher-ce/commit/857341625d5ec29d4213dab91199da5d934a3677))
