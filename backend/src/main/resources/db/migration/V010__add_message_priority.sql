ALTER TABLE message_dispatch
    ADD COLUMN priority INT NOT NULL DEFAULT 20;

ALTER TABLE message_dispatch
    ALTER COLUMN priority DROP DEFAULT;

DROP INDEX idx_md_claim ON message_dispatch;
CREATE INDEX idx_md_claim ON message_dispatch (status, priority, next_retry_at, id);
