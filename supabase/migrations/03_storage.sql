-- Script para crear buckets de Supabase y sus políticas
-- Ejecuta este script desde el SQL Editor en Supabase.

-- Habilitar extensión en el caso de no existir, pero storage viene habilitado en Supabase por defecto

-- Insertar el Bucket de pet-photos
INSERT INTO storage.buckets (id, name, public) 
VALUES ('pet-photos', 'pet-photos', true)
ON CONFLICT (id) DO NOTHING;

-- Configuracion de políticas RLS para Storage

-- 1. Cualquiera puede ver las fotos (Bucket público)
CREATE POLICY "Fotos públicas" 
ON storage.objects FOR SELECT 
USING ( bucket_id = 'pet-photos' );

-- 2. Usuarios autenticados pueden subir fotos al bucket
-- Opcionalmente restringimos a que el archivo comience con el auth.uid() de carpeta para mayor control
CREATE POLICY "Usuarios suben fotos" 
ON storage.objects FOR INSERT 
WITH CHECK (
    bucket_id = 'pet-photos' AND 
    auth.role() = 'authenticated'
);

-- 3. Los dueños pueden borrar sus fotos
CREATE POLICY "Dueños borran fotos" 
ON storage.objects FOR DELETE 
USING (
    bucket_id = 'pet-photos' AND 
    auth.uid() = owner
);

-- 4. Los dueños pueden actualizar sus fotos
CREATE POLICY "Dueños actualizan fotos" 
ON storage.objects FOR UPDATE 
USING (
    bucket_id = 'pet-photos' AND 
    auth.uid() = owner
);
