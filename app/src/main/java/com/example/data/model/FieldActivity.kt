package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "field_activities")
data class FieldActivity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val operatorName: String,
    val highway: String,
    val direction: String,  // Norte, Sul, Leste, Oeste
    val laneType: String,   // Principal, Marginal, Dispositivo
    val activityType: String = "Implantação Tacha", // Implantação Tacha, Remoção Tacha, Pintura Mecânica, Pintura Manual, Implantação Defensa, Remoção Defensa
    val studType: String = "",     // Tipo de Tacha (Tacha Mono Branca, etc.)
    val plateType: String = "",    // Tipo de Placa (Regulamentação, Advertência, Indicação)
    val plateCode: String = "",    // Cód. Placa (Ex: R-1, A-1a)
    val plateText: String = "",    // Texto da Placa (quando Tipo for Indicação)
    val lane: String = "",         // Faixa
    val legendDescription: String = "", // Descrição Legenda (quando Faixa for LEGENDA)
    val eixo: String = "",         // Eixo
    val cadence: String = "",      // Cadência
    val observations: String = "", // Observações
    val kmStart: String,
    val kmEnd: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val photoBeforePath: String, // Path to watermarked "Antes" photo
    val photoDuringPath: String = "", // Path to watermarked "Durante" photo (optional/3-photo mode)
    val photoAfterPath: String,  // Path to watermarked "Depois" photo
    val isSent: Boolean = false
)
