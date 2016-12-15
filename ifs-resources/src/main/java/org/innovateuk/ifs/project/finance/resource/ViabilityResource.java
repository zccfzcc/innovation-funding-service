package org.innovateuk.ifs.project.finance.resource;

/**
 * Resource to hold the Viability details
 */
public class ViabilityResource {

    private Viability viability;
    private ViabilityStatus viabilityStatus;

    // for JSON marshalling
    ViabilityResource() {
    }

    public ViabilityResource(Viability viability, ViabilityStatus viabilityStatus) {
        this.viability = viability;
        this.viabilityStatus = viabilityStatus;
    }

    public Viability getViability() {
        return viability;
    }

    public void setViability(Viability viability) {
        this.viability = viability;
    }

    public ViabilityStatus getViabilityStatus() {
        return viabilityStatus;
    }

    public void setViabilityStatus(ViabilityStatus viabilityStatus) {
        this.viabilityStatus = viabilityStatus;
    }
}
