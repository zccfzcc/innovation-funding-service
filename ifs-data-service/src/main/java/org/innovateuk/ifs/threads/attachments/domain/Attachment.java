package org.innovateuk.ifs.threads.attachments.domain;

import org.innovateuk.ifs.file.domain.FileEntry;
import org.innovateuk.ifs.user.domain.User;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
public class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @NotNull
    private User uploader;

    @OneToOne
    @Column(nullable = false, unique = true)
    private FileEntry fileEntry;

    @CreatedDate
    private LocalDateTime createdOn;

    public Attachment() {}

    public Attachment(User uploader, FileEntry fileEntry) {
        this(null, uploader, fileEntry);
    }

    public Attachment(Long id, User uploader, FileEntry fileEntry) {
        this.id = id;
        this.uploader = uploader;
        this.fileEntry = fileEntry;
    }

    public Long id() {
        return id;
    }

    public User uploader() {
        return uploader;
    }

    public FileEntry fileEntry() {
        return fileEntry;
    }

    public boolean isFile(Long fileId) {
        return this.fileEntry.getId().equals(fileId);
    }

    public boolean wasUploadedBy(Long userId) {
        return uploader.hasId(userId);
    }
}
