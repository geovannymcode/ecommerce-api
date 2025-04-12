-- Crear secuencia para la tabla de historial de estado de orden
create sequence order_status_history_id_seq start with 1 increment by 50;

-- Crear tabla order_status_history
create table order_status_history (
                                      id              bigint default nextval('order_status_history_id_seq') not null,
                                      order_number    text not null,
                                      previous_status text,
                                      new_status      text not null,
                                      comments        text,
                                      changed_at      timestamp not null,
                                      changed_by      text,
                                      primary key (id)
);