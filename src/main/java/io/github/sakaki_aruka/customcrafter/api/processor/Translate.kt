package io.github.sakaki_aruka.customcrafter.api.processor

import java.util.Locale

/**
 * @since 5.0.8
 */
object Translate {
    // for recipe name i18n
    data class Word(
        val id: String,
        val locale: Locale,
        val translated: String
    )
}