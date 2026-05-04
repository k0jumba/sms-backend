CREATE TABLE event_publication (
    id                   UUID                        NOT NULL PRIMARY KEY,
    listener_id          VARCHAR(255)                NOT NULL,
    event_type           VARCHAR(255)                NOT NULL,
    serialized_event     VARCHAR(255)                NOT NULL,
    publication_date     TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    completion_date      TIMESTAMP(6) WITH TIME ZONE,
    last_resubmission_date TIMESTAMP(6) WITH TIME ZONE,
    completion_attempts  INTEGER                     NOT NULL,
    status               VARCHAR(255)
);