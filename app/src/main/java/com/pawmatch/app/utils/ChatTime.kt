/*
 * Copyright 2026 Vincent Tsen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.pawmatch.app.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Helpers de formato de tiempos para el módulo de chat.
// Se mantienen aquí para evitar duplicar SimpleDateFormat en cada pantalla
// y para poder ajustar el formato globalmente con un solo cambio.

// Formato para la lista de conversaciones:
// si el último mensaje es de hoy se muestra la hora ("HH:mm"),
// si es de otro día se muestra la fecha corta ("dd/MM").
fun formatConversationTimestamp(epochMillis: Long): String {
    if (epochMillis <= 0L) return ""
    val now = Calendar.getInstance()
    val msgTime = Calendar.getInstance().apply { timeInMillis = epochMillis }
    val sameDay = now.get(Calendar.YEAR) == msgTime.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) == msgTime.get(Calendar.DAY_OF_YEAR)
    val pattern = if (sameDay) "HH:mm" else "dd/MM"
    return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(epochMillis))
}

// Formato para las burbujas individuales de mensaje:
// siempre la hora del envío ("HH:mm") en el locale del dispositivo.
fun formatMessageTimestamp(epochMillis: Long): String {
    if (epochMillis <= 0L) return ""
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(epochMillis))
}