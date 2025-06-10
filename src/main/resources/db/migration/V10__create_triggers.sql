CREATE OR REPLACE FUNCTION set_updated_at_timestamp()
    RETURNS trigger
    LANGUAGE plpgsql
AS
$$
BEGIN
    NEW.updated_at := now();
    RETURN NEW;
END;
$$;

CREATE TRIGGER set_updated_at_on_nlip_messages
    BEFORE UPDATE
    ON nlip_messages
    FOR EACH ROW
EXECUTE PROCEDURE set_updated_at_timestamp();

CREATE TRIGGER set_updated_at_on_nlip_submessages
    BEFORE UPDATE
    ON nlip_submessages
    FOR EACH ROW
EXECUTE PROCEDURE set_updated_at_timestamp();

CREATE TRIGGER set_updated_at_on_conversations
    BEFORE UPDATE
    ON conversations
    FOR EACH ROW
EXECUTE PROCEDURE set_updated_at_timestamp();

CREATE TRIGGER set_updated_at_on_internal_messages
    BEFORE UPDATE
    ON internal_messages
    FOR EACH ROW
EXECUTE PROCEDURE set_updated_at_timestamp();

CREATE OR REPLACE FUNCTION update_conversation_timestamp()
    RETURNS trigger
    LANGUAGE plpgsql
AS
$$
BEGIN
    UPDATE conversations
    SET updated_at = now()
    WHERE id = NEW.conversation_id;
    RETURN NEW;
END;
$$;

CREATE TRIGGER touch_conversation_on_new_message
    AFTER INSERT
    ON internal_messages
    FOR EACH ROW
EXECUTE PROCEDURE update_conversation_timestamp();

CREATE OR REPLACE FUNCTION set_internal_user_accounts_last_modified_timestamp()
    RETURNS trigger
    LANGUAGE plpgsql
AS
$$
BEGIN
    NEW.last_modified_timestamp := now();
    RETURN NEW;
END;
$$;

CREATE TRIGGER update_last_modified_on_internal_user_accounts
    BEFORE UPDATE
    ON internal_user_accounts
    FOR EACH ROW
EXECUTE PROCEDURE set_internal_user_accounts_last_modified_timestamp();
