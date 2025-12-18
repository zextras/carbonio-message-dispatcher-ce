-- SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
--
-- SPDX-License-Identifier: AGPL-3.0-only

-- Cluster node discovery used by CETS
CREATE TABLE discovery_nodes (
                                 cluster_name varchar(250) NOT NULL,
                                 node_name varchar(250) NOT NULL,
                                 node_num INT NOT NULL,
                                 address varchar(250) NOT NULL DEFAULT '', -- empty means we should ask DNS
                                 updated_timestamp BIGINT NOT NULL, -- in seconds
                                 PRIMARY KEY (cluster_name, node_name)
);
CREATE UNIQUE INDEX i_discovery_nodes_node_num ON discovery_nodes USING BTREE(cluster_name, node_num);

UPDATE database_version SET version = '6.2.0'
