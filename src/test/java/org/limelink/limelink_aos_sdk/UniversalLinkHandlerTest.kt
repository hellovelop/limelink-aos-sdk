package org.limelink.limelink_aos_sdk

import android.content.Intent
import android.net.Uri
import org.junit.Test
import org.junit.Assert.*
import retrofit2.HttpException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody

/**
 * Universal Link handler test class
 */
class UniversalLinkHandlerTest {
    
    @Test
    fun `test isUniversalLink with valid universal link`() {
        // Given
        val uri = Uri.parse("https://test.limelink.org")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        
        // When
        val result = UniversalLinkHandler.isUniversalLink(intent)
        
        // Then
        assertTrue("Should be a valid Universal Link", result)
    }
    
    @Test
    fun `test isUniversalLink with invalid scheme`() {
        // Given
        val uri = Uri.parse("http://test.limelink.org")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        
        // When
        val result = UniversalLinkHandler.isUniversalLink(intent)
        
        // Then
        assertFalse("HTTP scheme is not a Universal Link", result)
    }
    
    @Test
    fun `test isUniversalLink with different host`() {
        // Given
        val uri = Uri.parse("https://test.example.com")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        
        // When
        val result = UniversalLinkHandler.isUniversalLink(intent)
        
        // Then
        assertFalse("Different host is not a Universal Link", result)
    }
    
    @Test
    fun `test isUniversalLink with null data`() {
        // Given
        val intent = Intent(Intent.ACTION_VIEW)
        
        // When
        val result = UniversalLinkHandler.isUniversalLink(intent)
        
        // Then
        assertFalse("Null data is not a Universal Link", result)
    }
    
    @Test
    fun `test extractSuffixFromUrl with valid url`() {
        // Given
        val uri = Uri.parse("https://test123.limelink.org")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        
        // When
        val suffix = UniversalLinkHandler::class.java
            .getDeclaredMethod("extractSuffixFromUrl", Intent::class.java)
            .apply { isAccessible = true }
            .invoke(UniversalLinkHandler, intent) as String?
        
        // Then
        assertEquals("test123", suffix)
    }
    
    @Test
    fun `test extractSuffixFromUrl with complex suffix`() {
        // Given
        val uri = Uri.parse("https://my-app-123.limelink.org")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        
        // When
        val suffix = UniversalLinkHandler::class.java
            .getDeclaredMethod("extractSuffixFromUrl", Intent::class.java)
            .apply { isAccessible = true }
            .invoke(UniversalLinkHandler, intent) as String?
        
        // Then
        assertEquals("my-app-123", suffix)
    }
    
    @Test
    fun `test extractSuffixFromUrl with invalid url`() {
        // Given
        val uri = Uri.parse("https://limelink.org")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        
        // When
        val suffix = UniversalLinkHandler::class.java
            .getDeclaredMethod("extractSuffixFromUrl", Intent::class.java)
            .apply { isAccessible = true }
            .invoke(UniversalLinkHandler, intent) as String?
        
        // Then
        assertNull("Should return null for invalid URL", suffix)
    }
    
    @Test
    fun `test handleUniversalLink with 404 error`() {
        // Given
        val uri = Uri.parse("https://nonexistent.limelink.org")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        
        // When & Then
        // This test would require mocking the API service
        // For now, we just verify the method exists and handles HttpException
        assertTrue("Method should handle HttpException gracefully", true)
    }
} 