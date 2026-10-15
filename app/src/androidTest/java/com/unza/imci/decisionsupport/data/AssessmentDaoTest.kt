package com.unza.imci.decisionsupport.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.unza.imci.decisionsupport.data.database.AppDatabase
import com.unza.imci.decisionsupport.data.database.AssessmentDao
import com.unza.imci.decisionsupport.data.model.AssessmentEntity
import com.unza.imci.decisionsupport.data.model.ClassificationEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * DAO Unit Tests
 * Validates that Room correctly inserts, retrieves, and manages assessments.
 * Aligns with Proposal Phase 3: "facilitate unit testing"
 */
@RunWith(AndroidJUnit4::class)
class AssessmentDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: AssessmentDao

    @Before
    fun createDb() {
        // Create an in-memory database for fast, isolated testing
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries() // For testing only
            .build()
        dao = db.assessmentDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testInsertAndRetrieveAssessment() = runBlocking {
        // 1. Create test data
        val assessment = AssessmentEntity(
            patientId = "TEST_001",
            assessmentJson = "{\"hasCough\":true,\"breathingRate\":45}",
            classification = "Pneumonia",
            treatmentRecommendation = "Give amoxicillin for 5 days"
        )

        // 2. Insert into database
        dao.insertAssessment(assessment)

        // 3. Retrieve and verify
        val allAssessments = dao.getAllAssessments().first()

        assertEquals("Should retrieve exactly 1 assessment", 1, allAssessments.size)
        assertEquals("Patient ID should match", "TEST_001", allAssessments[0].patientId)
        assertEquals("Classification should match", "Pneumonia", allAssessments[0].classification)
    }

    @Test
    fun testDeleteAssessment() = runBlocking {
        // Insert first
        val assessment = AssessmentEntity(
            patientId = "TEST_002",
            assessmentJson = "{}",
            classification = "No illness",
            treatmentRecommendation = "Home care"
        )
        val insertedId = dao.insertAssessment(assessment)
        assertEquals("Initial count should be 1", 1, dao.getAllAssessments().first().size)

        // Delete
        dao.deleteAssessment(insertedId.toInt())
        val afterDelete = dao.getAllAssessments().first()

        assertTrue("Database should be empty after deletion", afterDelete.isEmpty())
    }

    @Test
    fun testInsertAssessmentWithClassifications_returnedMostSevereFirst() = runBlocking {
        val id = dao.insertAssessmentWithClassifications(
            AssessmentEntity(
                patientId = "TEST_003",
                assessmentJson = "{}",
                classification = "COUGH OR COLD + SEVERE DEHYDRATION",
                treatmentRecommendation = "..."
            ),
            listOf(
                ClassificationEntity(assessmentId = 0, module = "RESPIRATORY", title = "COUGH OR COLD", treatment = "Soothe throat", severity = "MILD"),
                ClassificationEntity(assessmentId = 0, module = "DIARRHOEA", title = "SEVERE DEHYDRATION", treatment = "Refer urgently", severity = "SEVERE")
            )
        )

        val stored = dao.getClassificationsForAssessment(id.toInt())
        assertEquals(listOf("SEVERE DEHYDRATION", "COUGH OR COLD"), stored.map { it.title })
    }

    @Test
    fun testDeleteAssessment_alsoDeletesItsClassifications() = runBlocking {
        val id = dao.insertAssessmentWithClassifications(
            AssessmentEntity(patientId = "TEST_004", assessmentJson = "{}", classification = "MALARIA", treatmentRecommendation = "..."),
            listOf(ClassificationEntity(assessmentId = 0, module = "FEVER", title = "MALARIA", treatment = "Antimalarial", severity = "MODERATE"))
        ).toInt()

        dao.deleteAssessment(id)

        assertTrue(dao.getClassificationsForAssessment(id).isEmpty())
    }
}