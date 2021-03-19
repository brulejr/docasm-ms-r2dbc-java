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
    do_created_by VARCHAR(64),
    do_created_on TIMESTAMP,
    do_modified_by VARCHAR(64),
    do_modified_on TIMESTAMP
);

