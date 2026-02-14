CREATE INDEX idx_md_dojang_type_created
    ON message_dispatch (dojang_id, message_type, created_at);
