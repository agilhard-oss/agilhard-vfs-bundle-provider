package net.agilhard.util.vfs.provider.bundleresource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A file in a bundleresource file system.
 */
public class BundleResourceFileObject extends AbstractFileObject<BundleResourceFileSystem> {

    /** The Bundle. */
    protected Bundle bundle;

    protected String path;

    private FileType type;

    public static final String BUNDLE_PROTOCOL = "bundleresource:";

    /** The Logger. */
    private final Logger log = LoggerFactory.getLogger(BundleResourceFileObject.class);

    protected BundleResourceFileObject(final AbstractFileName name, final BundleResourceFileSystem fs)
        throws FileSystemException {
        super(name, fs);

        this.log.debug("new BundleResourceFileObject ... name=" + name + " path=" + name.getPath());

        this.setBundlePath(name.getPath());

    }

    /**
     * Sets the details for this file object.
     */
    protected void setBundlePath(final String path) {
        if (this.bundle != null) {
            return;
        }

        this.path = path;
        if (this.path == null || "".equals(this.path)) {
            this.path = "/";
        }
        if (this.path.startsWith(BUNDLE_PROTOCOL)) {
            this.path = this.path.substring(BUNDLE_PROTOCOL.length());
        }

        URL url = null;
        final List<Bundle> bundles = BundleResourceFileSystem.getBundles();
        for (final Bundle b : bundles) {
            url = b.getEntry(path);
            if (url != null) {
                this.log.debug("found first try");
                this.bundle = b;
                break;
            } else {
                url = b.getEntry(path + "/");
                if (url != null) {
                    this.log.debug("found second try");
                    this.bundle = b;
                    this.path = this.path + "/";
                    break;
                } else if (path.length() > 2) {
                    final int i = path.indexOf('/', 1);
                    if (i > 0 && path.length() > i) {
                        final String path2 = path.substring(i);
                        this.log.debug("path2=" + path2);
                        url = b.getEntry(path2);
                        if (url != null) {
                            this.bundle = b;
                            this.path = path2;
                            this.log.debug("found third try");
                        } else {
                            url = b.getEntry(path2 + "/");
                            url = b.getEntry(path2);
                            if (url != null) {
                                this.bundle = b;
                                this.path = path2 + "/";
                                this.log.debug("found third try");
                            }
                        }
                    }
                }
            }
        }
        if (url != null) {
            this.log.debug("path=" + path + " found.");

            if (this.bundle != null) {
                this.log.debug("bundle SymbolicName=" + this.bundle.getSymbolicName());
                this.log.debug("bundle url=" + url.toString());
            }

            if (url.getPath().endsWith("/")) {
                this.type = FileType.FOLDER;
            } else {
                this.type = FileType.FILE;
            }
        } else {
            this.log.debug("path=" + path + " not found.");
            this.type = FileType.IMAGINARY;
            this.bundle = null;
        }
    }

    /**
     * Determines if this file can be written to.
     *
     * @return {@code true} if this file is writeable, {@code false} if not.
     * @throws FileSystemException
     *             if an error occurs.
     */
    @Override
    public boolean isWriteable() throws FileSystemException {
        return false;
    }

    /**
     * Returns the file's type.
     */
    @Override
    protected FileType doGetType() {
        return this.type;
    }

    /**
     * Lists the children of the file.
     */
    @Override
    protected String[] doListChildren() {
        try {
            if (!this.getType().hasChildren()) {
                return null;
            }
        }
        catch (final FileSystemException e) {
            // should not happen as the type has already been set.
            throw new RuntimeException(e);
        }

        final List<String> list = new ArrayList<>();

        final Enumeration<String> en = this.bundle.getEntryPaths(this.path);
        if (en != null) {
            while (en.hasMoreElements()) {
                String entry = en.nextElement();
                if (entry.charAt(entry.length() - 1) == '/') {
                    entry = entry.substring(0, entry.length() - 2);
                }
                final int i = entry.lastIndexOf('/');
                if (i > 0) {
                    entry = entry.substring(i + 1);
                }
                this.log.debug("entry=" + entry);
                list.add(entry);
            }
            this.log.debug("vfs doListChildren size=" + list.size());
        }
        final String[] a = new String[list.size()];
        return list.toArray(a);
    }

    /**
     * Returns the size of the file content (in bytes). Is only called if {@link #doGetType} returns
     * {@link FileType#FILE}.
     */
    @Override
    protected long doGetContentSize() {
        int l = 0;
        int r = 0;
        InputStream is = null;
        try {
            is = this.doGetInputStream();
            if (is != null) {
                final byte[] buf = new byte[1024];
                while ((r = is.read(buf)) > 0) {
                    l += r;
                }
            }
        }
        catch (final Exception e) {
            return 0;
        } finally {
            if (is != null) {
                try {
                    is.close();
                }
                catch (final IOException e) {
                    this.log.debug("I/O error", e);
                }
            }
        }
        return l;
    }

    /**
     * Returns the last modified time of this file.
     */
    @Override
    protected long doGetLastModifiedTime() throws Exception {
        return this.bundle.getLastModified();
    }

    /**
     * Creates an input stream to read the file content from. Is only called if {@link #doGetType} returns
     * {@link FileType#FILE}. The input stream returned by this method is guaranteed to be closed before this method is
     * called again.
     */
    @Override
    protected InputStream doGetInputStream() throws Exception {
        if (!this.getType().hasContent()) {
            throw new FileSystemException("vfs.provider/read-not-file.error", this.getName());
        }

        final URL url = this.bundle.getResource(this.path);
        if (url == null) {
            throw new FileSystemException("resource not found", this.getName());
        }
        return url.openStream();
    }
}
