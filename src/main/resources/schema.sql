CREATE TABLE public.bin (
                            id int8 NOT NULL GENERATED ALWAYS AS IDENTITY( INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1 NO CYCLE),
                            "location" varchar NULL,
                            capacity numeric NULL,
                            emptied_last timestamp NULL,
                            pick_up_time timestamp NULL,
                            fill_threshold numeric NULL,
                            device_id int8 NULL,
                            CONSTRAINT bin_pk PRIMARY KEY (id)
);

CREATE TABLE public.humidity (
                                 value numeric NULL,
                                 date_time timestamp NOT NULL,
                                 bin_id int8 NOT NULL,
                                 CONSTRAINT humidity_pk PRIMARY KEY (bin_id, date_time),
                                 CONSTRAINT humidity_fk FOREIGN KEY (bin_id) REFERENCES public.bin(id) ON UPDATE CASCADE
);

CREATE TABLE public."level" (
                                value numeric NULL,
                                date_time timestamp NOT NULL,
                                bin_id int8 NOT NULL,
                                CONSTRAINT level_pk PRIMARY KEY (bin_id, date_time),
                                CONSTRAINT level_fk FOREIGN KEY (bin_id) REFERENCES public.bin(id) ON UPDATE CASCADE
);

CREATE TABLE public.temperature (
                                    value numeric NULL,
                                    date_time timestamp NOT NULL,
                                    bin_id int8 NOT NULL,
                                    CONSTRAINT temperature_pk PRIMARY KEY (bin_id, date_time),
                                    CONSTRAINT temperature_fk FOREIGN KEY (bin_id) REFERENCES public.bin(id) ON UPDATE CASCADE

);
