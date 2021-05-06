ALTER TABLE ONLY public.case_payment_orders
    DROP COLUMN effective_from;

ALTER TABLE ONLY public.case_payment_orders_audit
    DROP COLUMN effective_from;

ALTER TABLE ONLY public.case_payment_orders_audit
    DROP COLUMN effective_from_mod;
