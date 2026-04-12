-- ==============================================
-- Row Level Security (RLS) Policies para PawMatch
-- ==============================================

-- Habilitar RLS en todas las tablas
ALTER TABLE public.users_profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.pets ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.swipes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.matches ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.meetups ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.reviews ENABLE ROW LEVEL SECURITY;

-- ----------------------------------------------------------------------------------
-- 1. Políticas de users_profiles
-- ----------------------------------------------------------------------------------
-- SELECT: público (todos los usuarios autenticados pueden ver perfiles públicos)
CREATE POLICY "Public profiles are viewable by everyone."
ON public.users_profiles FOR SELECT USING (true);

-- UPDATE/DELETE: Solo el propio usuario
CREATE POLICY "Users can insert their own profile."
ON public.users_profiles FOR INSERT WITH CHECK (auth.uid() = id);

CREATE POLICY "Users can update own profile."
ON public.users_profiles FOR UPDATE USING (auth.uid() = id);

CREATE POLICY "Users can delete own profile."
ON public.users_profiles FOR DELETE USING (auth.uid() = id);

-- ----------------------------------------------------------------------------------
-- 2. Políticas de pets
-- ----------------------------------------------------------------------------------
-- SELECT: Si is_active es true, o si auth.uid() es el dueño.
CREATE POLICY "Active pets are viewable by everyone."
ON public.pets FOR SELECT USING (is_active = true OR owner_id = auth.uid());

-- INSERT/UPDATE/DELETE: Solo el dueño de la mascota
CREATE POLICY "Users can insert their own pets."
ON public.pets FOR INSERT WITH CHECK (auth.uid() = owner_id);

CREATE POLICY "Users can update their own pets."
ON public.pets FOR UPDATE USING (auth.uid() = owner_id);

CREATE POLICY "Users can delete their own pets."
ON public.pets FOR DELETE USING (auth.uid() = owner_id);

-- ----------------------------------------------------------------------------------
-- 3. Políticas de swipes
-- ----------------------------------------------------------------------------------
-- SELECT: restringido. El usuario solo puede ver los swipes que ha hecho su mascota
CREATE POLICY "Users can view swipes of their own pets."
ON public.swipes FOR SELECT USING (
    EXISTS (SELECT 1 FROM public.pets WHERE pets.id = swipes.swiper_pet_id AND pets.owner_id = auth.uid())
);

-- INSERT: Solo puede insertar si es dueño de swiper_pet_id
CREATE POLICY "Users can insert swipes for their own pets."
ON public.swipes FOR INSERT WITH CHECK (
    EXISTS (SELECT 1 FROM public.pets WHERE pets.id = swiper_pet_id AND pets.owner_id = auth.uid())
);

-- ----------------------------------------------------------------------------------
-- 4. Políticas de matches
-- ----------------------------------------------------------------------------------
-- SELECT: Solo participantes del match (dueños de pet_a o pet_b)
CREATE POLICY "Users can see matches of their pets."
ON public.matches FOR SELECT USING (
    EXISTS (SELECT 1 FROM public.pets WHERE pets.id IN (pet_a_id, pet_b_id) AND pets.owner_id = auth.uid())
);
-- INSERT/UPDATE directos estarán restringidos por ahora a Funciones o Triggers de seguridad
-- (Edge Functions en Deno con Service Role).

-- ----------------------------------------------------------------------------------
-- 5. Políticas de messages
-- ----------------------------------------------------------------------------------
-- SELECT: Solo participantes del match relacionado
CREATE POLICY "Users can view messages of their matches."
ON public.messages FOR SELECT USING (
    EXISTS (
        SELECT 1 FROM public.matches 
        JOIN public.pets ON public.pets.id IN (public.matches.pet_a_id, public.matches.pet_b_id)
        WHERE public.matches.id = messages.match_id AND public.pets.owner_id = auth.uid()
    )
);

-- INSERT: Solo puede insertar si pertenece al match
CREATE POLICY "Users can insert messages in their matches."
ON public.messages FOR INSERT WITH CHECK (
    sender_id = auth.uid() AND
    EXISTS (
        SELECT 1 FROM public.matches 
        JOIN public.pets ON public.pets.id IN (public.matches.pet_a_id, public.matches.pet_b_id)
        WHERE public.matches.id = messages.match_id AND public.pets.owner_id = auth.uid()
    )
);

-- ----------------------------------------------------------------------------------
-- 6. Políticas de meetups
-- ----------------------------------------------------------------------------------
-- SELECT/UPDATE: Solo dueños de los pets involucrados
CREATE POLICY "Users can view meetups of their matches."
ON public.meetups FOR SELECT USING (
    EXISTS (
        SELECT 1 FROM public.matches 
        JOIN public.pets ON public.pets.id IN (public.matches.pet_a_id, public.matches.pet_b_id)
        WHERE public.matches.id = meetups.match_id AND public.pets.owner_id = auth.uid()
    )
);

CREATE POLICY "Users can insert meetups."
ON public.meetups FOR INSERT WITH CHECK (
    proposed_by = auth.uid()
);

CREATE POLICY "Users can update meetups of their matches."
ON public.meetups FOR UPDATE USING (
    EXISTS (
        SELECT 1 FROM public.matches 
        JOIN public.pets ON public.pets.id IN (public.matches.pet_a_id, public.matches.pet_b_id)
        WHERE public.matches.id = meetups.match_id AND public.pets.owner_id = auth.uid()
    )
);

-- ----------------------------------------------------------------------------------
-- 7. Políticas de reviews
-- ----------------------------------------------------------------------------------
-- SELECT: Público
CREATE POLICY "Reviews are viewable by everyone."
ON public.reviews FOR SELECT USING (true);

-- INSERT: Solo el revisor autenticado
CREATE POLICY "Users can insert review after meetup."
ON public.reviews FOR INSERT WITH CHECK (
    reviewer_id = auth.uid()
);
