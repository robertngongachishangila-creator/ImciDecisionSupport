# IMCI Decision Support: Technical Documentation

## 1. Architecture

The app follows the three-layer architecture described in the proposal, implemented with
MVVM and Jetpack Compose.

| Layer | Package | Contents |
|---|---|---|
| Presentation | `ui.screens`, `ui.components`, `AssessmentViewModel`, `MainActivity` | One Compose screen per IMCI step, the results, history, detail, user guide and PIN screens. The ViewModel holds the form state. `MainActivity` handles navigation. |
| Domain (decision engine) | `domain.decision_engine` | `ImciDecisionEngine` runs one `ImciClassifier` per illness category and returns an `AssessmentResult`. |
| Data | `data.database`, `data.repository`, `data.model`, `data.security` | Room (SQLite) database, `AssessmentRepository`, entities, and `PinManager`. |

**Data flow for one assessment:** the CHW enters signs on each step screen (stored in the
ViewModel) → on the last step the ViewModel builds a `PatientAssessment` and calls
`ImciDecisionEngine.assess()` → the `ClassificationScreen` shows every classification →
**Save** writes the assessment and its classifications to Room in one transaction. There are
no network calls anywhere in this flow.

## 2. Decision engine

`ImciDecisionEngine` runs every classifier for every child, because IMCI is an integrated
assessment. It sorts the combined result by severity (pink, then yellow, then green). The sort
is stable, so results of the same severity stay in IMCI assessment order. If nothing applies,
the result is `NO SPECIFIC ILLNESS` (green).

| Classifier | Assessed when | Classifications (severity) |
|---|---|---|
| `DangerSignsClassifier` | Always | SEVERE GENERAL DISEASE (pink) if any general danger sign |
| `RespiratoryClassifier` | Cough or difficult breathing | SEVERE PNEUMONIA (pink): danger sign, chest indrawing or stridor · PNEUMONIA (yellow): fast breathing (≥60 under 2 months, ≥50 at 2–11 months, ≥40 at 12–59 months) · COUGH OR COLD (green) |
| `DiarrhoeaClassifier` | Diarrhoea | Dehydration by the "two signs" rule: SEVERE DEHYDRATION (pink) · SOME DEHYDRATION (yellow) · NO DEHYDRATION (green). Also, 14 days or more: SEVERE PERSISTENT DIARRHOEA (pink, if dehydrated) or PERSISTENT DIARRHOEA (yellow). Also, blood in stool: DYSENTERY (yellow) |
| `FeverClassifier` | Fever | VERY SEVERE FEBRILE DISEASE (pink): danger sign or stiff neck · MALARIA (yellow): RDT positive, or malaria risk area with no test, or no risk area and no other cause · FEVER: NO MALARIA (green): risk area, RDT negative · FEVER: MALARIA UNLIKELY (green): no risk area, runny nose or measles. Fever for more than 7 days adds "refer for assessment". Measles: SEVERE COMPLICATED MEASLES (pink) · MEASLES WITH EYE OR MOUTH COMPLICATIONS (yellow) · MEASLES (green) |
| `EarClassifier` | Ear pain, discharge or swelling | MASTOIDITIS (pink) · CHRONIC EAR INFECTION (yellow): discharge 14 days or more · ACUTE EAR INFECTION (yellow) |
| `NutritionClassifier` | Always | SEVERE ACUTE MALNUTRITION (pink): oedema of both feet, visible severe wasting or MUAC < 11.5 cm · MODERATE ACUTE MALNUTRITION (yellow): MUAC 11.5 to < 12.5 cm. MUAC is used only at age 6 months or older. SEVERE ANAEMIA (pink): severe palmar pallor · ANAEMIA (yellow): some palmar pallor |

**Verification status:** every rule must be cross-checked by the developer against the WHO
IMCI Chart Booklet edition used in Zambia before the evaluation, as the proposal's AI
disclosure requires. See the development log below.

## 3. Database schema (Room, version 2)

**`assessments`**: one row per saved assessment.

| Column | Type | Notes |
|---|---|---|
| `id` | INTEGER PK | Auto-generated |
| `patientId` | TEXT | Anonymised ID; `ANON_xxxxxxxx` is generated if blank. Indexed |
| `assessmentJson` | TEXT | All recorded signs and measurements (`PatientAssessment` fields) |
| `classification` | TEXT | Summary of all titles joined with " + ", for list display |
| `treatmentRecommendation` | TEXT | Summary of all treatments |
| `timestamp` | INTEGER | Epoch milliseconds. Indexed |
| `isSynced` | INTEGER | Always false (offline only) |

**`classifications`**: one row per classification (several per assessment).

| Column | Type | Notes |
|---|---|---|
| `id` | INTEGER PK | Auto-generated |
| `assessmentId` | INTEGER FK → `assessments.id` | ON DELETE CASCADE. Indexed |
| `module` | TEXT | `ImciModule` name, e.g. `RESPIRATORY` (`LEGACY` for migrated rows) |
| `title` | TEXT | e.g. `PNEUMONIA` |
| `treatment` | TEXT | Treatment recommendation |
| `severity` | TEXT | `SEVERE` / `MODERATE` / `MILD` |

**Migration 1 → 2** (`AppDatabase.MIGRATION_1_2`) adds the indexes and the
`classifications` table. It copies each existing assessment's single classification into
the table as a `LEGACY` row, so no saved record is lost.

**Deviation from the proposal:** Phase 3 listed separate tables for patients and symptoms.
The app keeps symptoms as JSON on the assessment row, and patients are identified only by an
anonymised `patientId`. This keeps the schema simple for a single-user, offline app.
Classifications and treatments, which are queried and displayed, have their own table.

## 4. Security and privacy

- **PIN lock** (`PinManager`): a 4-digit PIN is required at launch. Only a salted SHA-256
  hash is stored. *Forgot PIN* deletes all assessments before a new PIN can be set.
- **No cloud copies:** `android:allowBackup="false"`, and `data_extraction_rules.xml` /
  `backup_rules.xml` exclude every data domain from cloud backup and device-to-device transfer.
- **Disclaimer:** shown on the home, results and user guide screens.

## 5. Testing

| Suite | Location | Runs on | Covers |
|---|---|---|---|
| `ImciDecisionEngineTest` | `app/src/test` | JVM (`./gradlew testDebugUnitTest`) | 8+ cases for each of the 6 modules at severity boundaries, plus integrated multi-illness cases |
| `AssessmentDaoTest` | `app/src/androidTest` | Emulator/device (`./gradlew connectedAndroidTest`) | Insert, retrieve, delete, classifications ordering, deleting children with their parent |

**Still to do before evaluation:** classification accuracy against the Chart Booklet tables
(target ≥ 95%), completion time (≤ 5 min) and response time (< 500 ms) measurements, and the
usability session (task completion ≥ 80%, SUS ≥ 68).

## 6. Development log

AI-assisted work is recorded here and in the matching commit messages, as the proposal's
AI-Assisted Development Disclosure requires.

| Date | Change | AI assistance |
|---|---|---|
| 2026-09-15 | Fever, ear and malnutrition modules; PIN lock; navigation fixes; test suite expanded to 56 cases | Not recorded in the commit message; developer to complete |
| 2026-09-30 | Integrated (multi-classification) decision engine split into one classifier per module; diarrhoea rules (two-sign dehydration, persistent diarrhoea, dysentery); malaria risk area and RDT; separate anaemia classification; MUAC age limit; `classifications` table, indexes and migration; colour-coded results; disclaimer; user guide; backup disabled; tests rewritten; this documentation | Claude (Claude Code) drafted the code, tests and documentation. **Clinical rules pending developer verification against the WHO IMCI Chart Booklet.** |
