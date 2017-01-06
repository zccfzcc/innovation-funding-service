package org.innovateuk.ifs.assessment.transactional;

import org.innovateuk.ifs.assessment.mapper.AssessorInviteToSendMapper;
import org.innovateuk.ifs.assessment.mapper.CompetitionInviteMapper;
import org.innovateuk.ifs.category.domain.Category;
import org.innovateuk.ifs.category.mapper.CategoryMapper;
import org.innovateuk.ifs.category.repository.CategoryRepository;
import org.innovateuk.ifs.category.resource.CategoryResource;
import org.innovateuk.ifs.commons.error.Error;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.domain.Competition;
import org.innovateuk.ifs.competition.repository.CompetitionRepository;
import org.innovateuk.ifs.email.resource.EmailContent;
import org.innovateuk.ifs.invite.domain.CompetitionInvite;
import org.innovateuk.ifs.invite.domain.CompetitionParticipant;
import org.innovateuk.ifs.invite.domain.RejectionReason;
import org.innovateuk.ifs.invite.repository.CompetitionInviteRepository;
import org.innovateuk.ifs.invite.repository.CompetitionParticipantRepository;
import org.innovateuk.ifs.invite.repository.RejectionReasonRepository;
import org.innovateuk.ifs.invite.resource.*;
import org.innovateuk.ifs.notifications.resource.ExternalUserNotificationTarget;
import org.innovateuk.ifs.notifications.resource.Notification;
import org.innovateuk.ifs.notifications.resource.NotificationTarget;
import org.innovateuk.ifs.notifications.resource.SystemNotificationSource;
import org.innovateuk.ifs.notifications.service.senders.NotificationSender;
import org.innovateuk.ifs.user.domain.User;
import org.innovateuk.ifs.user.repository.UserRepository;
import org.innovateuk.ifs.user.resource.BusinessType;
import org.innovateuk.ifs.user.resource.UserResource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.util.Collections.*;
import static java.util.stream.Collectors.toList;
import static org.innovateuk.ifs.category.resource.CategoryType.INNOVATION_AREA;
import static org.innovateuk.ifs.commons.error.CommonErrors.notFoundError;
import static org.innovateuk.ifs.commons.error.CommonFailureKeys.*;
import static org.innovateuk.ifs.commons.service.ServiceResult.*;
import static org.innovateuk.ifs.competition.resource.CompetitionStatus.*;
import static org.innovateuk.ifs.invite.constant.InviteStatus.CREATED;
import static org.innovateuk.ifs.invite.constant.InviteStatus.OPENED;
import static org.innovateuk.ifs.invite.domain.Invite.generateInviteHash;
import static org.innovateuk.ifs.invite.domain.ParticipantStatus.ACCEPTED;
import static org.innovateuk.ifs.invite.domain.ParticipantStatus.REJECTED;
import static org.innovateuk.ifs.util.CollectionFunctions.mapWithIndex;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleMap;
import static org.innovateuk.ifs.util.EntityLookupCallbacks.find;
import static org.innovateuk.ifs.util.MapFunctions.asMap;

/**
 * Service for managing {@link org.innovateuk.ifs.invite.domain.CompetitionInvite}s.
 */
@Service
@Transactional
public class CompetitionInviteServiceImpl implements CompetitionInviteService {

    private static final String WEB_CONTEXT = "/assessment";

    @Autowired
    private CompetitionInviteRepository competitionInviteRepository;

    @Autowired
    private CompetitionParticipantRepository competitionParticipantRepository;

    @Autowired
    private RejectionReasonRepository rejectionReasonRepository;

    @Autowired
    private CompetitionRepository competitionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CompetitionInviteMapper competitionInviteMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private AssessorInviteToSendMapper toSendMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationSender notificationSender;

    @Autowired
    private SystemNotificationSource systemNotificationSource;

    @Value("${ifs.web.baseURL}")
    private String webBaseUrl;

    enum Notifications {
        INVITE_ASSESSOR
    }

    @Override
    public ServiceResult<AssessorInviteToSendResource> getCreatedInvite(long inviteId) {
        return getById(inviteId).andOnSuccess(invite -> {
            if (invite.getStatus() != CREATED) {
                return ServiceResult.serviceFailure(new Error(COMPETITION_INVITE_ALREADY_SENT, invite.getTarget().getName()));
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
            NotificationTarget recipient = new ExternalUserNotificationTarget(invite.getName(), invite.getEmail());
            Notification notification = new Notification(systemNotificationSource, singletonList(recipient), Notifications.INVITE_ASSESSOR,
                    asMap("name", invite.getName(),
                            "competitionName", invite.getTarget().getName(),
                            "innovationArea", invite.getInnovationArea(),
                            "acceptsDate", invite.getTarget().getAssessorAcceptsDate().format(formatter),
                            "deadlineDate", invite.getTarget().getAssessorDeadlineDate().format(formatter),
                            "inviteUrl", format("%s/invite/competition/%s", webBaseUrl + WEB_CONTEXT, invite.getHash())));
            EmailContent content = notificationSender.renderTemplates(notification).getSuccessObject().get(recipient);
            AssessorInviteToSendResource resource = toSendMapper.mapToResource(invite);
            resource.setEmailContent(content);
            return serviceSuccess(resource);
        });
    }

    @Override
    public ServiceResult<CompetitionInviteResource> getInvite(String inviteHash) {
        return getByHashIfOpen(inviteHash)
                .andOnSuccessReturn(competitionInviteMapper::mapToResource);
    }

    @Override
    public ServiceResult<CompetitionInviteResource> openInvite(String inviteHash) {
        return getByHashIfOpen(inviteHash)
                .andOnSuccessReturn(this::openInvite)
                .andOnSuccessReturn(competitionInviteMapper::mapToResource);
    }

    @Override
    public ServiceResult<Void> acceptInvite(String inviteHash, UserResource currentUser) {
        final User user = userRepository.findOne(currentUser.getId());
        return getParticipantByInviteHash(inviteHash)
                .andOnSuccess(p -> accept(p, user))
                .andOnSuccessReturnVoid();
    }

    @Override
    public ServiceResult<Void> rejectInvite(String inviteHash, RejectionReasonResource rejectionReason, Optional<String> rejectionComment) {
        return getRejectionReason(rejectionReason)
                .andOnSuccess(reason -> getParticipantByInviteHash(inviteHash)
                        .andOnSuccess(invite -> reject(invite, reason, rejectionComment)))
                .andOnSuccessReturnVoid();
    }

    @Override
    public ServiceResult<Boolean> checkExistingUser(@P("inviteHash") String inviteHash) {
        return getByHash(inviteHash).andOnSuccessReturn(invite -> {
            if (invite.getUser() != null) {
                return TRUE;
            }

            return userRepository.findByEmail(invite.getEmail()).isPresent();
        });
    }

    @Override
    public ServiceResult<List<AvailableAssessorResource>> getAvailableAssessors(long competitionId) {
        List<User> assessors = userRepository.findAllAvailableAssessorsByCompetition(competitionId);

        return serviceSuccess(assessors.stream()
                .map(assessor -> {
                    AvailableAssessorResource availableAssessor = new AvailableAssessorResource();
                    availableAssessor.setEmail(assessor.getEmail());
                    availableAssessor.setName(assessor.getName());
                    availableAssessor.setBusinessType(getBusinessType(assessor));
                    availableAssessor.setCompliant(assessor.isProfileCompliant());
                    availableAssessor.setAdded(wasInviteCreated(assessor.getEmail(), competitionId));
                    // TODO INFUND-6865 Users should have innovation areas
                    availableAssessor.setInnovationArea(null);
                    return availableAssessor;
                }).collect(toList()));
    }

    @Override
    public ServiceResult<List<AssessorCreatedInviteResource>> getCreatedInvites(long competitionId) {
        return serviceSuccess(simpleMap(competitionInviteRepository.getByCompetitionIdAndStatus(competitionId, CREATED), competitionInvite ->
                new AssessorCreatedInviteResource(competitionInvite.getName(), getInnovationAreaForInvite(competitionInvite), isUserCompliant(competitionInvite), competitionInvite.getEmail(), competitionInvite.getId())));
    }

    @Override
    public ServiceResult<List<AssessorInviteOverviewResource>> getInvitationOverview(long competitionId) {
        // TODO INFUND-6450
        return serviceSuccess(emptyList());
    }

    @Override
    public ServiceResult<CompetitionInviteResource> inviteUser(NewUserStagedInviteResource stagedInvite) {
        return getByEmailAndCompetition(stagedInvite.getEmail(), stagedInvite.getCompetitionId()).handleSuccessOrFailure(
                failure -> getCompetition(stagedInvite.getCompetitionId())
                        .andOnSuccess(competition -> getInnovationArea(stagedInvite.getInnovationCategoryId())
                                .andOnSuccess(innovationArea ->
                                        inviteUserToCompetition(
                                                stagedInvite.getName(),
                                                stagedInvite.getEmail(),
                                                competition,
                                                innovationArea
                                        )
                                )
                        )
                        .andOnSuccessReturn(competitionInviteMapper::mapToResource),
                success -> serviceFailure(Error.globalError(
                        "validation.competitionInvite.create.email.exists",
                        singletonList(stagedInvite.getEmail())
                ))
        );
    }

    @Override
    public ServiceResult<Void> inviteNewUsers(List<NewUserStagedInviteResource> newUserStagedInvites, long competitionId) {
        return getCompetition(competitionId).andOnSuccessReturn(competition ->
                mapWithIndex(newUserStagedInvites, (index, invite) ->
                        getByEmailAndCompetition(invite.getEmail(), competitionId).handleSuccessOrFailure(
                                failure -> getInnovationArea(invite.getInnovationCategoryId())
                                        .andOnSuccess(innovationArea ->
                                                inviteUserToCompetition(invite.getName(), invite.getEmail(), competition, innovationArea)
                                        )
                                        .andOnFailure(() -> serviceFailure(Error.fieldError(
                                                "invites[" + index + "].innovationArea",
                                                invite.getInnovationCategoryId(),
                                                "validation.competitionInvite.create.innovationArea.required"
                                                ))
                                        ),
                                success -> serviceFailure(Error.fieldError(
                                        "invites[" + index + "].email",
                                        invite.getEmail(),
                                        "validation.competitionInvite.create.email.exists"
                                ))
                        )
                ))
                .andOnSuccess(list -> aggregate(list))
                .andOnSuccessReturnVoid();
    }

    private BusinessType getBusinessType(User assessor) {
        return (assessor.getProfile() != null) ? assessor.getProfile().getBusinessType() : null;
    }

    private boolean wasInviteCreated(String email, long competitionId) {
        ServiceResult<CompetitionInvite> result = getByEmailAndCompetition(email, competitionId);
        return result.isSuccess() ? result.getSuccessObject().getStatus() == CREATED : FALSE;
    }

    private ServiceResult<Category> getInnovationArea(long innovationCategoryId) {
        return find(categoryRepository.findByIdAndType(innovationCategoryId, INNOVATION_AREA), notFoundError(Category.class, innovationCategoryId, INNOVATION_AREA));
    }

    private ServiceResult<CompetitionInvite> inviteUserToCompetition(String name, String email, Competition competition, Category innovationArea) {
        return serviceSuccess(
                competitionInviteRepository.save(new CompetitionInvite(name, email, generateInviteHash(), competition, innovationArea))
        );
    }

    @Override
    public ServiceResult<CompetitionInviteResource> inviteUser(ExistingUserStagedInviteResource stagedInvite) {
        return getUserByEmail(stagedInvite.getEmail()) // I'm not particularly tied to finding by email, vs id
                .andOnSuccess(user -> inviteUserToCompetition(user, stagedInvite.getCompetitionId()))
                .andOnSuccessReturn(competitionInviteMapper::mapToResource);
    }

    private ServiceResult<CompetitionInvite> inviteUserToCompetition(User user, long competitionId) {
        return getCompetition(competitionId)
                .andOnSuccessReturn(
                        competition -> competitionInviteRepository.save(new CompetitionInvite(user, generateInviteHash(), competition))
                );
    }

    private ServiceResult<Competition> getCompetition(long competitionId) {
        return find(competitionRepository.findOne(competitionId), notFoundError(Competition.class, competitionId));
    }

    private ServiceResult<User> getUserByEmail(String email) {
        return find(userRepository.findByEmail(email), notFoundError(User.class, email));
    }

    @Override
    public ServiceResult<AssessorInviteToSendResource> sendInvite(long inviteId, EmailContent content) {
        return getById(inviteId).andOnSuccessReturn(invite -> sendInvite(invite, content)).andOnSuccessReturn(toSendMapper::mapToResource);
    }

    private CompetitionInvite sendInvite(CompetitionInvite invite, EmailContent content) {
        competitionParticipantRepository.save(new CompetitionParticipant(invite.send()));

        NotificationTarget recipient = new ExternalUserNotificationTarget(invite.getName(), invite.getEmail());
        Notification notification = new Notification(systemNotificationSource, singletonList(recipient), Notifications.INVITE_ASSESSOR, emptyMap());
        notificationSender.sendEmailWithContent(notification, recipient, content);

        return invite;
    }

    @Override
    public ServiceResult<Void> deleteInvite(String email, long competitionId) {
        return getByEmailAndCompetition(email, competitionId).andOnSuccess(this::deleteInvite);
    }

    private ServiceResult<CompetitionInvite> getByHash(String inviteHash) {
        return find(competitionInviteRepository.getByHash(inviteHash), notFoundError(CompetitionInvite.class, inviteHash));
    }

    private ServiceResult<CompetitionInvite> getById(long id) {
        return find(competitionInviteRepository.findOne(id), notFoundError(CompetitionInvite.class, id));
    }

    private ServiceResult<CompetitionInvite> getByEmailAndCompetition(String email, long competitionId) {
        return find(competitionInviteRepository.getByEmailAndCompetitionId(email, competitionId), notFoundError(CompetitionInvite.class, email, competitionId));
    }

    private ServiceResult<Void> deleteInvite(CompetitionInvite invite) {
        if (invite.getStatus() != CREATED) {
            return ServiceResult.serviceFailure(new Error(COMPETITION_INVITE_CANNOT_DELETE_ONCE_SENT, invite.getEmail()));
        }

        competitionInviteRepository.delete(invite);
        return serviceSuccess();
    }

    private ServiceResult<CompetitionInvite> getByHashIfOpen(String inviteHash) {
        return getByHash(inviteHash).andOnSuccess(invite -> {

            if (!EnumSet.of(READY_TO_OPEN, IN_ASSESSMENT, CLOSED, OPEN).contains(invite.getTarget().getCompetitionStatus())) {
                return ServiceResult.serviceFailure(new Error(COMPETITION_INVITE_EXPIRED, invite.getTarget().getName()));
            }

            CompetitionParticipant participant = competitionParticipantRepository.getByInviteHash(inviteHash);

            if (participant == null) {
                return serviceSuccess(invite);
            }

            if (participant.getStatus() == ACCEPTED || participant.getStatus() == REJECTED) {
                return ServiceResult.serviceFailure(new Error(COMPETITION_INVITE_CLOSED, invite.getTarget().getName()));
            }

            return serviceSuccess(invite);
        });
    }

    private CompetitionInvite openInvite(CompetitionInvite invite) {
        return competitionInviteRepository.save(invite.open());
    }

    private ServiceResult<CompetitionParticipant> getParticipantByInviteHash(String inviteHash) {
        return find(competitionParticipantRepository.getByInviteHash(inviteHash), notFoundError(CompetitionParticipant.class, inviteHash));
    }

    private ServiceResult<CompetitionParticipant> accept(CompetitionParticipant participant, User user) {
        if (participant.getInvite().getStatus() != OPENED) {
            return ServiceResult.serviceFailure(new Error(COMPETITION_PARTICIPANT_CANNOT_ACCEPT_UNOPENED_INVITE, getInviteCompetitionName(participant)));
        } else if (participant.getStatus() == ACCEPTED) {
            return ServiceResult.serviceFailure(new Error(COMPETITION_PARTICIPANT_CANNOT_ACCEPT_ALREADY_ACCEPTED_INVITE, getInviteCompetitionName(participant)));
        } else if (participant.getStatus() == REJECTED) {
            return ServiceResult.serviceFailure(new Error(COMPETITION_PARTICIPANT_CANNOT_ACCEPT_ALREADY_REJECTED_INVITE, getInviteCompetitionName(participant)));
        } else {
            return serviceSuccess(participant.acceptAndAssignUser(user));
        }
    }

    private ServiceResult<CompetitionParticipant> reject(CompetitionParticipant participant, RejectionReason rejectionReason, Optional<String> rejectionComment) {
        if (participant.getInvite().getStatus() != OPENED) {
            return ServiceResult.serviceFailure(new Error(COMPETITION_PARTICIPANT_CANNOT_REJECT_UNOPENED_INVITE, getInviteCompetitionName(participant)));
        } else if (participant.getStatus() == ACCEPTED) {
            return ServiceResult.serviceFailure(new Error(COMPETITION_PARTICIPANT_CANNOT_REJECT_ALREADY_ACCEPTED_INVITE, getInviteCompetitionName(participant)));
        } else if (participant.getStatus() == REJECTED) {
            return ServiceResult.serviceFailure(new Error(COMPETITION_PARTICIPANT_CANNOT_REJECT_ALREADY_REJECTED_INVITE, getInviteCompetitionName(participant)));
        } else {
            return serviceSuccess(participant.reject(rejectionReason, rejectionComment));
        }
    }

    private ServiceResult<RejectionReason> getRejectionReason(final RejectionReasonResource rejectionReason) {
        return find(rejectionReasonRepository.findOne(rejectionReason.getId()), notFoundError(RejectionReason.class, rejectionReason.getId()));
    }

    private String getInviteCompetitionName(CompetitionParticipant participant) {
        return participant.getInvite().getTarget().getName();
    }

    private boolean isUserCompliant(CompetitionInvite competitionInvite) {
        return competitionInvite.getUser() != null && competitionInvite.getUser().isProfileCompliant();
    }

    private CategoryResource getInnovationAreaForInvite(CompetitionInvite competitionInvite) {
        boolean inviteForNewUser = competitionInvite.getUser() == null;
        if (inviteForNewUser) {
            return categoryMapper.mapToResource(competitionInvite.getInnovationArea());
        }
        // TODO INFUND-6865 User should have an innovation area
        return null;
    }
}
