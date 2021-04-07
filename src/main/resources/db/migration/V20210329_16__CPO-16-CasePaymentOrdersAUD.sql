CREATE OR REPLACE FUNCTION revinfo_row_removal() RETURNS TRIGGER AS $$
BEGIN
    DELETE FROM revinfo
    WHERE revinfo.rev = OLD.rev;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;


CREATE TRIGGER revinfo_delete AFTER DELETE on case_payment_orders_audit
    FOR EACH ROW EXECUTE PROCEDURE revinfo_row_removal();
