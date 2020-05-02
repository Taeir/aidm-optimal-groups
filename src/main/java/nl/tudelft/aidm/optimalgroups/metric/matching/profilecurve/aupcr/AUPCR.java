package nl.tudelft.aidm.optimalgroups.metric.matching.profilecurve.aupcr;

import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.match.Matching;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;

/**
 * TODO: Use the ProfileCurve class?
 */
public abstract class AUPCR {

    protected Matching<? extends Group, Project> matching;
    protected Projects projects;
    protected Agents students;

    protected float aupcr = -1;

    /**
     * Area Under Profile Curve Ratio (AUPCR) metric.
     * An AUPCR of 1 is perfect and an AUPCR of 0 is terrible.
     * Defined on page 8 of (Diebold & Bichler, 2016)
     */
    public AUPCR (Matching<? extends Group, Project> matching, Projects projects, Agents students) {
        this.matching = matching;
        this.projects = projects;
        this.students = students;
    }

    public float result() {
        if (this.aupcr == -1) {
            this.aupcr = ((float) this.aupc()) / this.totalArea();
        }
        return this.aupcr;
    }

    public abstract void printResult();

    protected abstract float totalArea();

    protected abstract int aupc();

}
