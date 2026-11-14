package org.umn.projectuts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.umn.projectuts.util.Validators

class ValidatorsTest {

    @Test
    fun validEmail_isAccepted() {
        assertTrue(Validators.isValidEmail("raditya.agra@student.umn.ac.id"))
    }

    @Test
    fun invalidEmail_isRejected() {
        assertFalse(Validators.isValidEmail("raditya@"))
        assertFalse(Validators.isValidEmail("tanpa-at.com"))
    }

    @Test
    fun digitsOnly_rejectsLetters() {
        assertTrue(Validators.isDigitsOnly("00000138599"))
        assertFalse(Validators.isDigitsOnly("0000abc"))
    }

    @Test
    fun filterDigits_removesNonDigitsAndLimitsLength() {
        assertEquals("123", Validators.filterDigits("1a2b3c", 11))
        assertEquals("12345", Validators.filterDigits("1234567", 5))
    }

    @Test
    fun umnAccount_isDetectedByDomain() {
        assertTrue(Validators.isUmnAccount("a@student.umn.ac.id"))
        assertFalse(Validators.isUmnAccount("a@gmail.com"))
    }
}
