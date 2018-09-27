/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.agilhard.util.vfs.provider.bundleresource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A read-only file system for bundleresources.
 */
public class BundleResourceFileSystem extends AbstractFileSystem {

    /** Bundles. */
    private static List<Bundle> bundles = new ArrayList<>();

    /** logger */
    final Logger log = LoggerFactory.getLogger(BundleResourceFileSystem.class);

    /**
     * Cache doesn't need to be synchronized since it is read-only.
     */
    private final Map<FileName, FileObject> cache = new HashMap<>();

    /**
     * BundleResourceFileSystem Constructor.
     * 
     * @param rootName
     * @param fileSystemOptions
     */
    protected BundleResourceFileSystem(final FileName rootName, final FileSystemOptions fileSystemOptions) {
        super(rootName, null, fileSystemOptions);
    }

    @Override
    public void init() throws FileSystemException {
        super.init();
    }

    /**
     * Register bundle by classFromBundle.
     * 
     * @param classFromBundle
     */
    public static void registerBundle(final Class<?> classFromBundle) {
        final Bundle bundle = FrameworkUtil.getBundle(classFromBundle);

        final Logger log = LoggerFactory.getLogger(BundleResourceFileSystem.class);

        if (bundle != null) {
            log.debug("registerBundle bundle SymbolicName=" + bundle.getSymbolicName());
        } else {
            log.debug("registerBundle bundle for class=" + classFromBundle.getName() + " not found");
        }
        bundles.add(bundle);
    }

    /**
     * Release bundles.
     */
    public static void releaseBundles() {
        bundles = new ArrayList<>();
    }

    /**
     * Get Bundles.
     * 
     * @return the list of Bundles.
     */
    public static List<Bundle> getBundles() {
        return bundles;
    }

    /**
     * Create BundleResourceFileObject.
     */
    protected BundleResourceFileObject createBundleResourceFileObject(final AbstractFileName name)
        throws FileSystemException {
        return new BundleResourceFileObject(name, this);
    }

    @Override
    protected void doCloseCommunicationLink() {

    }

    /**
     * Returns the capabilities of this file system.
     */
    @Override
    protected void addCapabilities(final Collection<Capability> caps) {
        caps.addAll(BundleResourceFileProvider.capabilities);
    }

    /**
     * Creates a file object.
     */
    @Override
    protected FileObject createFile(final AbstractFileName name) throws FileSystemException {
        return new BundleResourceFileObject(name, this);
    }

    /**
     * Adds a file object to the cache.
     */
    @Override
    protected void putFileToCache(final FileObject file) {
        this.cache.put(file.getName(), file);
    }

    /**
     * Returns a cached file.
     */
    @Override
    protected FileObject getFileFromCache(final FileName name) {
        return this.cache.get(name);
    }

    /**
     * remove a cached file.
     */
    @Override
    protected void removeFileFromCache(final FileName name) {
        this.cache.remove(name);
    }

    /**
     * will be called after all file-objects closed their streams. protected
     * void notifyAllStreamsClosed() { closeCommunicationLink(); }
     */
}
