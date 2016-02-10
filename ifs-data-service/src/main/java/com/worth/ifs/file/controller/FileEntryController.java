package com.worth.ifs.file.controller;

import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.file.resource.FileEntryResource;
import com.worth.ifs.file.transactional.FileEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.worth.ifs.commons.rest.RestResultBuilder.newRestHandler;

@RestController
@RequestMapping("/fileentry")
public class FileEntryController {

    @Autowired
    private FileEntryService service;

    @RequestMapping("/{id}")
    public RestResult<FileEntryResource> findById(@PathVariable("id") final Long id) {
        return newRestHandler().perform(() -> service.findOne(id));
    }
}