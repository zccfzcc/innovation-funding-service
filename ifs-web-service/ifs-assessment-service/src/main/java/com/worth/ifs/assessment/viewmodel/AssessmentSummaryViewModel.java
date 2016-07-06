package com.worth.ifs.assessment.viewmodel;

import com.worth.ifs.application.resource.ApplicationResource;
import com.worth.ifs.competition.resource.CompetitionResource;

import java.util.List;

/**
 * Holder of model attribute of Assessment Summary, which combine the all of questions and existing assessment feedback data
 * , application and competitions
 */
public class AssessmentSummaryViewModel {

    private List<QuestionWithFeedbackHelper> listOfQuestionWithFeedback;
    private ApplicationResource application;
    private CompetitionResource competition;

    public AssessmentSummaryViewModel(List<QuestionWithFeedbackHelper> listOfQuestionWithFeedback, ApplicationResource application, CompetitionResource competition) {
        this.listOfQuestionWithFeedback = listOfQuestionWithFeedback;
        this.application = application;
        this.competition = competition;
    }


     public List<QuestionWithFeedbackHelper> getListOfQuestionWithFeedback() {
          return listOfQuestionWithFeedback;
      }

     public ApplicationResource getApplicationResource(){
        return this.application;
      }
      public CompetitionResource getCompetitionResource(){
        return this.competition;
    }
}
