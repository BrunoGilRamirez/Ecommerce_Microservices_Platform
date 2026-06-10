// UserPage.tsx: Main page for user management
import { useUser } from "../Security/useUser";
import { UserProfile } from "./UserProfile";

export function UserPage() {
  const {
    currentUserInfo,
    userProfile,
    hasProfile,
    isLoading,
    error,
    refreshUserProfile,
  } = useUser();

  if (isLoading) {
    return (
      <div className="container">
        <div className="loading-container">
          <div className="loading-spinner large"></div>
          <p>Loading user information...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container">
        <div className="alert alert-error">
          <h3>Error loading user information</h3>
          <p>{error}</p>
          <button
            className="btn btn-primary"
            onClick={() => window.location.reload()}
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  if (!currentUserInfo) {
    return (
      <div className="container">
        <div className="alert alert-error">
          <h3>Authentication error</h3>
          <p>Could not retrieve authenticated user information.</p>
        </div>
      </div>
    );
  }
  console.log("Current user info:", currentUserInfo);
  console.log("User profile:", userProfile);

  return (
    <UserProfile
      userProfile={userProfile}
      userEmail={currentUserInfo.subject} //subject contains the email
      userName={currentUserInfo.name}
      onProfileUpdated={refreshUserProfile}
      isCreating={!hasProfile}
    />
  );
}
