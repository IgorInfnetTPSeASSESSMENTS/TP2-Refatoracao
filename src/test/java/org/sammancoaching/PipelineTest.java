package org.sammancoaching;

import org.sammancoaching.dependencies.Config;
import org.sammancoaching.dependencies.Emailer;
import org.sammancoaching.dependencies.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.sammancoaching.dependencies.TestStatus.FAILING_TESTS;
import static org.sammancoaching.dependencies.TestStatus.NO_TESTS;
import static org.sammancoaching.dependencies.TestStatus.PASSING_TESTS;

class PipelineTest {
    private final Config config = mock(Config.class);
    private final CapturingLogger log = new CapturingLogger();
    private final Emailer emailer = mock(Emailer.class);

    private Pipeline pipeline;

    @BeforeEach
    void setUp() {
        pipeline = new Pipeline(config, emailer, log);
    }

    @Test
    void project_with_tests_and_full_success_with_email_notification() {
        when(config.sendEmailSummary()).thenReturn(true);

        Project project = Project.builder()
                .setTestStatus(PASSING_TESTS)
                .setDeploysSuccessfullyToStaging(true)
                .setSmokeTestStatus(PASSING_TESTS)
                .setDeploysSuccessfully(true)
                .build();

        pipeline.run(project);

        assertEquals(Arrays.asList(
                "INFO: Tests passed",
                "INFO: Staging deployment successful",
                "INFO: Smoke tests passed",
                "INFO: Production deployment successful",
                "INFO: Sending email"
        ), log.getLoggedLines());

        verify(emailer).send("Deployment completed successfully");
    }

    @Test
    void project_with_tests_and_full_success_without_email_notification() {
        when(config.sendEmailSummary()).thenReturn(false);

        Project project = Project.builder()
                .setTestStatus(PASSING_TESTS)
                .setDeploysSuccessfullyToStaging(true)
                .setSmokeTestStatus(PASSING_TESTS)
                .setDeploysSuccessfully(true)
                .build();

        pipeline.run(project);

        assertEquals(Arrays.asList(
                "INFO: Tests passed",
                "INFO: Staging deployment successful",
                "INFO: Smoke tests passed",
                "INFO: Production deployment successful",
                "INFO: Email disabled"
        ), log.getLoggedLines());

        verify(emailer, never()).send(any());
    }

    @Test
    void project_without_tests_and_full_success_with_email_notification() {
        when(config.sendEmailSummary()).thenReturn(true);

        Project project = Project.builder()
                .setTestStatus(NO_TESTS)
                .setDeploysSuccessfullyToStaging(true)
                .setSmokeTestStatus(PASSING_TESTS)
                .setDeploysSuccessfully(true)
                .build();

        pipeline.run(project);

        assertEquals(Arrays.asList(
                "INFO: No tests",
                "INFO: Staging deployment successful",
                "INFO: Smoke tests passed",
                "INFO: Production deployment successful",
                "INFO: Sending email"
        ), log.getLoggedLines());

        verify(emailer).send("Deployment completed successfully");
    }

    @Test
    void project_without_tests_and_full_success_without_email_notification() {
        when(config.sendEmailSummary()).thenReturn(false);

        Project project = Project.builder()
                .setTestStatus(NO_TESTS)
                .setDeploysSuccessfullyToStaging(true)
                .setSmokeTestStatus(PASSING_TESTS)
                .setDeploysSuccessfully(true)
                .build();

        pipeline.run(project);

        assertEquals(Arrays.asList(
                "INFO: No tests",
                "INFO: Staging deployment successful",
                "INFO: Smoke tests passed",
                "INFO: Production deployment successful",
                "INFO: Email disabled"
        ), log.getLoggedLines());

        verify(emailer, never()).send(any());
    }

    @Test
    void project_with_failing_tests_with_email_notification() {
        when(config.sendEmailSummary()).thenReturn(true);

        Project project = Project.builder()
                .setTestStatus(FAILING_TESTS)
                .build();

        pipeline.run(project);

        assertEquals(Arrays.asList(
                "ERROR: Tests failed",
                "INFO: Sending email"
        ), log.getLoggedLines());

        verify(emailer).send("Pipeline failed - tests failed");
    }

    @Test
    void project_with_failing_tests_without_email_notification() {
        when(config.sendEmailSummary()).thenReturn(false);

        Project project = Project.builder()
                .setTestStatus(FAILING_TESTS)
                .build();

        pipeline.run(project);

        assertEquals(Arrays.asList(
                "ERROR: Tests failed",
                "INFO: Email disabled"
        ), log.getLoggedLines());

        verify(emailer, never()).send(any());
    }

    @Test
    void project_with_staging_deployment_failure_with_email_notification() {
        when(config.sendEmailSummary()).thenReturn(true);

        Project project = Project.builder()
                .setTestStatus(PASSING_TESTS)
                .setDeploysSuccessfullyToStaging(false)
                .build();

        pipeline.run(project);

        assertEquals(Arrays.asList(
                "INFO: Tests passed",
                "ERROR: Staging deployment failed",
                "INFO: Sending email"
        ), log.getLoggedLines());

        verify(emailer).send("Pipeline failed - staging deployment failed");
    }

    @Test
    void project_with_staging_deployment_failure_without_email_notification() {
        when(config.sendEmailSummary()).thenReturn(false);

        Project project = Project.builder()
                .setTestStatus(PASSING_TESTS)
                .setDeploysSuccessfullyToStaging(false)
                .build();

        pipeline.run(project);

        assertEquals(Arrays.asList(
                "INFO: Tests passed",
                "ERROR: Staging deployment failed",
                "INFO: Email disabled"
        ), log.getLoggedLines());

        verify(emailer, never()).send(any());
    }

    @Test
    void project_without_smoke_tests_with_email_notification() {
        when(config.sendEmailSummary()).thenReturn(true);

        Project project = Project.builder()
                .setTestStatus(PASSING_TESTS)
                .setDeploysSuccessfullyToStaging(true)
                .setSmokeTestStatus(NO_TESTS)
                .build();

        pipeline.run(project);

        assertEquals(Arrays.asList(
                "INFO: Tests passed",
                "INFO: Staging deployment successful",
                "ERROR: No smoke tests",
                "INFO: Sending email"
        ), log.getLoggedLines());

        verify(emailer).send("Pipeline failed - no smoke tests");
    }

    @Test
    void project_without_smoke_tests_without_email_notification() {
        when(config.sendEmailSummary()).thenReturn(false);

        Project project = Project.builder()
                .setTestStatus(PASSING_TESTS)
                .setDeploysSuccessfullyToStaging(true)
                .setSmokeTestStatus(NO_TESTS)
                .build();

        pipeline.run(project);

        assertEquals(Arrays.asList(
                "INFO: Tests passed",
                "INFO: Staging deployment successful",
                "ERROR: No smoke tests",
                "INFO: Email disabled"
        ), log.getLoggedLines());

        verify(emailer, never()).send(any());
    }

    @Test
    void project_with_failing_smoke_tests_with_email_notification() {
        when(config.sendEmailSummary()).thenReturn(true);

        Project project = Project.builder()
                .setTestStatus(PASSING_TESTS)
                .setDeploysSuccessfullyToStaging(true)
                .setSmokeTestStatus(FAILING_TESTS)
                .build();

        pipeline.run(project);

        assertEquals(Arrays.asList(
                "INFO: Tests passed",
                "INFO: Staging deployment successful",
                "ERROR: Smoke tests failed",
                "INFO: Sending email"
        ), log.getLoggedLines());

        verify(emailer).send("Pipeline failed - smoke tests failed");
    }

    @Test
    void project_with_failing_smoke_tests_without_email_notification() {
        when(config.sendEmailSummary()).thenReturn(false);

        Project project = Project.builder()
                .setTestStatus(PASSING_TESTS)
                .setDeploysSuccessfullyToStaging(true)
                .setSmokeTestStatus(FAILING_TESTS)
                .build();

        pipeline.run(project);

        assertEquals(Arrays.asList(
                "INFO: Tests passed",
                "INFO: Staging deployment successful",
                "ERROR: Smoke tests failed",
                "INFO: Email disabled"
        ), log.getLoggedLines());

        verify(emailer, never()).send(any());
    }

    @Test
    void project_with_production_deployment_failure_with_email_notification() {
        when(config.sendEmailSummary()).thenReturn(true);

        Project project = Project.builder()
                .setTestStatus(PASSING_TESTS)
                .setDeploysSuccessfullyToStaging(true)
                .setSmokeTestStatus(PASSING_TESTS)
                .setDeploysSuccessfully(false)
                .build();

        pipeline.run(project);

        assertEquals(Arrays.asList(
                "INFO: Tests passed",
                "INFO: Staging deployment successful",
                "INFO: Smoke tests passed",
                "ERROR: Production deployment failed",
                "INFO: Sending email"
        ), log.getLoggedLines());

        verify(emailer).send("Pipeline failed - production deployment failed");
    }

    @Test
    void project_with_production_deployment_failure_without_email_notification() {
        when(config.sendEmailSummary()).thenReturn(false);

        Project project = Project.builder()
                .setTestStatus(PASSING_TESTS)
                .setDeploysSuccessfullyToStaging(true)
                .setSmokeTestStatus(PASSING_TESTS)
                .setDeploysSuccessfully(false)
                .build();

        pipeline.run(project);

        assertEquals(Arrays.asList(
                "INFO: Tests passed",
                "INFO: Staging deployment successful",
                "INFO: Smoke tests passed",
                "ERROR: Production deployment failed",
                "INFO: Email disabled"
        ), log.getLoggedLines());

        verify(emailer, never()).send(any());
    }
}