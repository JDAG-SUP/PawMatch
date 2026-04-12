-- Habilitar extensión PostGIS para cálculos geográficos
CREATE EXTENSION IF NOT EXISTS postgis;

-- 1. Perfiles de Usuario (users_profiles)
CREATE TABLE public.users_profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    display_name TEXT NOT NULL,
    bio TEXT,
    avatar_url TEXT,
    phone TEXT,
    verified BOOLEAN DEFAULT false,
    location GEOGRAPHY(POINT),
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- 2. Mascotas (pets)
CREATE TABLE public.pets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID REFERENCES public.users_profiles(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    species TEXT NOT NULL,
    breed TEXT,
    gender TEXT,
    birth_date DATE,
    size TEXT,
    temperament TEXT[],
    photos TEXT[],
    bio TEXT,
    vaccinated BOOLEAN DEFAULT false,
    neutered BOOLEAN DEFAULT false,
    health_notes TEXT,
    looking_for TEXT[],
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Indice para búsquedas más rápidas en pets activos
CREATE INDEX idx_pets_active ON public.pets(owner_id) WHERE is_active = true;

-- 3. Swipes (swipes)
CREATE TABLE public.swipes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    swiper_pet_id UUID REFERENCES public.pets(id) ON DELETE CASCADE,
    swiped_pet_id UUID REFERENCES public.pets(id) ON DELETE CASCADE,
    direction TEXT NOT NULL CHECK (direction IN ('like', 'pass', 'superlike')),
    created_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE(swiper_pet_id, swiped_pet_id)
);

-- 4. Matches (matches)
CREATE TABLE public.matches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pet_a_id UUID REFERENCES public.pets(id) ON DELETE CASCADE,
    pet_b_id UUID REFERENCES public.pets(id) ON DELETE CASCADE,
    status TEXT DEFAULT 'active' CHECK (status IN ('active', 'archived', 'blocked')),
    created_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE(pet_a_id, pet_b_id)
);

-- 5. Mensajes (messages)
CREATE TABLE public.messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    match_id UUID REFERENCES public.matches(id) ON DELETE CASCADE,
    sender_id UUID REFERENCES public.users_profiles(id) ON DELETE CASCADE,
    content TEXT,
    media_url TEXT,
    type TEXT DEFAULT 'text' CHECK (type IN ('text', 'image', 'location', 'event')),
    read_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 6. Encuentros / Meetups (meetups)
CREATE TABLE public.meetups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    match_id UUID REFERENCES public.matches(id) ON DELETE CASCADE,
    proposed_by UUID REFERENCES public.users_profiles(id) ON DELETE CASCADE,
    title TEXT,
    scheduled_at TIMESTAMPTZ,
    location_name TEXT,
    location_coords GEOGRAPHY(POINT),
    status TEXT DEFAULT 'pending' CHECK (status IN ('pending', 'confirmed', 'cancelled', 'completed')),
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 7. Valoraciones / Reviews (reviews)
CREATE TABLE public.reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    meetup_id UUID REFERENCES public.meetups(id) ON DELETE CASCADE,
    reviewer_id UUID REFERENCES public.users_profiles(id) ON DELETE CASCADE,
    reviewed_pet_id UUID REFERENCES public.pets(id) ON DELETE CASCADE,
    rating SMALLINT CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMPTZ DEFAULT now()
);
