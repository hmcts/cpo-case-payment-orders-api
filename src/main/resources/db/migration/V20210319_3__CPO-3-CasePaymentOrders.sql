CREATE TABLE public.case_payment_orders (
                         id uuid not null PRIMARY KEY,
                         created_timestamp timestamp without time zone not null,
                         effective_from timestamp without time zone not null,
                         case_id BIGINT not null,
                         case_type_id character varying(70) not null,
                         action character varying(70) not null,
                         responsible_party character varying(1024) not null,
                         order_reference character varying(70) not null,
                         created_by character varying(70) not null
);

ALTER TABLE ONLY public.case_payment_orders
    ADD CONSTRAINT unique_case_id_order_reference UNIQUE (case_id, order_reference);

-- PostgreSQL automatically creates an index for each unique constraint and primary key constraint to enforce uniqueness
-- Thus, it is not necessary to create an index explicitly for primary key columns or unique constraints
-- See https://www.postgresql.org/docs/current/sql-createtable.html

CREATE INDEX case_payment_orders_order_reference_idx ON public.case_payment_orders USING btree (order_reference);
