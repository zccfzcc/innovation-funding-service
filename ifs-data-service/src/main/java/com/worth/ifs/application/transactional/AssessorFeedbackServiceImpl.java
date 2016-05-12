package com.worth.ifs.application.transactional;

import com.worth.ifs.application.domain.Application;
import com.worth.ifs.application.domain.AssessorFeedback;
import com.worth.ifs.application.mapper.AssessorFeedbackMapper;
import com.worth.ifs.application.repository.AssessorFeedbackRepository;
import com.worth.ifs.application.resource.AssessorFeedbackResource;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.file.domain.FileEntry;
import com.worth.ifs.file.mapper.FileEntryMapper;
import com.worth.ifs.file.resource.FileEntryResource;
import com.worth.ifs.file.transactional.FileService;
import com.worth.ifs.transactional.BaseTransactionalService;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.util.function.Supplier;

import static com.worth.ifs.commons.error.CommonErrors.notFoundError;
import static com.worth.ifs.commons.service.ServiceResult.serviceFailure;
import static com.worth.ifs.commons.service.ServiceResult.serviceSuccess;
import static com.worth.ifs.util.EntityLookupCallbacks.find;

@Service
public class AssessorFeedbackServiceImpl extends BaseTransactionalService implements AssessorFeedbackService {

    @Autowired
    private AssessorFeedbackRepository repository;

    @Autowired
    private AssessorFeedbackMapper mapper;

    @Autowired
    private FileService fileService;

    @Autowired
    private FileEntryMapper fileEntryMapper;

    @Override
    public ServiceResult<AssessorFeedbackResource> findOne(Long id) {
        return find(repository.findOne(id), notFoundError(AssessorFeedback.class, id)).andOnSuccessReturn(mapper::mapToResource);
    }

    @Override
    public ServiceResult<AssessorFeedbackResource> findByAssessorId(Long assessorId) {
        return find(repository.findByAssessorId(assessorId), notFoundError(AssessorFeedback.class, assessorId)).andOnSuccessReturn(mapper::mapToResource);
    }

    @Override
    public ServiceResult<FileEntryResource> createAssessorFeedbackFileEntry(long applicationId, FileEntryResource fileEntryResource, Supplier<InputStream> inputStreamSupplier) {
        return getApplication(applicationId).
                andOnSuccess(application -> fileService.createFile(fileEntryResource, inputStreamSupplier).
                andOnSuccessReturn(fileDetails -> linkAssessorFeedbackFileToApplication(application, fileDetails)));
    }

    @Override
    public ServiceResult<FileEntryResource> updateAssessorFeedbackFileEntry(long applicationId, FileEntryResource fileEntryResource, Supplier<InputStream> inputStreamSupplier) {
        return getApplication(applicationId).
                andOnSuccess(application -> fileService.updateFile(fileEntryResource, inputStreamSupplier).
                andOnSuccessReturn(fileDetails -> linkAssessorFeedbackFileToApplication(application, fileDetails)));
    }

    @Override
    public ServiceResult<Void> deleteAssessorFeedbackFileEntry(long applicationId) {
        return getApplication(applicationId).andOnSuccess(application ->
                getAssessorFeedbackFileEntry(application).andOnSuccess(fileEntry ->
                fileService.deleteFile(fileEntry.getId()).andOnSuccessReturnVoid(() ->
                removeAssessorFeedbackFileFromApplication(application))));
    }

    private void removeAssessorFeedbackFileFromApplication(Application application) {
        application.setAssessorFeedbackFileEntry(null);
    }

    private ServiceResult<FileEntry> getAssessorFeedbackFileEntry(Application application) {
        if (application.getAssessorFeedbackFileEntry() == null) {
            return serviceFailure(notFoundError(FileEntry.class));
        } else {
            return serviceSuccess(application.getAssessorFeedbackFileEntry());
        }
    }

    private FileEntryResource linkAssessorFeedbackFileToApplication(Application application, Pair<File, FileEntry> fileDetails) {
        FileEntry fileEntry = fileDetails.getValue();
        linkAssessorFeedbackFileEntryToApplication(fileEntry, application);
        return fileEntryMapper.mapToResource(fileEntry);
    }

    private void linkAssessorFeedbackFileEntryToApplication(FileEntry fileEntry, Application application) {
        application.setAssessorFeedbackFileEntry(fileEntry);
    }
}