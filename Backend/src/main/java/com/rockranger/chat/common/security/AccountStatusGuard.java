package com.rockranger.chat.common.security;

import com.rockranger.chat.common.error.ApiException;
import com.rockranger.chat.common.error.ErrorCode;
import com.rockranger.chat.user.domain.User;
import com.rockranger.chat.user.domain.UserStatus;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AccountStatusGuard {

    public void checkCanAuthenticate(User user) {
        if (user.getStatus() == UserStatus.PENDING_DELETION) {
            throw new ApiException(
                    ErrorCode.ACCOUNT_PENDING_DELETION,
                    "This account is scheduled for deletion. Check your email for a recovery link.",
                    Map.of("code", "ACCOUNT_PENDING_DELETION", "recoveryHint", "Check your email for a recovery link.")
            );
        }
        if (user.getStatus() == UserStatus.DEACTIVATED) {
            throw new ApiException(ErrorCode.ACCOUNT_DELETED, "This account has been permanently deleted.");
        }
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new ApiException(ErrorCode.ACCOUNT_SUSPENDED, "This account is suspended.");
        }
    }

    public void checkActive(CurrentUser user) {
        if (user.getStatus() == UserStatus.PENDING_DELETION) {
            throw new ApiException(
                    ErrorCode.ACCOUNT_PENDING_DELETION,
                    "This account is scheduled for deletion.",
                    Map.of("code", "ACCOUNT_PENDING_DELETION")
            );
        }
        if (user.getStatus() == UserStatus.DEACTIVATED) {
            throw new ApiException(ErrorCode.ACCOUNT_DELETED, "This account has been permanently deleted.");
        }
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new ApiException(ErrorCode.ACCOUNT_SUSPENDED, "This account is suspended.");
        }
        if (user.getStatus() == UserStatus.PENDING_SETUP) {
            throw new ApiException(ErrorCode.ACCOUNT_NOT_ACTIVE, "Please complete setup before using protected features.");
        }
    }

    public void checkActive(User user) {
        if (user.getStatus() == UserStatus.PENDING_DELETION) {
            throw new ApiException(
                    ErrorCode.ACCOUNT_PENDING_DELETION,
                    "This account is scheduled for deletion.",
                    Map.of("code", "ACCOUNT_PENDING_DELETION")
            );
        }
        if (user.getStatus() == UserStatus.DEACTIVATED) {
            throw new ApiException(ErrorCode.ACCOUNT_DELETED, "This account has been permanently deleted.");
        }
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new ApiException(ErrorCode.ACCOUNT_SUSPENDED, "This account is suspended.");
        }
        if (user.getStatus() == UserStatus.PENDING_SETUP) {
            throw new ApiException(ErrorCode.ACCOUNT_NOT_ACTIVE, "Please complete setup before using protected features.");
        }
    }
}
