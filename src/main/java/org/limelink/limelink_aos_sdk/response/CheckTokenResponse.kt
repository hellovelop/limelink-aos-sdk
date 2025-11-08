package org.limelink.limelink_aos_sdk.response

import com.google.gson.annotations.SerializedName

/**
 * Response model for token duplication check
 * @param isExist Whether the token already exists (true: duplicate, false: not duplicate)
 */
data class CheckTokenResponse(
    @SerializedName("is_exist")
    val isExist: Boolean
)

