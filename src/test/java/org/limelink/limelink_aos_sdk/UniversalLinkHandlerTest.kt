package org.limelink.limelink_aos_sdk

import android.content.Intent
import android.net.Uri
import org.junit.Test
import org.junit.Assert.*

/**
 * Universal Link handler test class
 */
class UniversalLinkHandlerTest {
    
    @Test
    fun `test isUniversalLink with valid subdomain pattern`() {
        // Given
        val uri = Uri.parse("https://test.limelink.org/link/abc123")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        
        // When
        val result = UniversalLinkHandler.isUniversalLink(intent)
        
        // Then
        assertTrue("Should be a valid Universal Link with subdomain pattern", result)
    }
    
    @Test
    fun `test isUniversalLink with valid legacy deeplink`() {
        // Given
        val uri = Uri.parse("https://deep.limelink.org/link/subdomain=test&path=abc123&platform=android")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        
        // When
        val result = UniversalLinkHandler.isUniversalLink(intent)
        
        // Then
        assertTrue("Should be a valid Universal Link with legacy deeplink", result)
    }
    
    @Test
    fun `test isUniversalLink with invalid scheme`() {
        // Given
        val uri = Uri.parse("http://test.limelink.org/link/abc123")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        
        // When
        val result = UniversalLinkHandler.isUniversalLink(intent)
        
        // Then
        assertFalse("HTTP scheme is not a Universal Link", result)
    }
    
    @Test
    fun `test isUniversalLink with different host`() {
        // Given
        val uri = Uri.parse("https://test.example.com/link/abc123")
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
    fun `test isUniversalLink with old format without link path`() {
        // Given
        val uri = Uri.parse("https://test.limelink.org")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        
        // When
        val result = UniversalLinkHandler.isUniversalLink(intent)
        
        // Then
        assertFalse("Old format without /link/ path should not be valid", result)
    }
    
    @Test
    fun `test isUniversalLink with valid subdomain but invalid path`() {
        // Given
        val uri = Uri.parse("https://test.limelink.org/invalid/path")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        
        // When
        val result = UniversalLinkHandler.isUniversalLink(intent)
        
        // Then
        assertTrue("Should be valid if host matches, path validation happens later", result)
    }
    
    @Test
    fun `test isUniversalLink with deep.limelink.org but different path`() {
        // Given
        val uri = Uri.parse("https://deep.limelink.org/some/other/path")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        
        // When
        val result = UniversalLinkHandler.isUniversalLink(intent)
        
        // Then
        assertTrue("Should be valid if host is deep.limelink.org", result)
    }
    
    @Test
    fun `test isUniversalLink with complex subdomain`() {
        // Given
        val uri = Uri.parse("https://my-app-123.limelink.org/link/complex-suffix")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        
        // When
        val result = UniversalLinkHandler.isUniversalLink(intent)
        
        // Then
        assertTrue("Should be valid with complex subdomain", result)
    }
    
    @Test
    fun `test isUniversalLink with multiple dots in subdomain`() {
        // Given
        val uri = Uri.parse("https://test.example.limelink.org/link/abc123")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        
        // When
        val result = UniversalLinkHandler.isUniversalLink(intent)
        
        // Then
        assertTrue("Should be valid with multiple dots in subdomain", result)
    }
} 