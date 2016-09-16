package com.worth.ifs.project.workflow.projectdetails.actions;

import com.worth.ifs.project.workflow.projectdetails.configuration.ProjectDetailsWorkflowService;
import org.springframework.stereotype.Component;

/**
 * The {@code {@link ReadyToSubmitProjectDetailsAction }} is triggered when all Project Details have been filled in.
 * At this point the Project Details are ready to be submitted.
 *
 * For more info see {@link ProjectDetailsWorkflowService}
 */
@Component
public class ReadyToSubmitProjectDetailsAction extends BaseProjectDetailsAction {

}
