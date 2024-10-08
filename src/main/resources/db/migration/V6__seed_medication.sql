INSERT INTO medication (name, disabled, band, rating, id_active_ingredient, id_category, dosage, quantity_card, id_user, is_valid, max_time, time_between)
VALUES ('Dipirona 500mg', FALSE, 1, 4.5,
        (SELECT id FROM active_ingredient WHERE description = 'Dipirona'),
        (SELECT id FROM category WHERE description = 'Analgésico'),
        500, 10, NULL, TRUE, 6.0, 8.0);

INSERT INTO medication (name, disabled, band, rating, id_active_ingredient, id_category, dosage, quantity_card, id_user, is_valid, max_time, time_between)
VALUES ('Paracetamol 750mg', FALSE, 1, 4.7,
        (SELECT id FROM active_ingredient WHERE description = 'Paracetamol'),
        (SELECT id FROM category WHERE description = 'Antipirético'),
        750, 12, NULL, TRUE, 8.0, 6.0);

INSERT INTO medication (name, disabled, band, rating, id_active_ingredient, id_category, dosage, quantity_card, id_user, is_valid, max_time, time_between)
VALUES ('Ibuprofeno 600mg', FALSE, 1, 4.6,
        (SELECT id FROM active_ingredient WHERE description = 'Ibuprofeno'),
        (SELECT id FROM category WHERE description = 'Anti-inflamatório'),
        600, 8, NULL, TRUE, 8.0, 12.0);

INSERT INTO medication (name, disabled, band, rating, id_active_ingredient, id_category, dosage, quantity_card, id_user, is_valid, max_time, time_between)
VALUES ('Amoxicilina 500mg', FALSE, 2, 4.8,
        (SELECT id FROM active_ingredient WHERE description = 'Amoxicilina'),
        (SELECT id FROM category WHERE description = 'Antibiótico'),
        500, 16, NULL, TRUE, 12.0, 6.0);

INSERT INTO medication (name, disabled, band, rating, id_active_ingredient, id_category, dosage, quantity_card, id_user, is_valid, max_time, time_between)
VALUES ('Losartana 50mg', FALSE, 1, 4.9,
        (SELECT id FROM active_ingredient WHERE description = 'Losartana'),
        (SELECT id FROM category WHERE description = 'Hipotensor'),
        50, 30, NULL, TRUE, 12.0, 24.0);

INSERT INTO medication (name, disabled, band, rating, id_active_ingredient, id_category, dosage, quantity_card, id_user, is_valid, max_time, time_between)
VALUES ('Metformina 850mg', FALSE, 1, 4.5,
        (SELECT id FROM active_ingredient WHERE description = 'Metformina'),
        (SELECT id FROM category WHERE description = 'Antidiabético'),
        850, 20, NULL, TRUE, 8.0, 12.0);

INSERT INTO medication (name, disabled, band, rating, id_active_ingredient, id_category, dosage, quantity_card, id_user, is_valid, max_time, time_between)
VALUES ('Ranitidina 150mg', FALSE, 1, 4.3,
        (SELECT id FROM active_ingredient WHERE description = 'Ranitidina'),
        (SELECT id FROM category WHERE description = 'Anti-inflamatório'),
        150, 20, NULL, TRUE, 12.0, 8.0);

INSERT INTO medication (name, disabled, band, rating, id_active_ingredient, id_category, dosage, quantity_card, id_user, is_valid, max_time, time_between)
VALUES ('Omeprazol 20mg', FALSE, 1, 4.4,
        (SELECT id FROM active_ingredient WHERE description = 'Omeprazol'),
        (SELECT id FROM category WHERE description = 'Anti-inflamatório'),
        20, 30, NULL, TRUE, 8.0, 24.0);

INSERT INTO medication (name, disabled, band, rating, id_active_ingredient, id_category, dosage, quantity_card, id_user, is_valid, max_time, time_between)
VALUES ('Simvastatina 40mg', FALSE, 1, 4.8,
        (SELECT id FROM active_ingredient WHERE description = 'Simvastatina'),
        (SELECT id FROM category WHERE description = 'Cardioprotetor'),
        40, 10, NULL, TRUE, 8.0, 24.0);

INSERT INTO medication (name, disabled, band, rating, id_active_ingredient, id_category, dosage, quantity_card, id_user, is_valid, max_time, time_between)
VALUES ('Aspirina 500mg', FALSE, 1, 4.7,
        (SELECT id FROM active_ingredient WHERE description = 'Aspirina'),
        (SELECT id FROM category WHERE description = 'Cardioprotetor'),
        500, 12, NULL, TRUE, 8.0, 12.0);

INSERT INTO medication (name, disabled, band, rating, id_active_ingredient, id_category, dosage, quantity_card, id_user, is_valid, max_time, time_between)
VALUES ('Clopidogrel 75mg', FALSE, 1, 4.7,
        (SELECT id FROM active_ingredient WHERE description = 'Clopidogrel'),
        (SELECT id FROM category WHERE description = 'Anticoagulante'),
        75, 20, NULL, TRUE, 8.0, 12.0);

INSERT INTO medication (name, disabled, band, rating, id_active_ingredient, id_category, dosage, quantity_card, id_user, is_valid, max_time, time_between)
VALUES ('Prednisona 20mg', FALSE, 1, 4.5,
        (SELECT id FROM active_ingredient WHERE description = 'Prednisona'),
        (SELECT id FROM category WHERE description = 'Anti-inflamatório'),
        20, 10, NULL, TRUE, 8.0, 24.0);

INSERT INTO medication (name, disabled, band, rating, id_active_ingredient, id_category, dosage, quantity_card, id_user, is_valid, max_time, time_between)
VALUES ('Azitromicina 500mg', FALSE, 1, 4.6,
        (SELECT id FROM active_ingredient WHERE description = 'Azitromicina'),
        (SELECT id FROM category WHERE description = 'Antibiótico'),
        500, 10, NULL, TRUE, 12.0, 24.0);

INSERT INTO medication (name, disabled, band, rating, id_active_ingredient, id_category, dosage, quantity_card, id_user, is_valid, max_time, time_between)
VALUES ('Enalapril 10mg', FALSE, 1, 4.4,
        (SELECT id FROM active_ingredient WHERE description = 'Enalapril'),
        (SELECT id FROM category WHERE description = 'Hipotensor'),
        10, 10, NULL, TRUE, 8.0, 24.0);

INSERT INTO medication (name, disabled, band, rating, id_active_ingredient, id_category, dosage, quantity_card, id_user, is_valid, max_time, time_between)
VALUES ('Salbutamol 100mcg', FALSE, 1, 4.3,
        (SELECT id FROM active_ingredient WHERE description = 'Salbutamol'),
        (SELECT id FROM category WHERE description = 'Broncodilatador'),
        100, 12, NULL, TRUE, 12.0, 24.0);

INSERT INTO medication (name, disabled, band, rating, id_active_ingredient, id_category, dosage, quantity_card, id_user, is_valid, max_time, time_between)
VALUES ('Prednisona 5mg', FALSE, 1, 4.5,
        (SELECT id FROM active_ingredient WHERE description = 'Prednisona'),
        (SELECT id FROM category WHERE description = 'Anti-inflamatório'),
        5, 20, NULL, TRUE, 8.0, 24.0);

INSERT INTO medication (name, disabled, band, rating, id_active_ingredient, id_category, dosage, quantity_card, id_user, is_valid, max_time, time_between)
VALUES ('Hidroxicloroquina 200mg', FALSE, 1, 4.5,
        (SELECT id FROM active_ingredient WHERE description = 'Hidroxicloroquina'),
        (SELECT id FROM category WHERE description = 'Antimalárico'),
        200, 20, NULL, TRUE, 12.0, 24.0);

INSERT INTO medication (name, disabled, band, rating, id_active_ingredient, id_category, dosage, quantity_card, id_user, is_valid, max_time, time_between)
VALUES ('Cetirizina 10mg', FALSE, 1, 4.4,
        (SELECT id FROM active_ingredient WHERE description = 'Cetirizina'),
        (SELECT id FROM category WHERE description = 'Antialérgico'),
        10, 10, NULL, TRUE, 12.0, 24.0);

INSERT INTO medication (name, disabled, band, rating, id_active_ingredient, id_category, dosage, quantity_card, id_user, is_valid, max_time, time_between)
VALUES ('Fluoxetina 20mg', FALSE, 1, 4.8,
        (SELECT id FROM active_ingredient WHERE description = 'Fluoxetina'),
        (SELECT id FROM category WHERE description = 'Antidepressivo'),
        20, 15, NULL, TRUE, 8.0, 24.0);

INSERT INTO medication (name, disabled, band, rating, id_active_ingredient, id_category, dosage, quantity_card, id_user, is_valid, max_time, time_between)
VALUES ('Cetoconazol 200mg', FALSE, 1, 4.6,
        (SELECT id FROM active_ingredient WHERE description = 'Cetoconazol'),
        (SELECT id FROM category WHERE description = 'Antifúngico'),
        200, 10, NULL, TRUE, 8.0, 24.0);