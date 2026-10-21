package com.unza.imci.decisionsupport.validation

import com.unza.imci.decisionsupport.data.model.AppetiteTestResult
import com.unza.imci.decisionsupport.data.model.MalariaTestResult
import com.unza.imci.decisionsupport.data.model.PatientAssessment
import com.unza.imci.decisionsupport.domain.decision_engine.ImciDecisionEngine
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Runs the hand-written case cards in cases/case_cards.csv through the Decision Engine and
 * reports agreement with the WHO IMCI Chart Booklet (March 2014).
 *
 * The cards are the gold standard: each one records a child's age, the signs, and the
 * classification(s) the booklet gives. They were written from the chart rows, not from the
 * engine, so a disagreement means either the engine or the card is wrong. Every disagreement is
 * written to cases/results/ and carried into Table 6.4 of the report with its cause and action.
 *
 * Writes cases/results/case_card_results.csv (one row per card) and
 * cases/results/case_card_summary.md (the counts quoted in Section 6.6).
 */
class CaseCardValidationTest {

    /**
     * Rows that record the absence of a problem rather than an action. The booklet only reaches
     * the ear row when the mother answers yes to "does the child have an ear problem", while the
     * app assesses every module for every child, so these rows are ignored on both sides.
     */
    private val noFindingRows = setOf("NO EAR INFECTION", "NO ACUTE MALNUTRITION")

    private data class Card(
        val id: String,
        val module: String,
        val ageMonths: Int,
        val signs: String,
        val expected: Set<String>,
        val bookletRow: String,
        val notes: String
    )

    private data class Outcome(val card: Card, val actual: Set<String>) {
        val missing get() = card.expected - actual
        val extra get() = actual - card.expected
        val matches get() = missing.isEmpty() && extra.isEmpty()
    }

    @Test
    fun caseCards_agreeWithTheBooklet() {
        val casesDir = findCasesDir()
        val cards = readCards(File(casesDir, "case_cards.csv"))
        assertTrue("Expected at least 50 case cards, found ${cards.size}", cards.size >= 50)

        val engine = ImciDecisionEngine()
        val outcomes = cards.map { card ->
            val titles = engine.assess(patientOf(card)).titles.toSet() - noFindingRows
            Outcome(card, titles)
        }

        val resultsDir = File(casesDir, "results").apply { mkdirs() }
        writeResults(File(resultsDir, "case_card_results.csv"), outcomes)
        writeSummary(File(resultsDir, "case_card_summary.md"), outcomes)
        println(File(resultsDir, "case_card_summary.md").readText())

        // Objective O2: at least 95% agreement with the WHO tables. The one card that is left
        // disagreeing by design (F06, no malaria test done) is named in Table 6.4.
        val matched = outcomes.count { it.matches }
        val agreement = 100.0 * matched / outcomes.size
        assertTrue(
            "Agreement with the case cards fell to ${String.format("%.1f", agreement)}% " +
                "($matched of ${outcomes.size}); see cases/results/case_card_summary.md",
            agreement >= 95.0
        )
    }

    // ---------------------------------------------------------------------
    // Cards -> PatientAssessment
    // ---------------------------------------------------------------------

    /** Builds the assessment a CHW would have entered for this card's signs. */
    private fun patientOf(card: Card): PatientAssessment {
        val tokens = card.signs.split(';').map { it.trim() }.filter { it.isNotEmpty() && it != "-" }
        val flags = tokens.filterNot { it.contains('=') }.toSet()
        val values = tokens.filter { it.contains('=') }
            .associate { it.substringBefore('=').trim() to it.substringAfter('=').trim() }

        val unknown = (flags - KNOWN_FLAGS) + (values.keys - KNOWN_VALUES)
        require(unknown.isEmpty()) { "Card ${card.id} uses unknown sign(s): $unknown" }

        val diarrhoeaDays = values["diarrhoea_days"]?.toInt()
        val earDischargeDays = values["ear_discharge_days"]?.toInt()

        return PatientAssessment(
            patientId = card.id,
            ageInMonths = card.ageMonths,

            cannotDrinkOrBreastfeed = "cannot_drink" in flags,
            vomitsEverything = "vomits_everything" in flags,
            convulsions = "convulsions" in flags,
            lethargicOrUnconscious = "lethargic" in flags,

            hasCough = "cough" in flags,
            hasDifficultBreathing = "difficult_breathing" in flags,
            breathingRatePerMin = values["rr"]?.toInt(),
            chestIndrawing = "chest_indrawing" in flags,
            stridorInCalmChild = "stridor" in flags,

            hasDiarrhoea = "diarrhoea" in flags || diarrhoeaDays != null,
            diarrhoeaDurationDays = diarrhoeaDays,
            bloodInStool = "blood_in_stool" in flags,
            restlessOrIrritable = "restless" in flags,
            sunkenEyes = "sunken_eyes" in flags,
            drinkingPoorly = "drinking_poorly" in flags,
            drinksEagerlyThirsty = "drinks_eagerly" in flags,
            skinPinchGoesBackSlowly = "pinch_slow" in flags,
            skinPinchGoesBackVerySlowly = "pinch_very_slow" in flags,

            hasFever = "fever" in flags,
            feverDurationDays = values["fever_days"]?.toInt(),
            malariaRiskArea = "no_malaria_risk" !in flags,
            malariaTestResult = when (values["rdt"]) {
                "pos" -> MalariaTestResult.POSITIVE
                "neg" -> MalariaTestResult.NEGATIVE
                else -> MalariaTestResult.NOT_DONE
            },
            stiffNeck = "stiff_neck" in flags,
            runnyNose = "runny_nose" in flags,
            measles = "measles" in flags,
            cloudingOfCornea = "cornea_clouding" in flags,
            deepOrExtensiveMouthUlcers = "deep_mouth_ulcers" in flags,
            mouthUlcers = "mouth_ulcers" in flags,
            pusDrainingFromEye = "pus_from_eye" in flags,

            hasEarPain = "ear_pain" in flags,
            earDischarge = "ear_discharge" in flags || earDischargeDays != null,
            earDischargeDurationDays = earDischargeDays,
            tenderSwellingBehindEar = "swelling_behind_ear" in flags,

            bilateralPittingOedema = "oedema" in flags,
            muacCm = values["muac"]?.toFloat(),
            appetiteTest = when (values["appetite"]) {
                "pass" -> AppetiteTestResult.PASSED
                "fail" -> AppetiteTestResult.FAILED
                else -> AppetiteTestResult.NOT_DONE
            },
            somePalmarPallor = "pallor_some" in flags,
            severePalmarPallor = "pallor_severe" in flags
        )
    }

    // ---------------------------------------------------------------------
    // CSV in, report out
    // ---------------------------------------------------------------------

    private fun readCards(file: File): List<Card> {
        require(file.exists()) { "Case cards not found at ${file.absolutePath}" }
        return file.readLines()
            .drop(1)
            .filter { it.isNotBlank() && !it.startsWith("#") }
            .map { line ->
                val c = line.split(',')
                require(c.size >= 6) { "Malformed card row: $line" }
                Card(
                    id = c[0].trim(),
                    module = c[1].trim(),
                    ageMonths = c[2].trim().toInt(),
                    signs = c[3].trim(),
                    expected = c[4].split('|').map { it.trim() }
                        .filter { it.isNotEmpty() && it != "-" }
                        .toSet() - noFindingRows,
                    bookletRow = c[5].trim(),
                    notes = c.getOrElse(6) { "" }.trim()
                )
            }
    }

    private fun writeResults(file: File, outcomes: List<Outcome>) {
        val rows = outcomes.joinToString("\n") { o ->
            listOf(
                o.card.id,
                o.card.module,
                o.card.ageMonths.toString(),
                o.card.signs,
                o.card.expected.sorted().joinToString("|").ifEmpty { "-" },
                o.actual.sorted().joinToString("|").ifEmpty { "-" },
                if (o.matches) "MATCH" else "MISMATCH",
                o.missing.sorted().joinToString("|").ifEmpty { "-" },
                o.extra.sorted().joinToString("|").ifEmpty { "-" }
            ).joinToString(",")
        }
        file.writeText("case_id,module,age_months,signs,card_says,engine_says,result,missing,extra\n$rows\n")
    }

    private fun writeSummary(file: File, outcomes: List<Outcome>) {
        val matched = outcomes.count { it.matches }
        val total = outcomes.size
        val percent = String.format("%.1f", 100.0 * matched / total)

        val byModule = outcomes.groupBy { it.card.module }.toSortedMap().map { (module, cases) ->
            "| $module | ${cases.size} | ${cases.count { it.matches }} | " +
                "${String.format("%.1f", 100.0 * cases.count { it.matches } / cases.size)}% |"
        }

        val mismatches = outcomes.filterNot { it.matches }.map { o ->
            "| ${o.card.id} | ${o.card.expected.sorted().joinToString("; ").ifEmpty { "(none)" }} | " +
                "${o.actual.sorted().joinToString("; ").ifEmpty { "(none)" }} | ${o.card.bookletRow} |"
        }

        file.writeText(
            """
            # Case-card comparison with the WHO IMCI Chart Booklet (March 2014)

            Cards: $total. Agreement: $matched of $total ($percent%). Target: 95%.
            A card agrees when the engine returns exactly the classification(s) the card gives,
            ignoring the no-finding rows ${noFindingRows.sorted().joinToString(" and ")}.

            ## By module

            | Module | Cards | Agreed | Agreement |
            | --- | --- | --- | --- |
            ${byModule.joinToString("\n            ")}

            ## Disagreements

            | Case | Card says | Engine says | Booklet row |
            | --- | --- | --- | --- |
            ${mismatches.joinToString("\n            ").ifEmpty { "| (none) | | | |" }}
            """.trimIndent() + "\n"
        )
    }

    /** Walks up from the test working directory to the repository root that holds cases/. */
    private fun findCasesDir(): File {
        var dir: File? = File(System.getProperty("user.dir")).absoluteFile
        while (dir != null) {
            val cases = File(dir, "cases")
            if (File(cases, "case_cards.csv").exists()) return cases
            dir = dir.parentFile
        }
        throw IllegalStateException("cases/case_cards.csv not found above ${System.getProperty("user.dir")}")
    }

    private companion object {
        val KNOWN_FLAGS = setOf(
            "cannot_drink", "vomits_everything", "convulsions", "lethargic",
            "cough", "difficult_breathing", "chest_indrawing", "stridor",
            "diarrhoea", "blood_in_stool", "restless", "sunken_eyes", "drinking_poorly",
            "drinks_eagerly", "pinch_slow", "pinch_very_slow",
            "fever", "no_malaria_risk", "stiff_neck", "runny_nose", "measles",
            "cornea_clouding", "deep_mouth_ulcers", "mouth_ulcers", "pus_from_eye",
            "ear_pain", "ear_discharge", "swelling_behind_ear",
            "oedema", "pallor_some", "pallor_severe"
        )
        val KNOWN_VALUES = setOf(
            "rr", "diarrhoea_days", "fever_days", "rdt", "ear_discharge_days", "muac", "appetite"
        )
    }
}
