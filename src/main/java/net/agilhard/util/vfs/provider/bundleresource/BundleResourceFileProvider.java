package net.agilhard.util.vfs.provider.bundleresource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractOriginatingFileProvider;
import org.apache.commons.vfs2.provider.FileProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A file system provider for bundleresource files. Provides read-only file systems.
 */
public class BundleResourceFileProvider extends AbstractOriginatingFileProvider implements FileProvider {

    /** logger */
    final Logger log = LoggerFactory.getLogger(BundleResourceFileProvider.class);

    /** The list of capabilities this provider supports */
    protected static final Collection<Capability> capabilities =
        Collections.unmodifiableCollection(Arrays.asList(new Capability[] { Capability.GET_LAST_MODIFIED,
            Capability.GET_TYPE, Capability.LIST_CHILDREN, Capability.READ_CONTENT, Capability.URI, }));

    public BundleResourceFileProvider() {
        super();
    }

    /**
     * Creates a {@link FileSystem}.
     */
    @Override
    protected FileSystem doCreateFileSystem(final FileName name, final FileSystemOptions fileSystemOptions)
        throws FileSystemException {
        this.log.debug("doCreateFileSystem ... name=" + name.getFriendlyURI());
        this.log.debug("doCreateFileSystem ... name class=" + name.getClass().getName());

        return new BundleResourceFileSystem(name, fileSystemOptions);
    }

    @Override
    public Collection<Capability> getCapabilities() {
        return capabilities;
    }
}
