package com.suguna.weighment_api_service.constants;

/**
 * Documented mobile workflow states (Flutter). The API may expose these in payloads later;
 * keep names aligned with the field app state machine.
 */
public enum ClientAppState {
    ACTIVATION_REQUIRED,
    AUTHENTICATED,
    SCHEDULE_ASSIGNED,
    SCHEDULE_DOWNLOADED,
    IN_PROGRESS,
    COMPLETED_LOCALLY,
    PENDING_SYNC,
    SYNCHRONIZING,
    SYNCED,
    SYNC_FAILED
}
