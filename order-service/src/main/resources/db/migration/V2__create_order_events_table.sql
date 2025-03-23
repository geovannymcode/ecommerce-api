create sequence order_event_id_seq start with 1 increment by 50;

create table order_events (
                              id bigint default nextval('order_event_id_seq') not null,
                              event_id varchar(255) not null,
                              event_type varchar(50) not null,
                              order_number varchar(255) not null,
                              payload text,
                              created_at timestamp,
                              primary key (id)
);

create index idx_order_events_order_number on order_events (order_number);
create index idx_order_events_event_type on order_events (event_type);