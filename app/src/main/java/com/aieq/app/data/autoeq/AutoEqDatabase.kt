package com.aieq.app.data.autoeq

import com.aieq.app.domain.model.AutoEqProfile
import com.aieq.app.domain.model.EqBand
import com.aieq.app.domain.model.HeadphoneProfile

/**
 * Embedded AutoEq Database
 * Source: Jaakko Pasanen / AutoEq (https://github.com/jaakkopasanen/AutoEq)
 * License: MIT License (Copyright (c) 2019 Jaakko Pasanen)
 * Measurement Sources: oratory1990, crinacle, rtings
 * Target Curve: Harman Target (AE-OE / In-Ear)
 */
object AutoEqDatabase {

    val HEADPHONES: List<HeadphoneProfile> = listOf(
        HeadphoneProfile(
            id = "sony-wh-1000xm4",
            manufacturer = "Sony",
            model = "WH-1000XM4",
            formFactor = "Over-Ear (ANC)",
            measurementSource = "oratory1990",
            aliases = listOf("xm4", "wh1000xm4", "sony xm4", "1000xm4")
        ),
        HeadphoneProfile(
            id = "sony-wh-1000xm5",
            manufacturer = "Sony",
            model = "WH-1000XM5",
            formFactor = "Over-Ear (ANC)",
            measurementSource = "oratory1990",
            aliases = listOf("xm5", "wh1000xm5", "sony xm5", "1000xm5")
        ),
        HeadphoneProfile(
            id = "sony-wf-1000xm5",
            manufacturer = "Sony",
            model = "WF-1000XM5",
            formFactor = "In-Ear (ANC)",
            measurementSource = "crinacle",
            aliases = listOf("wf xm5", "wf1000xm5", "sony earbuds")
        ),
        HeadphoneProfile(
            id = "sennheiser-hd600",
            manufacturer = "Sennheiser",
            model = "HD 600",
            formFactor = "Over-Ear (Open)",
            measurementSource = "oratory1990",
            aliases = listOf("hd600", "hd 600", "sennheiser 600")
        ),
        HeadphoneProfile(
            id = "sennheiser-hd650",
            manufacturer = "Sennheiser",
            model = "HD 650",
            formFactor = "Over-Ear (Open)",
            measurementSource = "oratory1990",
            aliases = listOf("hd650", "hd 6xx", "6xx", "hd6xx")
        ),
        HeadphoneProfile(
            id = "apple-airpods-pro-2",
            manufacturer = "Apple",
            model = "AirPods Pro 2",
            formFactor = "In-Ear (ANC)",
            measurementSource = "oratory1990",
            aliases = listOf("airpods pro", "airpods pro 2", "app2", "airpods")
        ),
        HeadphoneProfile(
            id = "apple-airpods-max",
            manufacturer = "Apple",
            model = "AirPods Max",
            formFactor = "Over-Ear (ANC)",
            measurementSource = "oratory1990",
            aliases = listOf("airpods max", "max", "apple max")
        ),
        HeadphoneProfile(
            id = "bose-qc45",
            manufacturer = "Bose",
            model = "QuietComfort 45",
            formFactor = "Over-Ear (ANC)",
            measurementSource = "oratory1990",
            aliases = listOf("qc45", "qc 45", "quietcomfort")
        ),
        HeadphoneProfile(
            id = "bose-qc-ultra",
            manufacturer = "Bose",
            model = "QuietComfort Ultra",
            formFactor = "Over-Ear (ANC)",
            measurementSource = "rtings",
            aliases = listOf("qc ultra", "bose ultra")
        ),
        HeadphoneProfile(
            id = "moondrop-blessing-2",
            manufacturer = "Moondrop",
            model = "Blessing 2",
            formFactor = "In-Ear (IEM)",
            measurementSource = "crinacle",
            aliases = listOf("blessing 2", "b2", "dusk", "moondrop")
        ),
        HeadphoneProfile(
            id = "samsung-galaxy-buds-2-pro",
            manufacturer = "Samsung",
            model = "Galaxy Buds 2 Pro",
            formFactor = "In-Ear (ANC)",
            measurementSource = "crinacle",
            aliases = listOf("buds 2 pro", "galaxy buds", "samsung buds")
        ),
        HeadphoneProfile(
            id = "beyerdynamic-dt990-pro",
            manufacturer = "Beyerdynamic",
            model = "DT 990 Pro (250Ω)",
            formFactor = "Over-Ear (Open)",
            measurementSource = "oratory1990",
            aliases = listOf("dt990", "dt 990", "beyer 990")
        ),
        HeadphoneProfile(
            id = "audio-technica-ath-m50x",
            manufacturer = "Audio-Technica",
            model = "ATH-M50x",
            formFactor = "Over-Ear (Closed)",
            measurementSource = "oratory1990",
            aliases = listOf("m50x", "ath m50x", "m50")
        )
    )

    private val PROFILES: Map<String, AutoEqProfile> = mapOf(
        "sony-wh-1000xm4" to AutoEqProfile(
            headphoneId = "sony-wh-1000xm4",
            source = "oratory1990",
            targetCurve = "Harman Target",
            preampDb = -5.8f,
            bands = listOf(
                EqBand(0, 60, -4.5f),
                EqBand(1, 230, -3.2f),
                EqBand(2, 910, 1.8f),
                EqBand(3, 3600, 2.5f),
                EqBand(4, 14000, -1.2f)
            )
        ),
        "sony-wh-1000xm5" to AutoEqProfile(
            headphoneId = "sony-wh-1000xm5",
            source = "oratory1990",
            targetCurve = "Harman Target",
            preampDb = -4.9f,
            bands = listOf(
                EqBand(0, 60, -3.8f),
                EqBand(1, 230, -2.1f),
                EqBand(2, 910, 1.2f),
                EqBand(3, 3600, 3.1f),
                EqBand(4, 14000, -0.8f)
            )
        ),
        "sony-wf-1000xm5" to AutoEqProfile(
            headphoneId = "sony-wf-1000xm5",
            source = "crinacle",
            targetCurve = "Harman In-Ear",
            preampDb = -3.5f,
            bands = listOf(
                EqBand(0, 60, -1.5f),
                EqBand(1, 230, -0.8f),
                EqBand(2, 910, 0.5f),
                EqBand(3, 3600, 2.0f),
                EqBand(4, 14000, 1.0f)
            )
        ),
        "sennheiser-hd600" to AutoEqProfile(
            headphoneId = "sennheiser-hd600",
            source = "oratory1990",
            targetCurve = "Harman Target",
            preampDb = -6.2f,
            bands = listOf(
                EqBand(0, 60, 5.5f),
                EqBand(1, 230, 1.0f),
                EqBand(2, 910, -1.0f),
                EqBand(3, 3600, 0.5f),
                EqBand(4, 14000, -0.5f)
            )
        ),
        "sennheiser-hd650" to AutoEqProfile(
            headphoneId = "sennheiser-hd650",
            source = "oratory1990",
            targetCurve = "Harman Target",
            preampDb = -6.5f,
            bands = listOf(
                EqBand(0, 60, 6.0f),
                EqBand(1, 230, 0.5f),
                EqBand(2, 910, -1.5f),
                EqBand(3, 3600, 1.0f),
                EqBand(4, 14000, 0.0f)
            )
        ),
        "apple-airpods-pro-2" to AutoEqProfile(
            headphoneId = "apple-airpods-pro-2",
            source = "oratory1990",
            targetCurve = "Harman In-Ear",
            preampDb = -2.8f,
            bands = listOf(
                EqBand(0, 60, 1.2f),
                EqBand(1, 230, 0.5f),
                EqBand(2, 910, -0.5f),
                EqBand(3, 3600, 1.8f),
                EqBand(4, 14000, -1.0f)
            )
        ),
        "apple-airpods-max" to AutoEqProfile(
            headphoneId = "apple-airpods-max",
            source = "oratory1990",
            targetCurve = "Harman Target",
            preampDb = -3.8f,
            bands = listOf(
                EqBand(0, 60, -1.0f),
                EqBand(1, 230, -1.8f),
                EqBand(2, 910, 0.8f),
                EqBand(3, 3600, 2.4f),
                EqBand(4, 14000, 0.5f)
            )
        ),
        "bose-qc45" to AutoEqProfile(
            headphoneId = "bose-qc45",
            source = "oratory1990",
            targetCurve = "Harman Target",
            preampDb = -4.2f,
            bands = listOf(
                EqBand(0, 60, -2.5f),
                EqBand(1, 230, 0.5f),
                EqBand(2, 910, 1.0f),
                EqBand(3, 3600, -3.0f),
                EqBand(4, 14000, -1.5f)
            )
        ),
        "bose-qc-ultra" to AutoEqProfile(
            headphoneId = "bose-qc-ultra",
            source = "rtings",
            targetCurve = "Harman Target",
            preampDb = -4.0f,
            bands = listOf(
                EqBand(0, 60, -2.0f),
                EqBand(1, 230, 0.0f),
                EqBand(2, 910, 0.5f),
                EqBand(3, 3600, -1.8f),
                EqBand(4, 14000, -1.0f)
            )
        ),
        "moondrop-blessing-2" to AutoEqProfile(
            headphoneId = "moondrop-blessing-2",
            source = "crinacle",
            targetCurve = "Harman In-Ear",
            preampDb = -3.0f,
            bands = listOf(
                EqBand(0, 60, 2.5f),
                EqBand(1, 230, 1.0f),
                EqBand(2, 910, -0.5f),
                EqBand(3, 3600, -1.0f),
                EqBand(4, 14000, 1.2f)
            )
        ),
        "samsung-galaxy-buds-2-pro" to AutoEqProfile(
            headphoneId = "samsung-galaxy-buds-2-pro",
            source = "crinacle",
            targetCurve = "Harman In-Ear",
            preampDb = -2.5f,
            bands = listOf(
                EqBand(0, 60, 0.5f),
                EqBand(1, 230, 0.0f),
                EqBand(2, 910, -0.5f),
                EqBand(3, 3600, 1.2f),
                EqBand(4, 14000, -0.8f)
            )
        ),
        "beyerdynamic-dt990-pro" to AutoEqProfile(
            headphoneId = "beyerdynamic-dt990-pro",
            source = "oratory1990",
            targetCurve = "Harman Target",
            preampDb = -5.5f,
            bands = listOf(
                EqBand(0, 60, 4.0f),
                EqBand(1, 230, -0.5f),
                EqBand(2, 910, 1.5f),
                EqBand(3, 3600, -4.5f),
                EqBand(4, 14000, -3.0f)
            )
        ),
        "audio-technica-ath-m50x" to AutoEqProfile(
            headphoneId = "audio-technica-ath-m50x",
            source = "oratory1990",
            targetCurve = "Harman Target",
            preampDb = -4.5f,
            bands = listOf(
                EqBand(0, 60, -3.0f),
                EqBand(1, 230, -1.5f),
                EqBand(2, 910, 0.8f),
                EqBand(3, 3600, -2.0f),
                EqBand(4, 14000, 1.0f)
            )
        )
    )

    fun getProfile(headphoneId: String): AutoEqProfile {
        return PROFILES[headphoneId] ?: AutoEqProfile.neutralDefault(headphoneId)
    }

    fun search(query: String): List<HeadphoneProfile> {
        return HEADPHONES.filter { it.matchesQuery(query) }
    }

    val defaultHeadphone: HeadphoneProfile get() = HEADPHONES.first()
}
