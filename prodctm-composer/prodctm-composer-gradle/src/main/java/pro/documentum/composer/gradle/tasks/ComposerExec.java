package pro.documentum.composer.gradle.tasks;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gradle.api.GradleException;
import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.TaskAction;

/**
 * @author Andrey B. Panfilov <andrey@panfilov.tel>
 */
public class ComposerExec extends JavaExec {

    public static final String COMPOSER_LOCATION = "COMPOSER_LOCATION";

    public static final String EQUINOX_PATTERN = "org.eclipse.equinox.launcher_*.jar";

    private Object _buildFile;

    private Object _workspace;

    private String _buildTarget;

    private Object _composerDirectory;

    public ComposerExec() {
        super();
    }

    @Override
    @TaskAction
    public void exec() {
        checkArgument(_buildFile != null, "buildfile is not specified");
        checkArgument(_workspace != null, "workspace is not specified");
        setMain("org.eclipse.equinox.launcher.Main");
        setClasspath(buildComposerCLassPath());
        args("-application", "org.eclipse.ant.core.antRunner");
        args("-buildfile", quote(getAbsolutePath(_buildFile)));
        args("-data", quote(getAbsolutePath(_workspace)));
        if (_buildTarget != null) {
            args(_buildTarget);
        }
        super.exec();
    }

    public void buildTarget(final String buildTarget) {
        _buildTarget = buildTarget;
    }

    public void buildFile(final Object buildFile) {
        _buildFile = buildFile;
    }

    public void composerDirectory(final Object composerDirectory) {
        _composerDirectory = composerDirectory;
    }

    public void workspace(final Object workspace) {
        _workspace = workspace;
    }

    public void define(final String key, final String value) {
        systemProperty(key, quote(value));
    }

    private File getFile(final Object value) {
        return getProject().file(value);
    }

    private String getAbsolutePath(final Object value) {
        return getFile(value).getAbsolutePath();
    }

    public void defineToFile(final String key, final Object value) {
        systemProperty(key, quote(getAbsolutePath(value)));
    }

    private FileTree buildComposerCLassPath() {
        File plugins = new File(locateComposerDirectory(), "plugins");
        checkDirectory(plugins);
        Map<String, Object> elements = new HashMap<String, Object>();
        elements.put("dir", plugins.getAbsolutePath());
        List<String> include = Collections.singletonList(EQUINOX_PATTERN);
        elements.put("include", include);
        FileTree files = getProject().fileTree(elements);
        checkArgument(!files.isEmpty(), "Unable to"
                + " locate org.eclipse.equinox.launcher in directory "
                + plugins.getAbsolutePath());
        return files;
    }

    private File locateComposerDirectory() {
        Object location = _composerDirectory;

        if (location == null) {
            location = getProject().getProperties().get(COMPOSER_LOCATION);
        }
        checkArgument(location != null, COMPOSER_LOCATION
                + " is not defined in project");
        File composerLocation = getFile(location);
        checkDirectory(composerLocation);
        return composerLocation;
    }

    private void checkDirectory(final File file) {
        checkArgument(file.exists(), "Path '" + file.getAbsolutePath()
                + "' does not exist");
        checkArgument(file.isDirectory(), "Path '" + file.getAbsolutePath()
                + "' is not a directory");
    }

    private void checkArgument(final boolean expression) {
        if (!expression) {
            throw new GradleException();
        }
    }

    private void checkArgument(final boolean expression,
            final Object errorMessage) {
        if (!expression) {
            throw new GradleException(String.valueOf(errorMessage));
        }
    }

    public static String quote(final String input) {
        if (input.contains(" ")) {
            return "\"" + input + "\"";
        } else {
            return input;
        }
    }

    public static String quote(final File input) {
        return quote(input.getAbsolutePath());
    }

}
