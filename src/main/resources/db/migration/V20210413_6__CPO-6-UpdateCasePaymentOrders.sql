ALTER TABLE public.case_payment_orders_audit
     ADD COLUMN IF NOT EXISTS created_timestamp_mod BOOLEAN;
