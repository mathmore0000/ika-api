INSERT INTO medication (name, disabled, band, rating, id_active_ingredient, id_category, dosage, id_user, is_valid, max_taking_time, time_between, quantity_int, quantity_ml)
VALUES ('Dipirona 500mg', FALSE, 1, 4.5,
        (SELECT id FROM active_ingredient WHERE description = 'Dipirona'),
        (SELECT id FROM category WHERE description = 'Analgésico'),
        500, NULL, TRUE, 0.5, 8.0, 20, NULL);

INSERT INTO medication (name, disabled, band, rating, id_active_ingredient, id_category, dosage, id_user, is_valid, max_taking_time, time_between, quantity_int, quantity_ml)
VALUES ('Paracetamol 750mg', FALSE, 1, 4.7,
        (SELECT id FROM active_ingredient WHERE description = 'Paracetamol'),
        (SELECT id FROM category WHERE description = 'Antipirético'),
        750, NULL, TRUE, 1.0, 6.0, 24, NULL);

INSERT INTO medication (name, disabled, band, rating, id_active_ingredient, id_category, dosage, id_user, is_valid, max_taking_time, time_between, quantity_int, quantity_ml)
VALUES ('Ibuprofeno 600mg', FALSE, 1, 4.6,
        (SELECT id FROM active_ingredient WHERE description = 'Ibuprofeno'),
        (SELECT id FROM category WHERE description = 'Anti-inflamatório'),
        600, NULL, TRUE, 1.0, 12.0, 16, NULL);

INSERT INTO medication (name, disabled, band, rating, id_active_ingredient, id_category, dosage, id_user, is_valid, max_taking_time, time_between, quantity_int, quantity_ml)
VALUES ('Amoxicilina 500mg', FALSE, 2, 4.8,
        (SELECT id FROM active_ingredient WHERE description = 'Amoxicilina'),
        (SELECT id FROM category WHERE description = 'Antibiótico'),
        500, NULL, TRUE, 1.0, 6.0, 32, NULL);

INSERT INTO medication (name, disabled, band, rating, id_active_ingredient, id_category, dosage, id_user, is_valid, max_taking_time, time_between, quantity_int, quantity_ml)
VALUES ('Losartana 50mg', FALSE, 1, 4.9,
        (SELECT id FROM active_ingredient WHERE description = 'Losartana'),
        (SELECT id FROM category WHERE description = 'Hipotensor'),
        50, NULL, TRUE, 1.0, 24.0, 60, NULL);

INSERT INTO medication (name, disabled, band, rating, id_active_ingredient, id_category, dosage, id_user, is_valid, max_taking_time, time_between, quantity_int, quantity_ml)
VALUES ('Metformina 850mg', FALSE, 1, 4.5,
        (SELECT id FROM active_ingredient WHERE description = 'Metformina'),
        (SELECT id FROM category WHERE description = 'Antidiabético'),
        850, NULL, TRUE, 0.5, 12.0, 40, NULL);

INSERT INTO medication (name, disabled, band, rating, id_active_ingredient, id_category, dosage, id_user, is_valid, max_taking_time, time_between, quantity_int, quantity_ml)
VALUES ('Ranitidina 150mg', FALSE, 1, 4.3,
        (SELECT id FROM active_ingredient WHERE description = 'Ranitidina'),
        (SELECT id FROM category WHERE description = 'Anti-inflamatório'),
        150, NULL, TRUE, 1.0, 8.0, 40, NULL);

INSERT INTO medication (name, disabled, band, rating, id_active_ingredient, id_category, dosage, id_user, is_valid, max_taking_time, time_between, quantity_int, quantity_ml)
VALUES ('Omeprazol 20mg', FALSE, 1, 4.4,
        (SELECT id FROM active_ingredient WHERE description = 'Omeprazol'),
        (SELECT id FROM category WHERE description = 'Anti-inflamatório'),
        20, NULL, TRUE, 0.5, 24.0, 60, NULL);

INSERT INTO medication (name, disabled, band, rating, id_active_ingredient, id_category, dosage, id_user, is_valid, max_taking_time, time_between, quantity_int, quantity_ml)
VALUES ('Simvastatina 40mg', FALSE, 1, 4.8,
        (SELECT id FROM active_ingredient WHERE description = 'Simvastatina'),
        (SELECT id FROM category WHERE description = 'Cardioprotetor'),
        40, NULL, TRUE, 0.5, 24.0, 20, NULL);

INSERT INTO medication (name, disabled, band, rating, id_active_ingredient, id_category, dosage, id_user, is_valid, max_taking_time, time_between, quantity_int, quantity_ml)
VALUES ('Aspirina 500mg', FALSE, 1, 4.7,
        (SELECT id FROM active_ingredient WHERE description = 'Aspirina'),
        (SELECT id FROM category WHERE description = 'Cardioprotetor'),
        500, NULL, TRUE, 0.5, 12.0, 24, NULL);

INSERT INTO medication (name, disabled, band, rating, id_active_ingredient, id_category, dosage, id_user, is_valid, max_taking_time, time_between, quantity_int, quantity_ml)
VALUES ('Clopidogrel 75mg', FALSE, 1, 4.7,
        (SELECT id FROM active_ingredient WHERE description = 'Clopidogrel'),
        (SELECT id FROM category WHERE description = 'Anticoagulante'),
        75, NULL, TRUE, 0.5, 12.0, 40, NULL);

INSERT INTO medication (name, disabled, band, rating, id_active_ingredient, id_category, dosage, id_user, is_valid, max_taking_time, time_between, quantity_int, quantity_ml)
VALUES ('Prednisona 20mg', FALSE, 1, 4.5,
        (SELECT id FROM active_ingredient WHERE description = 'Prednisona'),
        (SELECT id FROM category WHERE description = 'Anti-inflamatório'),
        20, NULL, TRUE, 0.5, 24.0, 20, NULL);

INSERT INTO medication (name, disabled, band, rating, id_active_ingredient, id_category, dosage, id_user, is_valid, max_taking_time, time_between, quantity_int, quantity_ml)
VALUES ('Azitromicina 500mg', FALSE, 1, 4.6,
        (SELECT id FROM active_ingredient WHERE description = 'Azitromicina'),
        (SELECT id FROM category WHERE description = 'Antibiótico'),
        500, NULL, TRUE, 1.0, 24.0, 20, NULL);

INSERT INTO medication (name, disabled, band, rating, id_active_ingredient, id_category, dosage, id_user, is_valid, max_taking_time, time_between, quantity_int, quantity_ml)
VALUES ('Enalapril 10mg', FALSE, 1, 4.4,
        (SELECT id FROM active_ingredient WHERE description = 'Enalapril'),
        (SELECT id FROM category WHERE description = 'Hipotensor'),
        10, NULL, TRUE, 0.5, 24.0, 20, NULL);

INSERT INTO medication (name, disabled, band, rating, id_active_ingredient, id_category, dosage, id_user, is_valid, max_taking_time, time_between, quantity_int, quantity_ml)
VALUES ('Salbutamol 100mcg', FALSE, 1, 4.3,
        (SELECT id FROM active_ingredient WHERE description = 'Salbutamol'),
        (SELECT id FROM category WHERE description = 'Broncodilatador'),
        100, NULL, TRUE, 1.0, 24.0, NULL, 50.0);

INSERT INTO medication (name, disabled, band, rating, id_active_ingredient, id_category, dosage, id_user, is_valid, max_taking_time, time_between, quantity_int, quantity_ml)
VALUES ('Prednisona 5mg', FALSE, 1, 4.5,
        (SELECT id FROM active_ingredient WHERE description = 'Prednisona'),
        (SELECT id FROM category WHERE description = 'Anti-inflamatório'),
        5, NULL, TRUE, 0.5, 24.0, 40, NULL);

INSERT INTO medication (name, disabled, band, rating, id_active_ingredient, id_category, dosage, id_user, is_valid, max_taking_time, time_between, quantity_int, quantity_ml)
VALUES ('Hidroxicloroquina 200mg', FALSE, 1, 4.5,
        (SELECT id FROM active_ingredient WHERE description = 'Hidroxicloroquina'),
        (SELECT id FROM category WHERE description = 'Antimalárico'),
        200, NULL, TRUE, 1.0, 24.0, 40, NULL);

INSERT INTO medication (name, disabled, band, rating, id_active_ingredient, id_category, dosage, id_user, is_valid, max_taking_time, time_between, quantity_int, quantity_ml)
VALUES ('Cetirizina 10mg', FALSE, 1, 4.4,
        (SELECT id FROM active_ingredient WHERE description = 'Cetirizina'),
        (SELECT id FROM category WHERE description = 'Antialérgico'),
        10, NULL, TRUE, 1.0, 24.0, 20, NULL);

INSERT INTO medication (name, disabled, band, rating, id_active_ingredient, id_category, dosage, id_user, is_valid, max_taking_time, time_between, quantity_int, quantity_ml)
VALUES ('Fluoxetina 20mg', FALSE, 1, 4.8,
        (SELECT id FROM active_ingredient WHERE description = 'Fluoxetina'),
        (SELECT id FROM category WHERE description = 'Antidepressivo'),
        20, NULL, TRUE, 0.5, 24.0, 30, NULL);

INSERT INTO medication (name, disabled, band, rating, id_active_ingredient, id_category, dosage, id_user, is_valid, max_taking_time, time_between, quantity_int, quantity_ml)
VALUES ('Cetoconazol 200mg', FALSE, 1, 4.6,
        (SELECT id FROM active_ingredient WHERE description = 'Cetoconazol'),
        (SELECT id FROM category WHERE description = 'Antifúngico'),
        200, NULL, TRUE, 0.5, 24.0, 20, NULL);
