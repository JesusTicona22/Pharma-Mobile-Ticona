package pe.edu.upeu.pharmamobil.data.mapper

import pe.edu.upeu.pharmamobil.data.remote.dto.ProductoDto
import pe.edu.upeu.pharmamobil.domain.model.Producto

fun ProductoDto.toDomain(): Producto {
    // 1. Limpiamos el precio usando la nueva variable 'precio'
    val precioLimpio = if (precio > 0) precio else 1.0

    // 2. Limpiamos el nombre usando la nueva variable 'nombre'
    val nombreLimpio = if (nombre.isNotBlank()) nombre else "Producto sin nombre"

    return Producto(
        id = id, // Ya es Long desde el backend, no necesita toLong()
        nombre = nombreLimpio,
        precio = precioLimpio,
        stock = stock // Ahora usamos el stock real de tu base de datos Oracle
    )
}