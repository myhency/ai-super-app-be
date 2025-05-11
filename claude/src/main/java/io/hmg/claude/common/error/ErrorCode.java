package io.hmg.claude.common.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    H400A("H400A", "Invalid request. Please check and try again.", HttpStatus.BAD_REQUEST),
    H401A("H401A", "Verification code not found. Please check and try again.", HttpStatus.UNAUTHORIZED),
    H401B("H401B", "Failed to retrieve an access token from Microsoft OAuth. Please ensure your credentials are valid.", HttpStatus.UNAUTHORIZED),
    H401C("H401C", "Unauthorized tenant ID. Access is restricted for this tenant.", HttpStatus.UNAUTHORIZED),
    H401D("H401D", "Client ID mismatch. Please verify your client configuration.", HttpStatus.UNAUTHORIZED),
    H401E("H401E", "Failed to retrieve user details from Microsoft Graph API. Please check your access token.", HttpStatus.UNAUTHORIZED),
    H401F("H401F", "Failed to create a new user", HttpStatus.UNAUTHORIZED),
    H401G("H401G", "Bearer token is missing in the request header", HttpStatus.UNAUTHORIZED),
    H401H("H401H", "Failed to get token from cache", HttpStatus.UNAUTHORIZED),
    H401I("H401I", "Failed to refresh token", HttpStatus.UNAUTHORIZED),
    H500A("H500A", "An unexpected server error occurred. Please contact support if the problem persists.", HttpStatus.INTERNAL_SERVER_ERROR),
    H500B("H500B", "Server error while requesting an access token from Microsoft OAuth. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR),
    H500C("H500C", "The provided token has expired. Please authenticate again.", HttpStatus.INTERNAL_SERVER_ERROR),
    H500D("H500D", "An error occurred while fetching user details from Microsoft Graph API. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR),
    H500E("H500E", "Something wrong with redis", HttpStatus.INTERNAL_SERVER_ERROR),
    H500F("H500F", "Failed to receive answer from Azure OpenAI API", HttpStatus.INTERNAL_SERVER_ERROR);


    private final String code;
    private final String description;
    private final HttpStatus httpStatus;
}
