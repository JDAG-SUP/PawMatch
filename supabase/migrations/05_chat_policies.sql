-- Habilitar RLS en Mensajes
ALTER TABLE public.messages ENABLE ROW LEVEL SECURITY;

-- Select (Lectura)
CREATE POLICY "Puedo ver mensajes de mis matches" ON public.messages FOR SELECT USING (
    EXISTS (
        SELECT 1 FROM public.matches 
        JOIN public.pets ON (pets.id = matches.pet1_id OR pets.id = matches.pet2_id)
        WHERE matches.id = messages.match_id AND pets.owner_id = auth.uid()
    )
);

-- Insert (Escritura)
CREATE POLICY "Puedo insertar mensajes si pertenezco al match y soy el sender" ON public.messages FOR INSERT WITH CHECK (
    EXISTS (
        SELECT 1 FROM public.matches 
        JOIN public.pets ON (pets.id = matches.pet1_id OR pets.id = matches.pet2_id)
        WHERE matches.id = messages.match_id AND pets.owner_id = auth.uid()
    )
    AND sender_id = auth.uid()
);

-- Update/Delete (Deshabilitados por seguridad en este MVP, dejándolos implícitamente inalcanzables)
