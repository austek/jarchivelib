/*
 * Copyright 2024 The Compress4J Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.compress4j.archivers;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.commons.compress.archivers.ArchiveEntry;

/**
 * Reads *nix file mode flags of commons-compress ArchiveEntry (where possible) and maps them onto Files on the file
 * system.
 */
abstract class FileModeMapper<E extends ArchiveEntry> {

    private static final Logger LOG = Logger.getLogger(FileModeMapper.class.getCanonicalName());
    private static final boolean IS_POSIX =
            FileSystems.getDefault().supportedFileAttributeViews().contains("posix");

    private final E archiveEntry;

    protected FileModeMapper(E archiveEntry) {
        this.archiveEntry = archiveEntry;
    }

    public abstract void map(File file) throws IOException;

    public E getArchiveEntry() {
        return archiveEntry;
    }

    /**
     * Utility method to create a FileModeMapper for the given entry, and use it to map the file mode onto the given
     * file.
     *
     * @param entry the archive entry that holds the mode
     * @param file the file to apply the mode onto
     */
    public static <T extends ArchiveEntry> void map(T entry, File file) throws IOException {
        create(entry).map(file);
    }

    /**
     * Factory method for creating a FileModeMapper for the given ArchiveEntry. Unknown types will yield a
     * FallbackFileModeMapper that discretely does nothing.
     *
     * @param entry the archive entry for which to create a FileModeMapper for
     * @return a new FileModeMapper instance
     */
    public static <T extends ArchiveEntry> FileModeMapper<T> create(T entry) {
        if (IS_POSIX) {
            return new PosixPermissionMapper<>(entry);
        }

        // TODO: implement basic windows permission mapping (e.g. with File.setX or attrib)
        return new FallbackFileModeMapper<>(entry);
    }

    /** Does nothing! */
    public static class FallbackFileModeMapper<F extends ArchiveEntry> extends FileModeMapper<F> {

        public FallbackFileModeMapper(F archiveEntry) {
            super(archiveEntry);
        }

        @Override
        public void map(File file) {
            // do nothing
        }
    }

    /**
     * Uses an AttributeAccessor to extract the posix file permissions from the ArchiveEntry and sets them on the given
     * file.
     */
    @SuppressWarnings("OctalInteger")
    public static class PosixPermissionMapper<P extends ArchiveEntry> extends FileModeMapper<P> {
        public static final int UNIX_PERMISSION_MASK = 0777;

        public PosixPermissionMapper(P archiveEntry) {
            super(archiveEntry);
        }

        @Override
        public void map(File file) throws IOException {
            int mode = getMode() & UNIX_PERMISSION_MASK;

            if (mode > 0) {
                setPermissions(mode, file);
            }
        }

        public int getMode() throws IOException {
            return AttributeAccessor.create(getArchiveEntry()).getMode();
        }

        private void setPermissions(int mode, File file) {
            try {
                Set<PosixFilePermission> posixFilePermissions = new PosixFilePermissionsMapper().map(mode);
                Files.setPosixFilePermissions(file.toPath(), posixFilePermissions);
            } catch (Exception e) {
                LOG.warning("Could not set file permissions of " + file + ". Exception was: " + e.getMessage());
            }
        }
    }

    @SuppressWarnings("OctalInteger")
    public static class PosixFilePermissionsMapper {

        public static final Map<Integer, PosixFilePermission> intToPosixFilePermission = Map.of(
                0400, PosixFilePermission.OWNER_READ,
                0200, PosixFilePermission.OWNER_WRITE,
                0100, PosixFilePermission.OWNER_EXECUTE,
                0040, PosixFilePermission.GROUP_READ,
                0020, PosixFilePermission.GROUP_WRITE,
                0010, PosixFilePermission.GROUP_EXECUTE,
                0004, PosixFilePermission.OTHERS_READ,
                0002, PosixFilePermission.OTHERS_WRITE,
                0001, PosixFilePermission.OTHERS_EXECUTE);

        public Set<PosixFilePermission> map(int mode) {
            return intToPosixFilePermission.entrySet().stream()
                    .filter(entry -> (mode & entry.getKey()) > 0)
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toSet());
        }
    }
}
