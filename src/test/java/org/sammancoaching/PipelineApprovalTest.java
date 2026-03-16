package org.sammancoaching;

import org.approvaltests.combinations.CombinationApprovals;
import org.junit.jupiter.api.Test;
import org.sammancoaching.dependencies.Project;
import org.sammancoaching.dependencies.TestStatus;

public class PipelineApprovalTest {

    @Test
    void pipeline() {
        TestStatus[] testStatuses = {
                TestStatus.PASSING_TESTS,
                TestStatus.NO_TESTS,
                TestStatus.FAILING_TESTS
        };
        Boolean[] sendSummaries = {true, false};
        Boolean[] stagingDeployments = {true, false};
        TestStatus[] smokeTestStatuses = {
                TestStatus.PASSING_TESTS,
                TestStatus.NO_TESTS,
                TestStatus.FAILING_TESTS
        };
        Boolean[] productionDeployments = {true, false};

        CombinationApprovals.verifyAllCombinations(
                this::doPipelineRun,
                testStatuses,
                sendSummaries,
                stagingDeployments,
                smokeTestStatuses,
                productionDeployments
        );
    }

    private String doPipelineRun(
            TestStatus testStatus,
            boolean sendSummary,
            boolean deploysSuccessfullyToStaging,
            TestStatus smokeTestStatus,
            boolean deploysSuccessfullyToProduction
    ) {
        var spy = new StringBuilder("\n");
        var config = new DefaultConfig(sendSummary);
        var emailer = new CapturingEmailer(spy);
        var log = new CapturingLogger(spy);
        var pipeline = new Pipeline(config, emailer, log);

        Project project = Project.builder()
                .setTestStatus(testStatus)
                .setDeploysSuccessfullyToStaging(deploysSuccessfullyToStaging)
                .setSmokeTestStatus(smokeTestStatus)
                .setDeploysSuccessfully(deploysSuccessfullyToProduction)
                .build();

        pipeline.run(project);
        return spy.toString();
    }
}