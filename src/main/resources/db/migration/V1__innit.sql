CREATE SCHEMA IF NOT EXISTS auth;
CREATE SCHEMA IF NOT EXISTS storage;
CREATE SCHEMA IF NOT EXISTS public;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE auth.users 
( 
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),  
    display_name VARCHAR(30) NOT NULL,  
    email VARCHAR(50) NOT NULL,
    notification_token VARCHAR(50),
    locale VARCHAR(2) NOT NULL DEFAULT 'pt',  
    phone_number VARCHAR(15),  
    password_hash VARCHAR(60) NOT NULL,  
    role VARCHAR(20) NOT NULL DEFAULT 'user',
    disabled BOOLEAN NOT NULL DEFAULT FALSE,  
    birth_date DATE,  
    last_seen TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    metadata JSONB DEFAULT '{}',
    avatar_url VARCHAR(255),
    UNIQUE (email)
);

CREATE TABLE auth.roles
(
    role VARCHAR(20) PRIMARY KEY
);

CREATE TABLE public.user_responsibles
(
    id_user UUID NOT NULL,
    id_responsible UUID NOT NULL,
    accepted BOOLEAN DEFAULT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE public.medication
(
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    disabled BOOLEAN NOT NULL DEFAULT FALSE,
    band INT NOT NULL,
    rating FLOAT,
    id_active_ingredient UUID NOT NULL,
    id_category UUID NOT NULL,
    dosage FLOAT NOT NULL,
    id_user UUID,
    is_valid BOOLEAN NOT NULL DEFAULT FALSE,
    max_taking_time FLOAT,
    quantity_int INT,
    quantity_ml FLOAT,
    time_between FLOAT NOT NULL
);

CREATE TABLE public.user_medication
(
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    id_user UUID,
    id_medication UUID,
    disabled BOOLEAN NOT NULL DEFAULT FALSE,
    quantity_int INT,
    quantity_ml FLOAT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    time_between FLOAT NOT NULL,
    first_dosage_time TIMESTAMPTZ NOT NULL,
    max_taking_time FLOAT
);

CREATE TABLE public.user_medication_stock
(
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    quantity_stocked INT,
    id_user_medication UUID,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    stocked_at TIMESTAMPTZ NOT NULL,
    expiration_date DATE NOT NULL
);

CREATE TABLE public.user_medication_stock_usage
(
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    id_usage UUID,
    id_user_medication_stock UUID,
    quantity_int INT,
    quantity_ml FLOAT
);

CREATE TABLE public.active_ingredient
(
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    description VARCHAR(100) NOT NULL,
    UNIQUE (description)
);

CREATE TABLE public.category
(
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    description VARCHAR(100) NOT NULL,
    UNIQUE (description)
);

CREATE TABLE public.usage
(
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    id_user UUID NOT NULL,
    id_file UUID NOT NULL,
    id_responsible UUID DEFAULT NULL,
    is_approved BOOLEAN DEFAULT NULL,
    obs VARCHAR(100) DEFAULT NULL,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    action_tmstamp TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE public.labels
(
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    description VARCHAR(100) NOT NULL,
    UNIQUE (description)
);

CREATE TABLE public.usage_labels
(
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    id_usage UUID,
    id_label UUID
);

CREATE TABLE public.notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    id_user UUID NOT NULL,
    seen BOOLEAN DEFAULT FALSE,
    seen_at TIMESTAMPTZ,
    message TEXT NOT NULL,
    detailed_message JSONB,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE storage.files
(
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    name VARCHAR(255) NOT NULL,  
    type VARCHAR(50),  
    id_bucket UUID
); 

CREATE TABLE storage.buckets 
( 
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),  
    description VARCHAR(255),  
    UNIQUE (description)
); 

-- Add foreign key constraint to auth.users
ALTER TABLE auth.users 
ADD CONSTRAINT fk_role 
FOREIGN KEY (role) 
REFERENCES auth.roles (role);

-- Add foreign key constraints to public.user_responsibles
ALTER TABLE public.user_responsibles 
ADD CONSTRAINT fk_user_user_responsible 
FOREIGN KEY (id_user) 
REFERENCES auth.users (id);

ALTER TABLE public.user_responsibles 
ADD CONSTRAINT fk_responsible_user_responsible 
FOREIGN KEY (id_responsible) 
REFERENCES auth.users (id);

-- Add foreign key constraints to public.medication
ALTER TABLE public.medication 
ADD CONSTRAINT fk_id_active_ingredient
FOREIGN KEY (id_active_ingredient)
REFERENCES public.active_ingredient (id);

ALTER TABLE public.medication 
ADD CONSTRAINT fk_id_category
FOREIGN KEY (id_category)
REFERENCES public.category (id);

ALTER TABLE public.medication 
ADD CONSTRAINT fk_user_medication 
FOREIGN KEY (id_user) 
REFERENCES auth.users (id);

-- Add foreign key constraints to public.user_medication
ALTER TABLE public.user_medication 
ADD CONSTRAINT fk_user_medication_user 
FOREIGN KEY (id_user) 
REFERENCES auth.users (id);

ALTER TABLE public.user_medication 
ADD CONSTRAINT fk_medication 
FOREIGN KEY (id_medication) 
REFERENCES public.medication (id);

-- Add foreign key constraint to public.user_medication_stock
ALTER TABLE public.user_medication_stock 
ADD CONSTRAINT fk_user_medication_stock 
FOREIGN KEY (id_user_medication) 
REFERENCES public.user_medication (id);

-- Add foreign key constraint to public.notifications
ALTER TABLE public.notifications
ADD CONSTRAINT fk_notifications_user
FOREIGN KEY (id_user) REFERENCES auth.users (id);

CREATE INDEX idx_notifications_user_seen ON notifications (id_user, seen);

-- Add foreign key constraints to public.usage
ALTER TABLE public.usage
ADD CONSTRAINT fk_user_usage
FOREIGN KEY (id_user) 
REFERENCES auth.users (id);

ALTER TABLE public.usage
ADD CONSTRAINT fk_responsible_usage
FOREIGN KEY (id_responsible)
REFERENCES auth.users (id);

ALTER TABLE public.usage
ADD CONSTRAINT fk_file_usage
FOREIGN KEY (id_file)
REFERENCES storage.files (id);

ALTER TABLE public.usage_labels
ADD CONSTRAINT fk_label_usage_labels
FOREIGN KEY (id_label) 
REFERENCES public.labels (id);

ALTER TABLE public.usage_labels
ADD CONSTRAINT fk_usage_usage_labels
FOREIGN KEY (id_usage)
REFERENCES public.usage (id);

-- Add foreign key constraint to storage.files
ALTER TABLE storage.files 
ADD CONSTRAINT fk_bucket 
FOREIGN KEY (id_bucket) 
REFERENCES storage.buckets (id);

-- Add foreign key constraint to public.user_medication_stock_usage
ALTER TABLE public.user_medication_stock_usage
ADD CONSTRAINT fk_usage
FOREIGN KEY (id_usage)
REFERENCES public.usage (id);

ALTER TABLE public.user_medication_stock_usage
ADD CONSTRAINT fk_user_medication_stock
FOREIGN KEY (id_user_medication_stock)
REFERENCES public.user_medication_stock (id);

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER set_updated_at
BEFORE UPDATE ON public.usage
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();
