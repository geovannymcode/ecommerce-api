create sequence order_event_id_seq start with 1 increment by 50;

create table order_events
(
    id           bigint default nextval('order_event_id_seq') not null,
    order_number text                                         not null references orders (order_number),
    event_id     text                                         not null unique,
    event_type   text                                         not null,
    payload      text                                         not null,
    created_at   timestamp                                    not null,
    updated_at   timestamp,
    primary key (id)
);

create index idx_order_events_order_number on order_events (order_number);
create index idx_order_events_event_type on order_events (event_type);