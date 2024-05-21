/**
 * Copyright 2013 Thomas Rausch
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.compress4j.archivers;

import java.io.IOException;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ar.ArArchiveEntry;
import org.apache.commons.compress.archivers.arj.ArjArchiveEntry;
import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;

/**
 * Adapter for accessing mode flags from the different types of ArchiveEntries.
 *
 * @param <E> the type of ArchiveEntry
 */
abstract class AttributeAccessor<E extends ArchiveEntry> {

    private final E entry;

    protected AttributeAccessor(E entry) {
        this.entry = entry;
    }

    /**
     * Returns the entry that is being accessed
     *
     * @return the entry
     */
    public E getEntry() {
        return entry;
    }

    /**
     * Returns the unix file mode.
     *
     * @return unix file mode flags
     * @throws IOException propagated I/O errors by {@code java.io}
     */
    public abstract int getMode() throws IOException;

    /**
     * Detects the type of the given ArchiveEntry and returns an appropriate AttributeAccessor for it.
     *
     * @param entry the adaptee
     * @return a new attribute accessor instance
     */
    public static AttributeAccessor<?> create(ArchiveEntry entry) {
        if (entry instanceof TarArchiveEntry tarArchiveEntry) {
            return new TarAttributeAccessor(tarArchiveEntry);
        } else if (entry instanceof ZipArchiveEntry zipArchiveEntry) {
            return new ZipAttributeAccessor(zipArchiveEntry);
        } else if (entry instanceof CpioArchiveEntry cpioArchiveEntry) {
            return new CpioAttributeAccessor(cpioArchiveEntry);
        } else if (entry instanceof ArjArchiveEntry arjArchiveEntry) {
            return new ArjAttributeAccessor(arjArchiveEntry);
        } else if (entry instanceof ArArchiveEntry arArchiveEntry) {
            return new ArAttributeAccessor(arArchiveEntry);
        }

        return new FallbackAttributeAccessor(entry);
    }

    public static class FallbackAttributeAccessor extends AttributeAccessor<ArchiveEntry> {
        protected FallbackAttributeAccessor(ArchiveEntry entry) {
            super(entry);
        }

        @Override
        public int getMode() {
            return 0;
        }
    }

    public static class TarAttributeAccessor extends AttributeAccessor<TarArchiveEntry> {
        public TarAttributeAccessor(TarArchiveEntry entry) {
            super(entry);
        }

        @Override
        public int getMode() {
            return getEntry().getMode();
        }
    }

    public static class ZipAttributeAccessor extends AttributeAccessor<ZipArchiveEntry> {
        public ZipAttributeAccessor(ZipArchiveEntry entry) {
            super(entry);
        }

        @Override
        public int getMode() {
            return getEntry().getUnixMode();
        }
    }

    public static class CpioAttributeAccessor extends AttributeAccessor<CpioArchiveEntry> {
        public CpioAttributeAccessor(CpioArchiveEntry entry) {
            super(entry);
        }

        @Override
        public int getMode() {
            return (int) getEntry().getMode();
        }
    }

    public static class ArjAttributeAccessor extends AttributeAccessor<ArjArchiveEntry> {
        public ArjAttributeAccessor(ArjArchiveEntry entry) {
            super(entry);
        }

        @Override
        public int getMode() throws IOException {
            return getEntry().getMode();
        }
    }

    public static class ArAttributeAccessor extends AttributeAccessor<ArArchiveEntry> {
        public ArAttributeAccessor(ArArchiveEntry entry) {
            super(entry);
        }

        @Override
        public int getMode() throws IOException {
            return getEntry().getMode();
        }
    }
}
