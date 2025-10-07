package com.ifpr.androidapptemplate

import android.text.SpannableStringBuilder
import com.ifpr.androidapptemplate.ui.registro.CapitalizeTextWatcher
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for CapitalizeTextWatcher functionality
 */
class CapitalizeTextWatcherTest {

    @Test
    fun testCapitalizeFirstWord() {
        val textWatcher = CapitalizeTextWatcher()
        val editable = SpannableStringBuilder("rosa")
        
        textWatcher.afterTextChanged(editable)
        
        assertEquals("Rosa", editable.toString())
    }

    @Test
    fun testCapitalizeMultipleWords() {
        val textWatcher = CapitalizeTextWatcher()
        val editable = SpannableStringBuilder("rosa do jardim")
        
        textWatcher.afterTextChanged(editable)
        
        assertEquals("Rosa Do Jardim", editable.toString())
    }

    @Test
    fun testCapitalizeWithMixedCase() {
        val textWatcher = CapitalizeTextWatcher()
        val editable = SpannableStringBuilder("ROSA do JARDIM")
        
        textWatcher.afterTextChanged(editable)
        
        assertEquals("Rosa Do Jardim", editable.toString())
    }

    @Test
    fun testCapitalizeEmptyString() {
        val textWatcher = CapitalizeTextWatcher()
        val editable = SpannableStringBuilder("")
        
        textWatcher.afterTextChanged(editable)
        
        assertEquals("", editable.toString())
    }

    @Test
    fun testCapitalizeSingleCharacter() {
        val textWatcher = CapitalizeTextWatcher()
        val editable = SpannableStringBuilder("r")
        
        textWatcher.afterTextChanged(editable)
        
        assertEquals("R", editable.toString())
    }

    @Test
    fun testCapitalizeWithSpaces() {
        val textWatcher = CapitalizeTextWatcher()
        val editable = SpannableStringBuilder("  rosa   do   jardim  ")
        
        textWatcher.afterTextChanged(editable)
        
        assertEquals("  Rosa   Do   Jardim  ", editable.toString())
    }

    @Test
    fun testCapitalizeLocationNames() {
        val textWatcher = CapitalizeTextWatcher()
        val editable = SpannableStringBuilder("são paulo")
        
        textWatcher.afterTextChanged(editable)
        
        assertEquals("São Paulo", editable.toString())
    }

    @Test
    fun testCapitalizeWithAccents() {
        val textWatcher = CapitalizeTextWatcher()
        val editable = SpannableStringBuilder("açaí do pará")
        
        textWatcher.afterTextChanged(editable)
        
        assertEquals("Açaí Do Pará", editable.toString())
    }

    @Test
    fun testAlreadyCapitalizedText() {
        val textWatcher = CapitalizeTextWatcher()
        val editable = SpannableStringBuilder("Rosa Do Jardim")
        
        textWatcher.afterTextChanged(editable)
        
        assertEquals("Rosa Do Jardim", editable.toString())
    }

    @Test
    fun testCapitalizeWithNumbers() {
        val textWatcher = CapitalizeTextWatcher()
        val editable = SpannableStringBuilder("rosa 123 jardim")
        
        textWatcher.afterTextChanged(editable)
        
        assertEquals("Rosa 123 Jardim", editable.toString())
    }
}