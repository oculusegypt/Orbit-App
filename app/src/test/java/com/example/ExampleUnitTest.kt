package com.example

import com.example.data.repository.OrbitRepository
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testOrbitRepositoryDataIntegrity() {
        // Verify all 4 certified courses are loaded with sections and outcomes
        val courses = OrbitRepository.courses.value
        assertEquals(4, courses.size)
        assertTrue(courses.any { it.id == "course-soroban-kids" })
        assertTrue(courses.any { it.id == "course-arabic-foundations" })
        assertTrue(courses.any { it.id == "course-smart-math" })
        assertTrue(courses.any { it.id == "course-tot-trainers" })

        // Verify soroban course has quiz
        val sorobanCourse = courses.first { it.id == "course-soroban-kids" }
        val firstLesson = sorobanCourse.sections.first().lessons.first()
        assertNotNull(firstLesson.quiz)
        assertEquals(4, firstLesson.quiz?.questions?.size)

        // Verify digital library books
        val books = OrbitRepository.books.value
        assertEquals(4, books.size)
        assertTrue(books.all { it.chapters.isNotEmpty() })

        // Verify live sessions
        val liveSessions = OrbitRepository.liveSessions.value
        assertEquals(3, liveSessions.size)
        assertEquals("SARA-SOROBAN-LIVE", liveSessions.first().meetingCode)

        // Verify certificate verification lookup
        val verifiedCert = OrbitRepository.verifyCertificate("CERT-2026-SOROBAN-001")
        assertNotNull(verifiedCert)
        assertEquals("ياسين أحمد", verifiedCert?.userName)
        assertEquals(98, verifiedCert?.gradePercentage)
    }

    @Test
    fun testEnrollmentAndProgressLogic() {
        val enrollment = OrbitRepository.enrollCourse("usr-student-test", "course-smart-math", "free")
        assertNotNull(enrollment)
        assertEquals("course-smart-math", enrollment.courseId)

        OrbitRepository.completeLesson("usr-student-test", "les-math-1", "course-smart-math")
        val progress = OrbitRepository.progressMap.value["les-math-1"]
        assertNotNull(progress)
        assertTrue(progress?.completed == true)
    }

    @Test
    fun testCertificateIssuanceAndAudit() {
        val cert = OrbitRepository.issueCertificate(
            courseId = "course-arabic-foundations",
            courseTitle = "تأسيس اللغة العربية والقرائية",
            userId = "usr-student-test",
            userName = "نور الهدى",
            grade = 100,
            skills = listOf("نور البيان", "التحليل الصوتي")
        )
        assertNotNull(cert)
        assertTrue(cert.verificationCode.startsWith("CERT-2026-"))

        val verified = OrbitRepository.verifyCertificate(cert.verificationCode)
        assertNotNull(verified)
        assertEquals("نور الهدى", verified?.userName)
    }
}
