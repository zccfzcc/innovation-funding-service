package com.worth.ifs.invite.transactional;

import com.worth.ifs.application.transactional.ApplicationService;
import com.worth.ifs.invite.constant.InviteStatusConstants;
import com.worth.ifs.invite.domain.Invite;
import com.worth.ifs.invite.repository.InviteRepository;
import com.worth.ifs.notifications.resource.*;
import com.worth.ifs.notifications.service.NotificationService;
import com.worth.ifs.transactional.BaseTransactionalService;
import com.worth.ifs.transactional.ServiceFailure;
import com.worth.ifs.transactional.ServiceResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.worth.ifs.application.transactional.ApplicationServiceImpl.Notifications.INVITE_COLLABORATOR;
import static com.worth.ifs.notifications.resource.NotificationMedium.EMAIL;
import static java.util.Collections.singletonList;

@Service
public class InviteServiceImpl extends BaseTransactionalService implements InviteService {
    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    ApplicationService applicationService;
    @Autowired
    InviteRepository inviteRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SystemNotificationSource systemNotificationSource;

    Validator validator;
    @Autowired
    private void setValidator() {
        validator = new org.springframework.validation.beanvalidation.LocalValidatorFactoryBean();
    }

    @Override
    public List<ServiceResult<Notification>> inviteCollaborators(String baseUrl, List<Invite> invites) {
        List<ServiceResult<Notification>> results = new ArrayList<>();
        invites.stream().forEach(i -> {
            Errors errors = new BeanPropertyBindingResult(i, i.getClass().getName());
            validator.validate(i, errors);

            if(errors.hasErrors()){
                errors.getFieldErrors().stream().peek(e -> log.debug(String.format("Field error: %s ", e.getField())));
                ServiceFailure inviteResult;
                inviteResult = ServiceFailure.error("Validation errors");
                ServiceResult<Notification> iR = ServiceResult.failure(inviteResult);

                results.add(iR);
                iR.mapLeftOrRight(
                        failure -> handleInviteError(i, failure),
                        success -> handleInviteSuccess(i)
                );
            }else{
                if(i.generateHash()){
                    inviteRepository.save(i);
                }

                ServiceResult<Notification> inviteResult = inviteCollaboratorToApplication(baseUrl, i);

                results.add(inviteResult);
                inviteResult.mapLeftOrRight(
                        failure -> handleInviteError(i, failure),
                        success -> handleInviteSuccess(i)
                );
            }
        });
        return results;
    }

    private boolean handleInviteSuccess(Invite i) {
        i.setStatus(InviteStatusConstants.SEND);
        inviteRepository.save(i);
        return true;
    }

    private boolean handleInviteError(Invite i, ServiceFailure failure) {
        log.error(String.format("Invite failed %s , %s (error count: %s)", i.getId(), i.getEmail(), failure.getErrors().size()));
        return true;
    }

    private String getInviteUrl(String baseUrl, Invite invite) {
        return String.format("%s/accept-invite/%s/%s", baseUrl, invite.getApplication().getId(), invite.getHash());
    }

    @Override
    public ServiceResult<Notification> inviteCollaboratorToApplication(String baseUrl, Invite invite) {
        log.warn("inviteCollaboratorToApplication");
        NotificationSource from = systemNotificationSource;
        NotificationTarget to = new ExternalUserNotificationTarget(invite.getName(), invite.getEmail());

        Map<String, Object> notificationArguments = new HashMap<>();
        notificationArguments.put("applicationName", invite.getApplication().getName());
        notificationArguments.put("inviteUrl", getInviteUrl(baseUrl, invite));
        notificationArguments.put("leadOrganisation", invite.getApplication().getLeadOrganisation().get().getName());
        notificationArguments.put("leadApplicant", invite.getApplication().getLeadApplicant().get().getName());
        notificationArguments.put("leadApplicantEmail", invite.getApplication().getLeadApplicant().get().getEmail());

        Notification notification = new Notification(from, singletonList(to), INVITE_COLLABORATOR, notificationArguments);
        log.warn(String.format("Send notification email to : %s <%s>", invite.getName(), invite.getEmail()));
        log.warn(String.format("Send notification with link : %s ", getInviteUrl(baseUrl, invite)));
        return notificationService.sendNotification(notification, EMAIL);

    }

}
