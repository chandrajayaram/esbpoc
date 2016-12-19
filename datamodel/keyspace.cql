CREATE KEYSPACE message_store_ks
WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1};
