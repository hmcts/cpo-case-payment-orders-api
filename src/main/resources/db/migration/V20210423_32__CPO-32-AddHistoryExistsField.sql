ALTER TABLE public.case_payment_orders
     ADD COLUMN IF NOT EXISTS history_exists BOOLEAN;

ALTER TABLE public.case_payment_orders_audit
     ADD COLUMN IF NOT EXISTS history_exists BOOLEAN;

ALTER TABLE public.case_payment_orders_audit
     ADD COLUMN IF NOT EXISTS history_exists_mod BOOLEAN;
