package com.worth.ifs.dashboard;

import com.worth.ifs.application.domain.Application;
import com.worth.ifs.application.service.ApplicationRestService;
import com.worth.ifs.assessment.domain.Assessment;
import com.worth.ifs.assessment.service.AssessmentRestService;
import com.worth.ifs.competition.domain.Competition;
import com.worth.ifs.competition.service.CompetitionsRestService;
import com.worth.ifs.security.UserAuthenticationService;
import com.worth.ifs.user.domain.ProcessRole;
import com.worth.ifs.user.domain.User;
import com.worth.ifs.user.domain.UserRoleType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This controller will handle requests related to the current applicant. So pages that are relative to that user,
 * are implemented here. For example the my-applications page.
 */
@Controller
@RequestMapping("/assessor")
public class AssessorController {
    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    CompetitionsRestService competitionService;
    @Autowired
    ApplicationRestService applicationService;

    @Autowired
    AssessmentRestService assessmentRestService;

    @Autowired
    UserAuthenticationService userAuthenticationService;



    private List<Application> applicationsForAssessment(Long competitionId, final Long userId) {
        return applicationService.getApplicationsByCompetitionIdAndUserId(competitionId,userId, UserRoleType.ASSESSOR);
    }
    private User getLoggedUser(HttpServletRequest req) {
        return userAuthenticationService.getAuthenticatedUser(req);
    }

    @RequestMapping(value="/dashboard", method= RequestMethod.GET)
    public String dashboard(Model model, HttpServletRequest request) {

        //gets logged user to know what to show
        User user = getLoggedUser(request);

        //for now gets all the competitions to show in the dashboard (assumes user was invited and accepted all)
        List<Competition> competitions = competitionService.getAll();

        Map<Long, Long> competitionsTotalAssignedAssessments = new HashMap<>();
        Map<Long, Integer> competitionsSubmittedAssessments = new HashMap<>();

        for ( Competition c : competitions ) {
            competitionsTotalAssignedAssessments.put(c.getId(), assessmentRestService.getTotalAssignedAssessmentsByCompetition(getLoggedUser(request).getId(), c.getId()));
            competitionsSubmittedAssessments.put(c.getId(), assessmentRestService.getTotalSubmittedAssessmentsByCompetition(getLoggedUser(request).getId(), c.getId()));
        }

        model.addAttribute("totalAssignedAssessments", competitionsTotalAssignedAssessments);
        model.addAttribute("submittedAssessments", competitionsSubmittedAssessments);


        //pass to view
        model.addAttribute("competitionsForAssessment", competitions);


        return "assessor-dashboard";
    }

    @RequestMapping(value="/competitions/{competitionId}/applications", method= RequestMethod.GET)
    public String competitionApplications(Model model, @PathVariable("competitionId") final Long competitionId,
                                          HttpServletRequest request) {

        Competition competition = competitionService.getCompetitionById(competitionId);

        List<Assessment> assessments = assessmentRestService.getAssessmentsByCompetition( getLoggedUser(request).getId(), competition.getId());

        //pass to view
        model.addAttribute("competition", competition);
        model.addAttribute("assessments", assessments);


        return "assessor-competition-applications";
    }

    private ProcessRole getAssessorApplicationRole(List<ProcessRole> roles, final Long userId, UserRoleType role) {
        int indexAt = -1;
        int i = 0;
        while( indexAt == -1 && i < roles.size()) {
            if ( roles.get(i).getUser().getId().equals(userId) && roles.get(i).getRole().getName().equals(role.getName()) )
                indexAt = -1;

            i++;
        }

        return roles.get(indexAt);
    }





}
