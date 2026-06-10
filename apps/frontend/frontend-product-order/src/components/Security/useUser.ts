// useUser.ts: Custom hook for managing user state
import { useState, useEffect } from "react";
import type { User, CurrentUserInfo } from "./userService";
import { getCurrentUserInfo, checkUserProfile } from "./userService";

export interface UserState {
  currentUserInfo: CurrentUserInfo | null;
  userProfile: User | null;
  hasProfile: boolean;
  isLoading: boolean;
  error: string | null;
}

export function useUser() {
  const [userState, setUserState] = useState<UserState>({
    currentUserInfo: null,
    userProfile: null,
    hasProfile: false,
    isLoading: true,
    error: null,
  });

  const loadUserData = async () => {
    try {
      setUserState((prev) => ({ ...prev, isLoading: true, error: null }));

      // First get current user information from JWT
      const currentUserInfo = await getCurrentUserInfo();
      console.log("Current user info:", currentUserInfo);

      // Then check if the profile exists in the user service
      const userProfile = await checkUserProfile(currentUserInfo.subject);

      setUserState({
        currentUserInfo,
        userProfile,
        hasProfile: userProfile !== null,
        isLoading: false,
        error: null,
      });
    } catch (error) {
      console.error("Error loading user data:", error);

      let errorMessage = "Unknown error";
      if (error instanceof Error) {
        if (error.message === "Failed to fetch") {
          errorMessage =
            "Server connection error. Verify that the backend is running.";
        } else {
          errorMessage = error.message;
        }
      }

      setUserState((prev) => ({
        ...prev,
        isLoading: false,
        error: errorMessage,
      }));
    }
  };

  const refreshUserProfile = async () => {
    if (!userState.currentUserInfo) return;
    console.log("Refreshing user profile for:", userState.currentUserInfo);
    try {
      const userProfile = await checkUserProfile(
        userState.currentUserInfo.subject
      );
      setUserState((prev) => ({
        ...prev,
        userProfile,
        hasProfile: userProfile !== null,
        error: null, // Clear errors on successful refresh
      }));
    } catch (error) {
      console.error("Error refreshing user profile:", error);
      setUserState((prev) => ({
        ...prev,
        error:
          error instanceof Error ? error.message : "Error updating profile",
      }));
    }
  };

  const clearError = () => {
    setUserState((prev) => ({ ...prev, error: null }));
  };

  const retryLoadUserData = async () => {
    clearError();
    await loadUserData();
  };

  useEffect(() => {
    loadUserData();
  }, []);

  return {
    ...userState,
    refreshUserProfile,
    reloadUserData: loadUserData,
    clearError,
    retryLoadUserData,
  };
}
