package com.worth.ifs.competitionsetup.service.formpopulator;

import org.springframework.stereotype.Service;

import com.worth.ifs.competition.resource.CompetitionResource;
import com.worth.ifs.competition.resource.CompetitionSetupSection;
import com.worth.ifs.competitionsetup.form.CompetitionSetupForm;
import com.worth.ifs.competitionsetup.form.MilestonesForm;

import java.time.LocalDate;
import java.util.List;

/**
 * Form populator for the milestones competition setup section.
 */
@Service
public class MilestonesFormPopulator implements CompetitionSetupFormPopulator {

	@Override
	public CompetitionSetupSection sectionToFill() {
		return CompetitionSetupSection.MILESTONES;
	}

	@Override
	public CompetitionSetupForm populateForm(CompetitionResource competitionResource) {
		MilestonesForm competitionSetupForm = new MilestonesForm();

		//temporary date
		LocalDate currentDate = LocalDate.now();
        List<Long> milestones = competitionResource.getMilestones();

        if (milestones == null) {
            competitionSetupForm.setOpenDateDay(null);
            competitionSetupForm.setOpenDateMonth(null);
            competitionSetupForm.setOpenDateYear(null);
        }
        else {
            competitionSetupForm.setOpenDateDay(null);
            competitionSetupForm.setOpenDateMonth(null);
            competitionSetupForm.setOpenDateYear(null);
        }

		competitionSetupForm.setOpenDateDayOfWeek(dateFormatter(currentDate.getDayOfMonth(), currentDate.getMonthValue(), currentDate.getYear()) );

		competitionSetupForm.setBriefingEventDay(currentDate.getDayOfMonth());
		competitionSetupForm.setBriefingEventMonth(currentDate.getMonthValue());
		competitionSetupForm.setBriefingEventYear(currentDate.getYear());
		competitionSetupForm.setBriefingEventDayOfWeek(dateFormatter(currentDate.getDayOfMonth(), currentDate.getMonthValue(), currentDate.getYear()) );

		competitionSetupForm.setSubmissionDateDay(currentDate.getDayOfMonth());
		competitionSetupForm.setSubmissionDateMonth(currentDate.getMonthValue());
		competitionSetupForm.setSubmissionDateYear(currentDate.getYear());
		competitionSetupForm.setSubmissionDateDayOfWeek(dateFormatter(currentDate.getDayOfMonth(), currentDate.getMonthValue(), currentDate.getYear()) );

		competitionSetupForm.setAllocateAssessorsDay(currentDate.getDayOfMonth());
		competitionSetupForm.setAllocateAssessorsMonth(currentDate.getMonthValue());
		competitionSetupForm.setAllocateAssessorsYear(currentDate.getYear());
		competitionSetupForm.setAllocateAssessorsDayOfWeek(dateFormatter(currentDate.getDayOfMonth(), currentDate.getMonthValue(), currentDate.getYear()) );

		competitionSetupForm.setAssessorBriefingDay(currentDate.getDayOfMonth());
		competitionSetupForm.setAssessorBriefingMonth(currentDate.getMonthValue());
		competitionSetupForm.setAssessorBriefingYear(currentDate.getYear());
		competitionSetupForm.setAssessorBriefingDayOfWeek(dateFormatter(currentDate.getDayOfMonth(), currentDate.getMonthValue(), currentDate.getYear()) );

		competitionSetupForm.setAssessorAcceptsDay(currentDate.getDayOfMonth());
		competitionSetupForm.setAssessorAcceptsMonth(currentDate.getMonthValue());
		competitionSetupForm.setAssessorAcceptsYear(currentDate.getYear());
		competitionSetupForm.setAssessorAcceptsDayOfWeek(dateFormatter(currentDate.getDayOfMonth(), currentDate.getMonthValue(), currentDate.getYear()) );

		competitionSetupForm.setAssessorDeadlineDay(currentDate.getDayOfMonth());
		competitionSetupForm.setAssessorDeadlineMonth(currentDate.getMonthValue());
		competitionSetupForm.setAssessorDeadlineYear(currentDate.getYear());
		competitionSetupForm.setAssessorDeadlineDayOfWeek(dateFormatter(currentDate.getDayOfMonth(), currentDate.getMonthValue(), currentDate.getYear()) );

		competitionSetupForm.setLineDrawDay(currentDate.getDayOfMonth());
		competitionSetupForm.setLineDrawMonth(currentDate.getMonthValue());
		competitionSetupForm.setLineDrawYear(currentDate.getYear());
		competitionSetupForm.setLineDrawDayOfWeek(dateFormatter(currentDate.getDayOfMonth(), currentDate.getMonthValue(), currentDate.getYear()) );

		competitionSetupForm.setAssessmentPanelDay(currentDate.getDayOfMonth());
		competitionSetupForm.setAssessmentPanelMonth(currentDate.getMonthValue());
		competitionSetupForm.setAssessmentPanelYear(currentDate.getYear());
		competitionSetupForm.setAssessmentPanelDayOfWeek(dateFormatter(currentDate.getDayOfMonth(), currentDate.getMonthValue(), currentDate.getYear()) );

		competitionSetupForm.setPanelDateDay(currentDate.getDayOfMonth());
		competitionSetupForm.setPanelDateMonth(currentDate.getMonthValue());
		competitionSetupForm.setPanelDateYear(currentDate.getYear());
		competitionSetupForm.setPanelDateDayOfWeek(dateFormatter(currentDate.getDayOfMonth(), currentDate.getMonthValue(), currentDate.getYear()) );

		competitionSetupForm.setFundersPanelDay(currentDate.getDayOfMonth());
		competitionSetupForm.setFundersPanelMonth(currentDate.getMonthValue());
		competitionSetupForm.setFundersPanelYear(currentDate.getYear());
		competitionSetupForm.setFundersPanelDayOfWeek(dateFormatter(currentDate.getDayOfMonth(), currentDate.getMonthValue(), currentDate.getYear()) );

		competitionSetupForm.setNotificationsDay(currentDate.getDayOfMonth());
		competitionSetupForm.setNotificationsMonth(currentDate.getMonthValue());
		competitionSetupForm.setNotificationsYear(currentDate.getYear());
		competitionSetupForm.setNotificationsDayOfWeek(dateFormatter(currentDate.getDayOfMonth(), currentDate.getMonthValue(), currentDate.getYear()) );

		competitionSetupForm.setReleaseFeedbackDay(currentDate.getDayOfMonth());
		competitionSetupForm.setReleaseFeedbackMonth(currentDate.getMonthValue());
		competitionSetupForm.setReleaseFeedbackYear(currentDate.getYear());
		competitionSetupForm.setReleaseFeedbackDayOfWeek(dateFormatter(currentDate.getDayOfMonth(), currentDate.getMonthValue(), currentDate.getYear()) );

		return competitionSetupForm;
	}

	/*
	 * Returns the first free letters of the day of the week
	 */
	private String dateFormatter(Integer day, Integer month, Integer year) {
		String shortDayName="-";
		try {
			LocalDate localDate = LocalDate.of(year, month, day);
			String dayOfWeek =  localDate.getDayOfWeek().name();
			shortDayName =  dayOfWeek.substring(0,1) + dayOfWeek.substring(1,3).toLowerCase();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return shortDayName;
	}
}
