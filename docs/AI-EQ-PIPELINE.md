# AI EQUALIZATION PIPELINE & SAFETY CLAMPING

## 1. Multi-Stage Pipeline Architecture

```
Track Metadata + Headphone Profile + AutoEq Profile + User Preferences
                                ↓
                     AI Reasoning Engine
      (Google Gemini 1.5 Flash / Local Heuristic Fallback)
                                ↓
                      Structured JSON Output
             {"reasoning": "...", "adjustments": [...]}
                                ↓
                        Schema Validation
                                ↓
                     Relative Gain Synthesis
                 Final_Gain[i] = AutoEq[i] + AI_Delta[i]
                                ↓
                     Hard Safety Clamping
               Clamp: -12.0 dB <= Final_Gain[i] <= +12.0 dB
                                ↓
                   Preamp Anti-Clipping Offset
             Preamp = min(AutoEq_Preamp, -max(0, max_positive_gain))
                                ↓
                    Android Audio Hardware DSP
```

---

## 2. Safety Invariants & Clamping Discipline

1. **Max AI Delta Limit:**
   - The AI is never permitted to adjust any individual band by more than $\pm 6.0\text{ dB}$ relative to the AutoEq baseline.
2. **Absolute Gain Ceiling & Floor:**
   - Total combined gain per band is strictly clamped to $[-12.0\text{ dB}, +12.0\text{ dB}]$.
3. **Preamp Compensation (Zero Clipping Guarantee):**
   - In digital audio processing, boosting a frequency band above $0\text{ dBFS}$ results in severe non-linear harmonic distortion (clipping).
   - If any frequency band has a positive gain of $+G\text{ dB}$, the digital pre-amplifier gain must be attenuated by at least $-G\text{ dB}$.
   - The final preamp value is calculated as:
     $$\text{Preamp} = \min(\text{AutoEq\_Preamp}, -\max(0, \max_i(\text{Final\_Gain}_i)))$$

---

## 3. Cache & Deduplication Strategy

To prevent battery drain and unnecessary API calls, the pipeline calculates a deterministic hash:
$$\text{CacheKey} = \text{MD5}(\text{Artist} + \text{Title} + \text{HeadphoneId} + \text{Preference} + \text{Intensity})$$
If an identical query was evaluated previously, the cached `FinalEqProfile` is re-applied instantly with $0\text{ ms}$ latency and $0$ token cost.
