package com.espertech.esper.client.deploy;

import java.util.Set;
import java.util.List;

/**
 * Result object of an undeployment operation.
 */
public class UndeploymentResult
{
    private final String deploymentId;
    private final List<DeploymentInformationItem> statementInfo;

    /**
     * Ctor.
     * @param deploymentId id generated by deployment operation
     * @param statementInfo statement-level deployment information
     */
    public UndeploymentResult(String deploymentId, List<DeploymentInformationItem> statementInfo)
    {
        this.deploymentId = deploymentId;
        this.statementInfo = statementInfo;
    }

    /**
     * Returns the deployment id.
     * @return id
     */
    public String getDeploymentId()
    {
        return deploymentId;
    }

    /**
     * Statement-level undeploy information.
     * @return statement info
     */
    public List<DeploymentInformationItem> getStatementInfo()
    {
        return statementInfo;
    }
}
