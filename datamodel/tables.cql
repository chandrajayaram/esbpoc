CREATE TABLE message_store_ks.message (
    service_name text,
    create_ts timestamp,
    payload_id text,
    soap_action text,
    mime_headers map<text,text>,
    request_xml blob,
    PRIMARY KEY (service_name, create_ts)
)WITH CLUSTERING ORDER BY(create_ts desc);
