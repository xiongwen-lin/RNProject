package com.afar.osaio.smart.electrician.eventbus;

public class UpdateProfileEvent {

    private boolean isUpdateProfile;

    public boolean isUpdateProfile() {
        return isUpdateProfile;
    }

    public void setUpdateProfile(boolean updateProfile) {
        isUpdateProfile = updateProfile;
    }

    public UpdateProfileEvent(boolean updateProfile) {
        this.isUpdateProfile = updateProfile;
    }

}
