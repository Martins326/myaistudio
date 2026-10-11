package com.example

import com.example.ui.components.MarkdownBlock
import com.example.ui.components.parseMarkdownBlocks
import com.example.util.SecurityHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testMarkdownCodeBlockParsing() {
        val markdown = """
            # Analysis
            Here is the code:
            ```kotlin
            val x = 42
            println(x)
            ```
            - bullet 1
            - bullet 2
        """.trimIndent()

        val blocks = parseMarkdownBlocks(markdown)
        assertTrue(blocks.any { it is MarkdownBlock.Heading && it.text == "Analysis" })
        assertTrue(blocks.any { it is MarkdownBlock.CodeBlock && it.language == "kotlin" })
        assertTrue(blocks.any { it is MarkdownBlock.ListItem && it.text == "bullet 1" })
    }

    @Test
    fun testSecurityHelperValidation() {
        assertTrue(SecurityHelper.isValidEmail("admin@nexa.ai"))
        assertTrue(SecurityHelper.isValidEmail("user.test@example.com"))
        assertFalse(SecurityHelper.isValidEmail("invalid-email"))
        assertFalse(SecurityHelper.isValidEmail(""))

        assertTrue(SecurityHelper.isValidPassword("secret123"))
        assertFalse(SecurityHelper.isValidPassword("123"))
    }

    @Test
    fun testPasswordHashingAndVerification() {
        val password = "NexaSecurePassword2026!"
        val salt = SecurityHelper.generateSalt()
        val hash = SecurityHelper.hashPassword(password, salt)

        assertTrue(SecurityHelper.verifyPassword(password, salt, hash))
        assertFalse(SecurityHelper.verifyPassword("wrongPassword", salt, hash))
    }
}
