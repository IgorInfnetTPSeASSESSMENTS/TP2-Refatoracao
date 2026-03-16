package org.sammancoaching;

import org.sammancoaching.dependencies.PipelineStatus;

public class PipelineResult {
    private final PipelineStatus status;

    public PipelineResult(PipelineStatus status) {
        this.status = status;
    }

    public String getEmailMessage() {
        return switch (status) {
            case SUCCESS -> "Deployment completed successfully";
            case TESTS_FAILED -> "Pipeline failed - tests failed";
            case STAGING_DEPLOY_FAILED -> "Pipeline failed - staging deployment failed";
            case NO_SMOKE_TESTS -> "Pipeline failed - no smoke tests";
            case SMOKE_TESTS_FAILED -> "Pipeline failed - smoke tests failed";
            case PRODUCTION_DEPLOY_FAILED -> "Pipeline failed - production deployment failed";
        };
    }
}