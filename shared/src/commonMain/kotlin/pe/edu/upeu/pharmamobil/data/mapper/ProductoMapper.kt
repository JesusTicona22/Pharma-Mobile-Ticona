package pe.edu.upeu.pharmamobil.data.mapper

import pe.edu.upeu.pharmamobil.data.remote.dto.ProductoDto
import pe.edu.upeu.pharmamobil.domain.model.Producto

fun ProductoDto.toDomain(): Producto {
    // 1. Limpiamos el precio: si viene 0 o negativo de internet, le asignamos 1.0
    val precioLimpio = if (price > 0) price else 1.0

    // 2. Limpiamos el nombre: si viene vacío, le ponemos un texto por defecto
    val nombreLimpio = if (title.isNotBlank()) title else "Producto sin nombre"

    return Producto(
        id = id.toLong(),
        nombre = nombreLimpio,
        precio = precioLimpio,
        stock = 15 // Stock por defecto
    )
}