package pro.documentum.composer.tasks.filters;

import com.emc.ide.artifactmanager.artifact.IDmArtifact;

import pro.documentum.composer.tasks.Artifact;

/**
 * @author Andrey B. Panfilov <andrey@panfilov.tel>
 */
public class FilterArtifactByName extends AbstractDmArtifactFilter {

    public FilterArtifactByName(final Artifact option) {
        super(option);
    }

    @Override
    protected boolean doAcceptArtifact(final IDmArtifact artifact) {
        return nameMatches(artifact);
    }

}
