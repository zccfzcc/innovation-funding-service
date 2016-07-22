package com.worth.ifs.assessment.repository;

import com.worth.ifs.BaseRepositoryIntegrationTest;
import com.worth.ifs.application.domain.Question;
import com.worth.ifs.application.repository.QuestionRepository;
import com.worth.ifs.assessment.domain.Assessment;
import com.worth.ifs.assessment.domain.AssessorFormInputResponse;
import com.worth.ifs.form.domain.FormInput;
import com.worth.ifs.form.repository.FormInputRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static com.worth.ifs.assessment.builder.AssessorFormInputResponseBuilder.newAssessorFormInputResponse;
import static com.worth.ifs.form.builder.FormInputBuilder.newFormInput;
import static com.worth.ifs.form.resource.FormInputScope.ASSESSMENT;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

public class AssessorFormInputResponseRepositoryIntegrationTest extends BaseRepositoryIntegrationTest<AssessorFormInputResponseRepository> {

    @Autowired
    private AssessmentRepository assessmentRepository;

    @Autowired
    private FormInputRepository formInputRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    @Override
    protected void setRepository(AssessorFormInputResponseRepository repository) {
        this.repository = repository;
    }

    @Test
    public void testFindAll() throws Exception {
        repository.deleteAll();

        Assessment assessment = assessmentRepository.save(new Assessment());

        List<Question> questions = asList(new Question(), new Question()).stream().map(question -> questionRepository.save(question)).collect(toList());

        List<FormInput> formInputs = newFormInput()
                .withId(null, null)
                .withQuestion(questions.get(0), questions.get(1))
                .withPriority(0, 0)
                .withScope(ASSESSMENT, ASSESSMENT)
                .build(2).stream().map(formInput -> formInputRepository.save(formInput)).collect(toList());

        List<AssessorFormInputResponse> saved = newAssessorFormInputResponse()
                .withId(null, null)
                .withAssessment(assessment, assessment)
                .withFormInput(formInputs.get(0), formInputs.get(1))
                .withValue("Sample response 1", "Sample response 2")
                .withUpdatedDate(LocalDateTime.parse("2016-07-12T16:10:50.21"), LocalDateTime.parse("2016-07-12T16:15:25.42"))
                .build(2).stream().map(assessorFormInputResponse -> repository.save(assessorFormInputResponse)).collect(toList());

        List<AssessorFormInputResponse> found = repository.findAll();

        assertEquals(2, found.size());
        assertEquals(saved.get(0), found.get(0));
        assertEquals(saved.get(1), found.get(1));
    }

    @Test
    public void testFindByAssessmentId() throws Exception {
        List<Assessment> assessments = asList(new Assessment(), new Assessment()).stream().map(assessment -> assessmentRepository.save(assessment)).collect(toList());

        // Save two questions
        List<Question> questions = asList(new Question(), new Question()).stream().map(question -> questionRepository.save(question)).collect(toList());

        // Save two form inputs for each of the two questions
        List<FormInput> formInputs = newFormInput()
                .withId(null, null)
                .withQuestion(questions.get(0), questions.get(1), questions.get(0), questions.get(1))
                .withPriority(0, 1, 0, 1)
                .withScope(ASSESSMENT, ASSESSMENT, ASSESSMENT, ASSESSMENT)
                .build(4).stream().map(formInput -> formInputRepository.save(formInput)).collect(toList());

        // For each of the assessments, save one response for each of the four form inputs
        List<AssessorFormInputResponse> saved = newAssessorFormInputResponse()
                .withId(null, null, null, null, null, null, null, null)
                .withAssessment(assessments.get(0), assessments.get(0), assessments.get(0), assessments.get(0), assessments.get(1), assessments.get(1), assessments.get(1), assessments.get(1))
                .withFormInput(formInputs.get(0), formInputs.get(1), formInputs.get(2), formInputs.get(3), formInputs.get(0), formInputs.get(1), formInputs.get(2), formInputs.get(3))
                .withValue("Sample response 1", "Sample response 2", "Sample response 3", "Sample response 4", "Sample response 5", "Sample response 6", "Sample response 7", "Sample response 8")
                .withUpdatedDate(LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now())
                .build(8).stream().map(assessorFormInputResponse -> repository.save(assessorFormInputResponse)).collect(toList());

        // There should be four responses found for each of the questions
        List<AssessorFormInputResponse> expectedForAssessment1 = saved.subList(0, 4);
        List<AssessorFormInputResponse> expectedForAssessment2 = saved.subList(4, 8);

        List<AssessorFormInputResponse> foundForAssessment1 = repository.findByAssessmentId(assessments.get(0).getId());
        List<AssessorFormInputResponse> foundForAssessment2 = repository.findByAssessmentId(assessments.get(1).getId());
        assertEquals(expectedForAssessment1, foundForAssessment1);
        assertEquals(expectedForAssessment2, foundForAssessment2);
    }

    @Test
    public void testFindByAssessmentIdAndFormInputQuestionId() throws Exception {
        Assessment assessment = assessmentRepository.save(new Assessment());

        // Save two questions
        List<Question> questions = asList(new Question(), new Question()).stream().map(question -> questionRepository.save(question)).collect(toList());

        // Save two form inputs for each of the two questions
        List<FormInput> formInputs = newFormInput()
                .withId(null, null, null, null)
                .withQuestion(questions.get(0), questions.get(0), questions.get(1), questions.get(1))
                .withPriority(0, 1, 0, 1)
                .withScope(ASSESSMENT, ASSESSMENT, ASSESSMENT, ASSESSMENT)
                .build(4).stream().map(formInput -> formInputRepository.save(formInput)).collect(toList());

        // For the assessment, save one response for each of the four form inputs
        List<AssessorFormInputResponse> saved = newAssessorFormInputResponse()
                .withId(null, null, null, null)
                .withAssessment(assessment, assessment, assessment, assessment)
                .withFormInput(formInputs.get(0), formInputs.get(1), formInputs.get(2), formInputs.get(3))
                .withValue("Sample response 1", "Sample response 2", "Sample response 3", "Sample response 4")
                .withUpdatedDate(LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now())
                .build(4).stream().map(assessorFormInputResponse -> repository.save(assessorFormInputResponse)).collect(toList());

        // There should be two responses found for each of the questions
        List<AssessorFormInputResponse> expectedForQuestion1 = saved.subList(0, 2);
        List<AssessorFormInputResponse> expectedForQuestion2 = saved.subList(2, 4);

        List<AssessorFormInputResponse> foundForQuestion1 = repository.findByAssessmentIdAndFormInputQuestionId(assessment.getId(), questions.get(0).getId());
        List<AssessorFormInputResponse> foundForQuestion2 = repository.findByAssessmentIdAndFormInputQuestionId(assessment.getId(), questions.get(1).getId());
        assertEquals(expectedForQuestion1, foundForQuestion1);
        assertEquals(expectedForQuestion2, foundForQuestion2);
    }

    @Test
    public void testFindByAssessmentIdAndFormInputId() throws Exception {
        List<Assessment> assessments = asList(new Assessment(), new Assessment()).stream().map(assessment -> assessmentRepository.save(assessment)).collect(toList());

        // Save a question
        Question question = questionRepository.save(new Question());

        // Save two form inputs for the question
        List<FormInput> formInputs = newFormInput()
                .withId(null, null)
                .withQuestion(question, question)
                .withPriority(0, 1)
                .withScope(ASSESSMENT, ASSESSMENT)
                .build(2).stream().map(formInput -> formInputRepository.save(formInput)).collect(toList());

        // For each of the assessments, save one response for each of the two form inputs
        List<AssessorFormInputResponse> saved = newAssessorFormInputResponse()
                .withId(null, null, null, null)
                .withAssessment(assessments.get(0), assessments.get(0), assessments.get(1), assessments.get(1))
                .withFormInput(formInputs.get(0), formInputs.get(1), formInputs.get(0), formInputs.get(1))
                .withValue("Sample response 1", "Sample response 2", "Sample response 3", "Sample response 4")
                .withUpdatedDate(LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now())
                .build(4).stream().map(assessorFormInputResponse -> repository.save(assessorFormInputResponse)).collect(toList());

        // There should be a response found for each of the form inputs, for each assessment
        AssessorFormInputResponse foundForAssessment1FormInput1 = repository.findByAssessmentIdAndFormInputId(assessments.get(0).getId(), formInputs.get(0).getId());
        AssessorFormInputResponse foundForAssessment1FormInput2 = repository.findByAssessmentIdAndFormInputId(assessments.get(0).getId(), formInputs.get(1).getId());
        AssessorFormInputResponse foundForAssessment2FormInput1 = repository.findByAssessmentIdAndFormInputId(assessments.get(1).getId(), formInputs.get(0).getId());
        AssessorFormInputResponse foundForAssessment2FormInput2 = repository.findByAssessmentIdAndFormInputId(assessments.get(1).getId(), formInputs.get(1).getId());
        assertEquals(saved.get(0), foundForAssessment1FormInput1);
        assertEquals(saved.get(1), foundForAssessment1FormInput2);
        assertEquals(saved.get(2), foundForAssessment2FormInput1);
        assertEquals(saved.get(3), foundForAssessment2FormInput2);
    }
}