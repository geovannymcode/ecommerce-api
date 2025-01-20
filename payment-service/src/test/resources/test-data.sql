-- Limpia los datos de la tabla antes de insertar
TRUNCATE TABLE credit_cards RESTART IDENTITY CASCADE;

insert into credit_cards(id, customer_name, card_number, cvv, expiry_month, expiry_year)
values (1, 'Geovanny', '1111222233334444', '123', 2, 2030),
       (2, 'Elena', '1234123412341234', '123', 10, 2030);
