CREATE TABLE public.user_medication_status
(
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    id_user_medication UUID NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE public.user_medication_status
ADD CONSTRAINT fk_user_medication_user_medication_status
FOREIGN KEY (id_user_medication)
REFERENCES public.user_medication (id);