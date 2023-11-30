INSERT INTO public.bin (location, capacity, emptied_last, pick_up_time, fill_threshold, device_id)
VALUES
    ('Location 1', 100.0, '2023-11-10 08:00:00', '2023-11-11 08:00:00', 75.0, '001'),
    ('Location 2', 150.0, '2023-11-10 09:00:00', '2023-11-11 09:00:00', 80.0, '002'),
    ('Location 3', 120.0, '2023-11-10 10:00:00', '2023-11-11 10:00:00', 70.0, '003'),
    ('Location 4', 200.0, '2023-11-10 11:00:00', '2023-11-11 11:00:00', 65.0, '004'),
    ('Location 5', 180.0, '2023-11-10 12:00:00', '2023-11-11 12:00:00', 90.0, '005');

INSERT INTO public.humidity (value, date_time, bin_id)
VALUES
    (45.0, '2023-11-10 08:30:00', 1),
    (50.0, '2023-11-10 09:30:00', 1),
    (55.0, '2023-11-10 10:30:00', 2),
    (60.0, '2023-11-10 11:30:00', 2),
    (65.0, '2023-11-10 12:30:00', 3),
    (70.0, '2023-11-10 13:30:00', 3),
    (75.0, '2023-11-10 14:30:00', 4),
    (80.0, '2023-11-10 15:30:00', 4),
    (85.0, '2023-11-10 16:30:00', 5),
    (90.0, '2023-11-10 17:30:00', 5);

INSERT INTO public."level" (value, date_time, bin_id)
VALUES
    (10.0, '2023-11-11 08:30:00', 1),
    (12.4, '2023-11-11 09:30:00', 1),
    (17.2, '2023-11-11 10:30:00', 2),
    (17.0, '2023-11-11 11:30:00', 2),
    (19.7, '2023-11-11 12:30:00', 3),
    (20.0, '2023-11-11 13:30:00', 3),
    (21.5, '2023-11-11 14:30:00', 4),
    (25.1, '2023-11-11 15:30:00', 4),
    (29.8, '2023-11-11 16:30:00', 5),
    (33.33, '2023-11-11 17:30:00', 5);

INSERT INTO public.temperature (value, date_time, bin_id)
VALUES
    (10.5, '2023-11-12 08:30:00', 1),
    (11.5, '2023-11-12 09:30:00', 1),
    (12.5, '2023-11-12 10:30:00', 2),
    (12.5, '2023-11-12 11:30:00', 2),
    (13.5, '2023-11-12 12:30:00', 3),
    (15.1, '2023-11-12 13:30:00', 3),
    (15.2, '2023-11-12 14:30:00', 4),
    (15.5, '2023-11-12 15:30:00', 4),
    (14.5, '2023-11-12 16:30:00', 5),
    (11.5, '2023-11-12 17:30:00', 5);