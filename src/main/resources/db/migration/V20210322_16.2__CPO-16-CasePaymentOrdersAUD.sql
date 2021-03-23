CREATE SEQUENCE hibernate_sequence INCREMENT 1 MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
    CACHE 1;

CREATE TABLE public.revinfo
(
    rev      integer not null constraint revinfo_pkey primary key,
    revtstmp bigint
);

CREATE TABLE public.case_payment_orders_audit
(
    id                    uuid    not null,
    rev                   integer not null,
    revtype               smallint,
    action                varchar(70),
    action_mod            boolean,
    case_id               bigint,
    case_id_mod           boolean,
    created_by            varchar(70),
    created_by_mod        boolean,
    created_timestamp     timestamp,
    created_timestamp_mod boolean,
    effective_from        timestamp,
    effective_from_mod    boolean,
    order_reference       varchar(70),
    order_reference_mod   boolean,
    responsible_party     varchar(1024),
    responsible_party_mod boolean
);

ALTER TABLE public.case_payment_orders_audit
    ADD CONSTRAINT case_payment_orders_aud_pkey
        PRIMARY KEY (id, rev);

ALTER TABLE public.case_payment_orders_audit
    ADD CONSTRAINT revRevInfoForeignKey
        FOREIGN KEY (rev) REFERENCES public.revinfo;
