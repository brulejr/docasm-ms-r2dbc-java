CREATE TABLE IF NOT EXISTS t_lookup_value (
    lv_id SERIAL PRIMARY KEY,
    lv_entity_type VARCHAR(64) NOT NULL,
    lv_entity_id NUMBER,
    lv_value_type VARCHAR(64) NOT NULL,
    lv_value VARCHAR(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS t_document (
    do_id SERIAL PRIMARY KEY,
    do_guid UUID NOT NULL,
    do_name VARCHAR(64) NOT NULL,
    do_type VARCHAR(64) NOT NULL,
    do_created_by VARCHAR(64),
    do_created_on TIMESTAMP,
    do_modified_by VARCHAR(64),
    do_modified_on TIMESTAMP
);

CREATE TABLE IF NOT EXISTS t_document_section (
    ds_id SERIAL PRIMARY KEY,
    ds_guid UUID NOT NULL,
    ds_do_id NUMBER NOT NULL,
    ds_name VARCHAR(64) NOT NULL,
    ds_type VARCHAR(64) NOT NULL,
    ds_created_by VARCHAR(64),
    ds_created_on TIMESTAMP,
    ds_modified_by VARCHAR(64),
    ds_modified_on TIMESTAMP,
    FOREIGN KEY (ds_do_id) REFERENCES t_document(do_id)
);

