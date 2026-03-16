package org.sammancoaching;

import org.sammancoaching.dependencies.Config;
import org.sammancoaching.dependencies.DeploymentEnvironment;
import org.sammancoaching.dependencies.Emailer;
import org.sammancoaching.dependencies.Logger;
import org.sammancoaching.dependencies.PipelineStatus;
import org.sammancoaching.dependencies.Project;
import org.sammancoaching.dependencies.TestStatus;

public class Pipeline {
    private final Config config;
    private final Emailer emailer;
    private final Logger log;

    public Pipeline(Config config, Emailer emailer, Logger log) {
        this.config = config;
        this.emailer = emailer;
        this.log = log;
    }

    public void run(Project project) {
        PipelineResult result = executePipeline(project);
        sendSummary(result);
    }

    private PipelineResult executePipeline(Project project) {
        if (!testsPassed(project)) {
            return new PipelineResult(PipelineStatus.TESTS_FAILED);
        }

        if (!stagingDeploymentSucceeded(project)) {
            return new PipelineResult(PipelineStatus.STAGING_DEPLOY_FAILED);
        }

        if (!smokeTestsExist(project)) {
            log.error("No smoke tests");
            return new PipelineResult(PipelineStatus.NO_SMOKE_TESTS);
        }

        if (!smokeTestsPassed(project)) {
            return new PipelineResult(PipelineStatus.SMOKE_TESTS_FAILED);
        }

        if (!productionDeploymentSucceeded(project)) {
            return new PipelineResult(PipelineStatus.PRODUCTION_DEPLOY_FAILED);
        }

        return new PipelineResult(PipelineStatus.SUCCESS);
    }

    private boolean testsPassed(Project project) {
        if (!project.hasTests()) {
            log.info("No tests");
            return true;
        }

        if ("success".equals(project.runTests())) {
            log.info("Tests passed");
            return true;
        }

        log.error("Tests failed");
        return false;
    }

    private boolean stagingDeploymentSucceeded(Project project) {
        if ("success".equals(project.deploy(DeploymentEnvironment.STAGING))) {
            log.info("Staging deployment successful");
            return true;
        }

        log.error("Staging deployment failed");
        return false;
    }

    private boolean smokeTestsExist(Project project) {
        return project.runSmokeTests() != TestStatus.NO_TESTS;
    }

    private boolean smokeTestsPassed(Project project) {
        if (project.runSmokeTests() == TestStatus.PASSING_TESTS) {
            log.info("Smoke tests passed");
            return true;
        }

        log.error("Smoke tests failed");
        return false;
    }

    private boolean productionDeploymentSucceeded(Project project) {
        if ("success".equals(project.deploy(DeploymentEnvironment.PRODUCTION))) {
            log.info("Production deployment successful");
            return true;
        }

        log.error("Production deployment failed");
        return false;
    }

    private void sendSummary(PipelineResult result) {
        if (!config.sendEmailSummary()) {
            log.info("Email disabled");
            return;
        }

        log.info("Sending email");
        emailer.send(result.getEmailMessage());
    }
}