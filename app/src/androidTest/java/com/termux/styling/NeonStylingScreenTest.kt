package com.termux.styling

import androidx.compose.ui.test.assertContentDescriptionContains
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.neonbytecode.neon.themeengine.AnsiPalette
import dev.neonbytecode.neon.termux.Selectable
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NeonStylingScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val scheme = SchemeEntry(
        selectable = Selectable("cyberpunk.properties"),
        palette = AnsiPalette.DEFAULT,
    )
    private val defaultFont = FontEntry(Selectable(Selectable.DEFAULT_FILENAME))

    @Test
    fun selectingSchemeReportsPreviewAndEnablesApply() {
        var selected by mutableStateOf<Selectable?>(null)
        var applied = false

        compose.setContent {
            NeonStylingScreen(
                ui = UiState(
                    termuxReady = true,
                    schemes = listOf(scheme),
                    fonts = listOf(defaultFont),
                    selectedScheme = selected,
                ),
                onSelectScheme = { selected = it },
                onSelectFont = {},
                onApplyAll = { applied = true },
            )
        }
        compose.onNodeWithContentDescription("Cyberpunk").performClick()
        compose.onNodeWithContentDescription("Cyberpunk").performClick()
        compose.onNodeWithContentDescription("Cyberpunk, previewing").assertContentDescriptionContains("previewing")
        compose.onNodeWithText("APPLY ALL CHANGES").assertIsEnabled().performClick()

        assert(selected == scheme.selectable)
        assert(applied)
    }

    @Test
    fun searchFiltersSchemeCards() {
        val other = scheme.copy(selectable = Selectable("dracula.properties"))

        compose.setContent {
            NeonStylingScreen(
                ui = UiState(
                    termuxReady = true,
                    schemes = listOf(scheme, other),
                    fonts = listOf(defaultFont),
                ),
                onSelectScheme = {},
                onSelectFont = {},
            )
        }

        compose.onNodeWithText("filter schemes & fonts").performTextInput("drac")
        compose.onNodeWithText("Dracula").assertExists()
        compose.onNodeWithText("Cyberpunk").assertDoesNotExist()
    }

    @Test
    fun applyIsDisabledWithoutPendingChanges() {
        compose.setContent {
            NeonStylingScreen(
                ui = UiState(
                    termuxReady = true,
                    schemes = listOf(scheme),
                    fonts = listOf(defaultFont),
                ),
                onSelectScheme = {},
                onSelectFont = {},
            )
        }

        compose.onNodeWithText("APPLY ALL CHANGES").assertIsNotEnabled()
    }
}
