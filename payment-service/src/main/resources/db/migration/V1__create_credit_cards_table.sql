create sequence credit_id_seq start with 1 increment by 50;

create table credit_cards
(
    id bigint default nextval('credit_id_seq') not null,
    customer_name varchar   not null,
    card_number   varchar   not null,
    cvv           varchar   not null,
    expiry_month  numeric   not null,
    expiry_year   numeric   not null,
    primary key (id),
    CONSTRAINT cc_card_num_unique UNIQUE (card_number)
);