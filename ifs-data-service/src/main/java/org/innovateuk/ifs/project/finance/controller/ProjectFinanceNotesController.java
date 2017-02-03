package org.innovateuk.ifs.project.finance.controller;

import org.innovateuk.ifs.project.finance.service.ProjectFinanceNotesService;
import org.innovateuk.threads.resource.NoteResource;
import org.innovateuk.threads.resource.PostResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/project/finance/notes")
public class ProjectFinanceNotesController extends CommonThreadController<NoteResource, PostResource> {

    @Autowired
    public ProjectFinanceNotesController(ProjectFinanceNotesService service) {
        super(service);
    }
}
