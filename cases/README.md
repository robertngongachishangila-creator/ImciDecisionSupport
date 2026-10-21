# Case cards: comparing the engine with the WHO IMCI Chart Booklet (March 2014)

`case_cards.csv` holds 67 case cards. Each card records a child's age, the signs a health worker
would have found, and the classification(s) the booklet's ASSESS & CLASSIFY chart for the sick
child aged 2 months up to 5 years gives for those signs. The cards are the gold standard: the
engine is judged against them, not the other way round.

## How to run the comparison

```
./gradlew :app:testDebugUnitTest --tests "com.unza.imci.decisionsupport.validation.CaseCardValidationTest"
```

The test reads `case_cards.csv`, builds a `PatientAssessment` for each card, runs
`ImciDecisionEngine.assess()`, and writes:

- `results/case_card_results.csv` — one row per card: what the card says, what the engine says,
  MATCH or MISMATCH, and which classifications were missing or extra.
- `results/case_card_summary.md` — the totals and the disagreement list quoted in Chapter 6.

`results/case_card_results_run1.csv` and `results/case_card_summary_run1.md` are kept from the
first run, before the engine was corrected, so the two runs can be compared.

The test fails if agreement falls below the 95% target in objective O2, so a later change to the
rules cannot quietly break booklet alignment.

## How a card is scored

A card agrees when the engine returns **exactly** the set of classifications on the card. Two
rows that record the absence of a problem — `NO EAR INFECTION` and `NO ACUTE MALNUTRITION` — are
ignored on both sides, because the booklet only reaches the ear row when the mother answers yes
to "does the child have an ear problem", while the app assesses every module for every child.
`NO DEHYDRATION` is **not** ignored: it carries treatment Plan A, so it is a real classification.

## Sign vocabulary

Signs are written as `;`-separated tokens. Flags are present or absent; the rest take a value.

| Token | Meaning |
| --- | --- |
| `cannot_drink`, `vomits_everything`, `convulsions`, `lethargic` | general danger signs |
| `cough`, `difficult_breathing`, `rr=N`, `chest_indrawing`, `stridor` | cough or difficult breathing; `rr` is breaths per minute |
| `diarrhoea`, `diarrhoea_days=N`, `blood_in_stool` | diarrhoea, its duration, dysentery |
| `restless`, `sunken_eyes`, `drinking_poorly`, `drinks_eagerly`, `pinch_slow`, `pinch_very_slow` | dehydration signs |
| `fever`, `fever_days=N`, `rdt=pos\|neg\|none`, `no_malaria_risk`, `stiff_neck`, `runny_nose` | fever; a card is in a malaria risk area unless `no_malaria_risk` is given |
| `measles`, `cornea_clouding`, `deep_mouth_ulcers`, `mouth_ulcers`, `pus_from_eye` | measles now or in the last 3 months, and its complications |
| `ear_pain`, `ear_discharge`, `ear_discharge_days=N`, `swelling_behind_ear` | ear problem |
| `oedema`, `muac=N` (cm), `appetite=pass\|fail\|none`, `pallor_some`, `pallor_severe` | acute malnutrition and anaemia |

`-` in the signs column means a child with no signs; `-` in the expected column means the
booklet gives no classification.

## Provenance (read before quoting the figure)

The cards were written against the chart rows of the March 2014 booklet before any engine output
was looked at, and the engine was not consulted while writing them. Two points must be stated
wherever the agreement figure is used:

1. The cards were drafted with AI assistance (Claude), from the booklet's published chart rows.
   They are **not** an independent clinical review by a second assessor. A clinician who has not
   seen the engine should re-check the cards before the figure is presented as external
   validation; the comparison then stands as written, with the cards signed off.
2. The pages of the booklet PDF are images, so the chart rows could not be machine-extracted.
   The `booklet_row` column names the chart row each card comes from rather than a page number.

## Known deviation from the booklet

Card `F06` (fever, malaria risk area, no malaria test done) is the one card the engine still
disagrees with. The 2014 chart expects a malaria test in every fever case in a high-risk setting
and gives no presumptive row when no test was done; the engine classifies MALARIA and tells the
health worker to do an RDT if one is available. This was kept deliberately for rapid diagnostic
test stock-outs and is recorded in Table 6.4 and in the limitations.
