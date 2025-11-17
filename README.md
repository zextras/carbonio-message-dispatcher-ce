<!--
SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
SPDX-License-Identifier: AGPL-3.0-only
-->

<div align="center">
  <h1>Carbonio Message Dispatcher</h1>
</div>

<div align="center">
  Message Dispatcher service for Zextras Carbonio  
</div>

***

## Upgrade Mongoose

### Migrations

In order to properly upgrade MongooseIm version follow these steps:

- check for migrations (sql) in mongoose repository and add in package folder following the naming convention
  `pgsql-script-migrations-{VERSION}.sql`
- add this sql statement on the bottom of migrations added like `UPDATE database_version SET version = '{VERSION}'`.
    - This statement is mandatory to apply logic in `carbonio-message-dispatcher-migration` script
- add references to migrations in PKGBUILD file in `source` array and `sha256sums` array
- install migrations in package function in PKGBUILD file like
    -
    `install -Dm644 pgsql-script-migrations-{VERSION}.sql "${pkgdir}/etc/carbonio/message-dispatcher/sql-scripts/migrations/{VERSION}.sql"`

### Update package version

In order to properly update package version follow these steps:

- modify reference in source about the new archive to download like
  `http://github.com/esl/MongooseIM/archive/{VERSION}.tar.gz`
- modify prepare() function in PKGBUILD:
    - ```
  prepare() {
  cd "${srcdir}/MongooseIM-{VERSION}"
  ...
  }
    ```
- modify build() function in PKGBUILD:
    - ```
  build() {
  cd "${srcdir}/MongooseIM-{VERSION}"
  ...
  }
    ```
- modify path for vm.args file in package function in PKGBUILD file like
    -
    `cp "${pkgdir}/opt/zextras/common/lib/mongooseim/releases/{VERSION}/vm.args" "${pkgdir}/etc/carbonio/message-dispatcher/vm.args"`

### Docker

While upgrade mongoose in carbonio-message-dispatcher remember to update also docker image in Dockerfile with same
version.
Update also docker db part changing init.sql. You can copy and paste from
`https://github.com/esl/MongooseIM/blob/master/priv/pg.sql`.

## License 📚

Carbonio Message Dispatcher is the message engine backend service for Zextras
Carbonio Workstream collaboration.

Copyright (C) 2023 Zextras <https://www.zextras.com>

> This program is free software: you can redistribute it and/or modify
> it under the terms of the GNU Affero General Public License as published by
> the Free Software Foundation, version 3 only of the License.
>
> This program is distributed in the hope that it will be useful,
> but WITHOUT ANY WARRANTY; without even the implied warranty of
> MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
> GNU Affero General Public License for more details.
>
> You should have received a copy of the GNU Affero General Public License
> along with this program. If not, see <https://www.gnu.org/licenses/>.

See [COPYING](COPYING) file for the project license details

See [THIRDPARTIES](THIRDPARTIES) file for other licenses details

## Copyright and Licensing notices

All non-software material (such as, for example, names, images, logos,
sounds) is owned by Zextras and is licensed under CC-BY-NC-SA
https://creativecommons.org/licenses/by-nc-sa/4.0/.
Where not specified, all source files owned by Zextras are licensed
under AGPL-3.0-only.

***