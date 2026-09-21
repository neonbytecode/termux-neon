package com.termux.styling

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class PresetsStoreTest {

    @Test
    fun `StylingPreset serialization and deserialization`() {
        val original = StylingPreset(
            id = "preset-1",
            name = "Cyber Night",
            schemeName = "cyberpunk.properties",
            fontName = "FiraCode-Regular.ttf",
            textColorOverride = 0xFF8BE9FD.toInt(),
        )

        val json = original.toJson()
        val parsed = StylingPreset.fromJson(json)

        assertNotNull(parsed)
        assertEquals(original.id, parsed?.id)
        assertEquals(original.name, parsed?.name)
        assertEquals(original.schemeName, parsed?.schemeName)
        assertEquals(original.fontName, parsed?.fontName)
        assertEquals(original.textColorOverride, parsed?.textColorOverride)
    }

    @Test
    fun `StylingPreset handles null scheme and font`() {
        val original = StylingPreset(
            id = "preset-2",
            name = "Default Setup",
            schemeName = null,
            fontName = null,
            textColorOverride = null,
        )

        val json = original.toJson()
        val parsed = StylingPreset.fromJson(json)

        assertNotNull(parsed)
        assertEquals("preset-2", parsed?.id)
        assertEquals("Default Setup", parsed?.name)
        assertNull(parsed?.schemeName)
        assertNull(parsed?.fontName)
        assertNull(parsed?.textColorOverride)
    }

    @Test
    fun `StylingPreset list serialization and deserialization`() {
        val presets = listOf(
            StylingPreset(id = "1", name = "One", schemeName = "s1.properties", fontName = "f1.ttf"),
            StylingPreset(id = "2", name = "Two", schemeName = null, fontName = "f2.ttf", textColorOverride = 12345),
        )

        val arrayJson = StylingPreset.listToJsonArray(presets)
        val parsedList = StylingPreset.listFromJsonArray(arrayJson)

        assertEquals(2, parsedList.size)
        assertEquals("1", parsedList[0].id)
        assertEquals("One", parsedList[0].name)
        assertEquals("s1.properties", parsedList[0].schemeName)
        assertEquals("2", parsedList[1].id)
        assertEquals("Two", parsedList[1].name)
        assertNull(parsedList[1].schemeName)
        assertEquals(12345, parsedList[1].textColorOverride)
    }
}
