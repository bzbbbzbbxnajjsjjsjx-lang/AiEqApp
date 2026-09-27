# AUTOEQ INTEGRATION & ACOUSTIC MEASUREMENT PROVENANCE

## 1. Project Background & Open-Source License

- **Source Project:** [AutoEq by Jaakko Pasanen](https://github.com/jaakkopasanen/AutoEq)
- **License:** MIT License (Copyright (c) 2019 Jaakko Pasanen).
- **Core Concept:** Headphone frequency response curves deviate substantially from the ideal human perceived neutrality (the Harman Target curve, Diffuse Field target, or Optimum HiFi target). AutoEq uses optimization algorithms to calculate inverse equalization filters that compensate for acoustic deficiencies in specific headphone models.

---

## 2. Headphone Profile Schema & Embedded Dataset

Each curated headphone entry contains:
- `id`: Unique identifier (e.g. `sony-wh-1000xm4`).
- `manufacturer`: Brand name (e.g. `Sony`, `Sennheiser`, `Apple`, `Bose`).
- `model`: Exact model name.
- `formFactor`: Over-Ear, In-Ear (IEM), Earbuds.
- `measurementSource`: Trusted measurement laboratory (e.g. `oratory1990`, `crinacle`, `rtings`).
- `preampDb`: Recommended negative digital gain offset (e.g. `-4.8 dB`) to prevent digital clipping when boosting frequencies.
- `bands`: List of 5 standardized frequency bands mapped to Android's native hardware equalizer centers (`60 Hz`, `230 Hz`, `910 Hz`, `3600 Hz`, `14000 Hz`) plus 10-band extensions.

---

## 3. Supported Canonical Models in Embedded Database

1. **Sony WH-1000XM4** (Over-Ear, ANC) — Harman Target via oratory1990.
2. **Sony WH-1000XM5** (Over-Ear, ANC) — Harman Target via oratory1990.
3. **Sony WF-1000XM5** (In-Ear, ANC) — Harman Target via crinacle.
4. **Sennheiser HD 600** (Over-Ear, Open-Back) — Harman Target via oratory1990.
5. **Sennheiser HD 650 / HD 6XX** (Over-Ear, Open-Back) — Harman Target via oratory1990.
6. **Apple AirPods Pro 2** (In-Ear, ANC) — Harman Target via oratory1990.
7. **Apple AirPods Max** (Over-Ear, ANC) — Harman Target via oratory1990.
8. **Bose QuietComfort 45 / SE** (Over-Ear, ANC) — Harman Target via oratory1990.
9. **Bose QuietComfort Ultra** (Over-Ear, ANC) — Harman Target via rtings.
10. **Moondrop Blessing 2** (In-Ear, Hybrid) — Harman Target via crinacle.
11. **Samsung Galaxy Buds 2 Pro** (In-Ear, ANC) — Harman Target via crinacle.
12. **Beyerdynamic DT 990 Pro (250 ohm)** (Over-Ear, Open-Back) — Harman Target via oratory1990.
13. **Audio-Technica ATH-M50x** (Over-Ear, Closed-Back) — Harman Target via oratory1990.
