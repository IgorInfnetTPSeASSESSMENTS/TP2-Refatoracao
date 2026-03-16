package org.sammancoaching.dependencies;

public enum PipelineStatus {
    SUCCESS,
    TESTS_FAILED,
    STAGING_DEPLOY_FAILED,
    NO_SMOKE_TESTS,
    SMOKE_TESTS_FAILED,
    PRODUCTION_DEPLOY_FAILED
}