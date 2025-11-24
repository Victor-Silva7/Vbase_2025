package com.ifpr.androidapptemplate.utils

/**
 * Configurações centralizadas de cache e constantes do app
 * Elimina valores hardcoded espalhados pelo código
 */
object AppConfig {
    
    // ===== PAGINAÇÃO =====
    const val DEFAULT_PAGE_SIZE = 10
    const val COMMENTS_PAGE_SIZE = 20
    const val INITIAL_LOAD_SIZE = 20
    const val PREFETCH_DISTANCE = 5
    
    // ===== CACHE =====
    const val MAX_CACHE_SIZE = 50
    const val CACHE_EXPIRATION_MS = 5 * 60 * 1000L // 5 minutos
    
    // ===== DELAYS (MOCK - Remover em produção) =====
    const val NETWORK_DELAY_SHORT = 500L
    const val NETWORK_DELAY_MEDIUM = 800L
    const val NETWORK_DELAY_LONG = 1000L
    
    // ===== DEBOUNCE =====
    const val SEARCH_DEBOUNCE_DELAY = 300L
    const val INPUT_DEBOUNCE_DELAY = 500L
    
    // ===== IMAGENS =====
    const val MAX_IMAGE_WIDTH = 1024
    const val MAX_IMAGE_HEIGHT = 1024
    const val IMAGE_QUALITY = 70
    const val MAX_IMAGES_PER_REGISTRATION = 1
    
    // ===== FIREBASE =====
    object Firebase {
        const val PERSISTENCE_CACHE_SIZE = 100 * 1024 * 1024L // 100MB
        const val UPLOAD_TIMEOUT_MS = 30000L
        const val DOWNLOAD_TIMEOUT_MS = 20000L
    }
    
    // ===== RELEVÂNCIA =====
    object Relevance {
        const val LIKE_WEIGHT = 3
        const val COMMENT_WEIGHT = 5
        const val SHARE_WEIGHT = 7
        const val FRESHNESS_BONUS = 1.2f
        const val VERIFICATION_BONUS = 2.0f
    }
    
    // ===== TEMPO =====
    object Time {
        const val SEVEN_DAYS_MS = 7 * 24 * 60 * 60 * 1000L
        const val THIRTY_DAYS_MS = 30 * 24 * 60 * 60 * 1000L
        const val ONE_WEEK_MS = 7 * 24 * 60 * 60 * 1000L
    }
}
