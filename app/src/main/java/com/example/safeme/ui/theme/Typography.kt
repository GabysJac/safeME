package com.example.safeme.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.safeme.R

val titleTypography = Typography().copy(
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        letterSpacing = 0.25.sp
    )

)
val KaushanScript = FontFamily(
    Font(R.font.kaushan_script)
)
val kaushanTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = KaushanScript,
        fontSize = 20.sp

    )

)