CREATE SCHEMA IF NOT EXISTS auth;
CREATE SCHEMA IF NOT EXISTS audit;
CREATE SCHEMA IF NOT EXISTS storage;
CREATE SCHEMA IF NOT EXISTS public;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE auth.users 
( 
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),  
    display_name VARCHAR(30) NOT NULL,  
    email VARCHAR(50) NOT NULL,  
    locale VARCHAR(2) NOT NULL DEFAULT 'pt',  
    phone_number VARCHAR(15),  
    password_hash VARCHAR(60) NOT NULL,  
    role VARCHAR(20) NOT NULL DEFAULT 'user',
    disabled BOOLEAN NOT NULL DEFAULT FALSE,  
    birth_date DATE,  
    last_seen TIMESTAMP,  
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  
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
    accepted BOOLEAN NOT NULL DEFAULT FALSE,
    datetime TIMESTAMP DEFAULT NOW()
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
    quantity_card INT,  
    id_user UUID,  
    is_valid BOOLEAN NOT NULL DEFAULT FALSE,  
    max_time FLOAT,
    time_between FLOAT NOT NULL
); 

CREATE TABLE public.user_medication 
( 
    id_user UUID,  
    id_medication UUID,  
    disabled BOOLEAN NOT NULL DEFAULT FALSE,  
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),  
    quantity_int INT,  
    quantity_ml FLOAT,  
    max_time FLOAT,
    time_between FLOAT NOT NULL,  
    first_dosage_time TIMESTAMP NOT NULL,  
    max_validation_time FLOAT
); 

CREATE TABLE public.user_medication_stock 
( 
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),  
    quantity_stocked INT,  
    id_user_medication UUID,  
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  
    stocked_at TIMESTAMP NOT NULL,  
    quantity_card INT,  
    expiration_date DATE NOT NULL,  
    quantity_now INT
); 

CREATE TABLE audit.logged_actions 
( 
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),  
    action CHAR(1) NOT NULL,  
    schema_name VARCHAR(50) NOT NULL,
    table_name VARCHAR(50) NOT NULL,
    action_tmstamp TIMESTAMP NOT NULL,  
    "user" JSONB,
    row_data JSONB NOT NULL,  
    changed_fields JSONB
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

CREATE TABLE public.log_usage 
( 
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),  
    id_user UUID,  
    id_user_medication_stock UUID,  
    id_video_info UUID NOT NULL,  
    is_approved BOOLEAN DEFAULT FALSE,  
    id_replacement UUID,  
    is_valid BOOLEAN NOT NULL DEFAULT TRUE,  
    action_tmstamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
); 

CREATE TABLE public.labels 
( 
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),  
    description VARCHAR(100) NOT NULL,  
    UNIQUE (description)
); 

CREATE TABLE public.video_info 
( 
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),  
    id_file UUID,  
    observation TEXT,  
    UNIQUE (id)
); 

CREATE TABLE public.video_info_labels 
( 
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),  
    id_video_info UUID,  
    id_label UUID
); 

CREATE TABLE storage.files 
( 
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),  
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,  
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

-- Add foreign key constraints to public.log_usage
ALTER TABLE public.log_usage 
ADD CONSTRAINT fk_user_log_usage 
FOREIGN KEY (id_user) 
REFERENCES auth.users (id);

ALTER TABLE public.log_usage 
ADD CONSTRAINT fk_user_medication_stock_log_usage 
FOREIGN KEY (id_user_medication_stock) 
REFERENCES public.user_medication_stock (id);

ALTER TABLE public.log_usage 
ADD CONSTRAINT fk_video_info 
FOREIGN KEY (id_video_info) 
REFERENCES public.video_info (id);

ALTER TABLE public.log_usage 
ADD CONSTRAINT fk_replacement_log_usage 
FOREIGN KEY (id_replacement) 
REFERENCES public.log_usage (id);

-- Add foreign key constraint to public.video_info
ALTER TABLE public.video_info 
ADD CONSTRAINT fk_file_user 
FOREIGN KEY (id_file) 
REFERENCES storage.files (id);

-- Add foreign key constraints to public.video_info_labels
ALTER TABLE public.video_info_labels 
ADD CONSTRAINT fk_video_info_video_info_labels 
FOREIGN KEY (id_video_info) 
REFERENCES public.video_info (id);

ALTER TABLE public.video_info_labels 
ADD CONSTRAINT fk_label_video_info_labels 
FOREIGN KEY (id_label) 
REFERENCES public.labels (id);

-- Add foreign key constraint to storage.files
ALTER TABLE storage.files 
ADD CONSTRAINT fk_bucket 
FOREIGN KEY (id_bucket) 
REFERENCES storage.buckets (id);