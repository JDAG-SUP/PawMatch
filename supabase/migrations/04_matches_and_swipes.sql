-- Tabla de Swipes (Interacciones)
CREATE TABLE public.swipes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    swiper_pet_id UUID REFERENCES public.pets(id) ON DELETE CASCADE,
    target_pet_id UUID REFERENCES public.pets(id) ON DELETE CASCADE,
    liked BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()),
    UNIQUE(swiper_pet_id, target_pet_id)
);

-- Tabla de Matches (Acuerdos mutuos)
CREATE TABLE public.matches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pet1_id UUID REFERENCES public.pets(id) ON DELETE CASCADE,
    pet2_id UUID REFERENCES public.pets(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()),
    UNIQUE(pet1_id, pet2_id)
);

-- Trigger de Búsqueda de Coincidencia (Mutuo Acuerdo)
CREATE OR REPLACE FUNCTION public.check_for_match()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.liked = true THEN
        -- Revisa si la "mascota target" también nos dio Like a "nosotros"
        IF EXISTS (
            SELECT 1 FROM public.swipes 
            WHERE swiper_pet_id = NEW.target_pet_id 
              AND target_pet_id = NEW.swiper_pet_id 
              AND liked = true
        ) THEN
            -- Inserción con IDs ordenados para evitar duplicados inversos
            INSERT INTO public.matches (pet1_id, pet2_id)
            VALUES (
                LEAST(NEW.swiper_pet_id, NEW.target_pet_id), 
                GREATEST(NEW.swiper_pet_id, NEW.target_pet_id)
            )
            ON CONFLICT (pet1_id, pet2_id) DO NOTHING;
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE TRIGGER on_interaction_created
AFTER INSERT ON public.swipes
FOR EACH ROW EXECUTE FUNCTION public.check_for_match();

-- Habilitar RLS
ALTER TABLE public.swipes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.matches ENABLE ROW LEVEL SECURITY;

-- Políticas
CREATE POLICY "Mis mascotas pueden leer sus swipes" ON public.swipes FOR SELECT USING (
    EXISTS (SELECT 1 FROM public.pets WHERE id = swipes.swiper_pet_id AND owner_id = auth.uid())
);
CREATE POLICY "Yo genero swipes por mi mascota" ON public.swipes FOR INSERT WITH CHECK (
    EXISTS (SELECT 1 FROM public.pets WHERE id = swipes.swiper_pet_id AND owner_id = auth.uid())
);

CREATE POLICY "Puedo ver mis matches" ON public.matches FOR SELECT USING (
    EXISTS (SELECT 1 FROM public.pets WHERE id = matches.pet1_id AND owner_id = auth.uid())
    OR EXISTS (SELECT 1 FROM public.pets WHERE id = matches.pet2_id AND owner_id = auth.uid())
);

-- Para Realtime, hay que asegurarnos que la publicación realtime incluya 'matches'
-- Esto usualmente se configura en la consola, pero podemos declararlo:
-- ALTER PUBLICATION supabase_realtime ADD TABLE public.matches;
