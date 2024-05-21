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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class AbstractArchiverTest extends AbstractResourceTest {

    private Archiver archiver;

    private File archive;

    protected static void assertExtractionWasSuccessful() throws Exception {
        assertDirectoryStructureEquals(ARCHIVE_DIR, ARCHIVE_EXTRACT_DIR);
        assertFilesEquals(ARCHIVE_DIR, ARCHIVE_EXTRACT_DIR);
    }

    @BeforeEach
    public void setUp() {
        archiver = getArchiver();
        archive = getArchive();
    }

    @AfterEach
    public void tearDown() {
        archiver = null;
        archive = null;
    }

    protected abstract Archiver getArchiver();

    protected abstract File getArchive();

    @Test
    void extract_properlyExtractsArchive() throws Exception {
        archiver.extract(archive, ARCHIVE_EXTRACT_DIR);

        assertExtractionWasSuccessful();
    }

    @Test
    void extract_properlyExtractsArchiveStream() throws Exception {
        try (InputStream archiveAsStream = new FileInputStream(archive)) {
            archiver.extract(archiveAsStream, ARCHIVE_EXTRACT_DIR);
            assertExtractionWasSuccessful();
        } catch (Exception e) {
            throw e;
        }
    }

    @Test
    void create_recursiveDirectory_withFileExtension_properlyCreatesArchive() throws Exception {
        String archiveName = archive.getName();

        File createdArchive = archiver.create(archiveName, ARCHIVE_CREATE_DIR, ARCHIVE_DIR);

        assertThat(createdArchive).exists().hasName(archiveName);

        archiver.extract(createdArchive, ARCHIVE_EXTRACT_DIR);
        assertExtractionWasSuccessful();
    }

    @Test
    void create_multipleSourceFiles_properlyCreatesArchive() throws Exception {
        String archiveName = archive.getName();

        File createdArchive = archiver.create(archiveName, ARCHIVE_CREATE_DIR, ARCHIVE_DIR.listFiles());

        assertThat(createdArchive).exists().hasName(archiveName);

        archiver.extract(createdArchive, ARCHIVE_EXTRACT_DIR);
        assertDirectoryStructureEquals(ARCHIVE_DIR, ARCHIVE_EXTRACT_DIR);
    }

    @Test
    void create_recursiveDirectory_withoutFileExtension_properlyCreatesArchive() throws Exception {
        String archiveName = archive.getName();

        File archive = archiver.create("archive", ARCHIVE_CREATE_DIR, ARCHIVE_DIR);

        assertThat(archive).exists().hasName(archiveName);

        archiver.extract(archive, ARCHIVE_EXTRACT_DIR);
        assertExtractionWasSuccessful();
    }

    @Test
    void create_withNonExistingSource_fails() {
        assertThrows(
                FileNotFoundException.class, () -> archiver.create("archive", ARCHIVE_CREATE_DIR, NON_EXISTING_FILE));
    }

    @Test
    void create_withNonReadableSource_fails() {
        assertThrows(
                FileNotFoundException.class, () -> archiver.create("archive", ARCHIVE_CREATE_DIR, NON_READABLE_FILE));
    }

    @Test
    void create_withFileAsDestination_fails() {
        assertThrows(IllegalArgumentException.class, () -> archiver.create("archive", NON_READABLE_FILE, ARCHIVE_DIR));
    }

    @Test
    void create_withNonWritableDestination_fails() {
        assertThrows(IllegalArgumentException.class, () -> archiver.create("archive", NON_WRITABLE_DIR, ARCHIVE_DIR));
    }

    @Test
    void extract_withNonExistingSource_fails() {
        assertThrows(FileNotFoundException.class, () -> archiver.extract(NON_EXISTING_FILE, ARCHIVE_EXTRACT_DIR));
    }

    @Test
    void extract_withNonReadableSource_fails() {
        assertThrows(IllegalArgumentException.class, () -> archiver.extract(NON_READABLE_FILE, ARCHIVE_EXTRACT_DIR));
    }

    @Test
    void extract_withFileAsDestination_fails() {
        assertThrows(IllegalArgumentException.class, () -> archiver.extract(archive, NON_READABLE_FILE));
    }

    @Test
    void extract_withNonWritableDestination_fails() {
        assertThrows(IllegalArgumentException.class, () -> archiver.extract(archive, NON_WRITABLE_DIR));
    }

    @Test
    void stream_returnsCorrectEntries() throws IOException {
        try (ArchiveStream stream = archiver.stream(archive)) {
            ArchiveEntry entry;
            List<String> entries = new ArrayList<>();

            while ((entry = stream.getNextEntry()) != null) {
                entries.add(entry.getName().replaceAll("/$", "")); // remove trailing slashes for test compatibility
            }

            assertThat(entries)
                    .hasSize(12)
                    .contains("file.txt")
                    .contains("file.txt")
                    .contains(
                            "looooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooong_filename.txt")
                    .contains("folder")
                    .contains("folder/folder_file.txt")
                    .contains("folder/subfolder/subfolder_file.txt")
                    .contains("folder/subfolder")
                    .contains("permissions")
                    .contains("permissions/executable_file.txt")
                    .contains("permissions/private_executable_file.txt")
                    .contains("permissions/readonly_file.txt")
                    .contains("permissions/private_folder")
                    .contains("permissions/private_folder/private_file.txt");
        }
    }

    @Test
    void entry_isDirectory_behavesCorrectly() throws Exception {
        ArchiveStream stream = null;
        try {
            stream = archiver.stream(archive);
            ArchiveEntry entry;

            while ((entry = stream.getNextEntry()) != null) {
                String name = entry.getName().replaceAll("/$", ""); // remove trailing slashes for test compatibility

                if (name.endsWith("folder")
                        || name.endsWith("subfolder")
                        || name.endsWith("permissions")
                        || name.endsWith("private_folder")) {
                    assertThat(entry.isDirectory())
                            .withFailMessage("<%s> is a directory", entry.getName())
                            .isTrue();
                } else {
                    assertThat(entry.isDirectory())
                            .withFailMessage("<%s> is not a directory", entry.getName())
                            .isFalse();
                }
            }
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    @Test
    void entry_geSize_behavesCorrectly() throws Exception {
        ArchiveStream stream = null;
        try {
            stream = archiver.stream(archive);
            ArchiveEntry entry;

            while ((entry = stream.getNextEntry()) != null) {
                String name = entry.getName().replaceAll("/$", ""); // remove trailing slashes for test compatibility

                if (name.endsWith("folder")
                        || name.endsWith("subfolder")
                        || name.endsWith("permissions")
                        || name.endsWith("private_folder")) {
                    assertThat(entry.getSize()).isZero();
                } else {
                    assertThat(entry.getSize()).isNotZero();
                }
            }
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    @Test
    void entry_getLastModifiedDate_behavesCorrectly() throws Exception {
        ArchiveStream stream = null;
        try {
            stream = archiver.stream(archive);
            ArchiveEntry entry;

            while ((entry = stream.getNextEntry()) != null) {
                assertThat(entry.getLastModifiedDate()).isNotNull();
                assertThat(entry.getLastModifiedDate())
                        .withFailMessage("modification date should be before now")
                        .isBefore(new Date());
            }
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    @Test
    void stream_extractEveryEntryWorks() throws Exception {
        ArchiveStream stream = null;
        try {
            stream = archiver.stream(archive);
            ArchiveEntry entry;
            while ((entry = stream.getNextEntry()) != null) {
                entry.extract(ARCHIVE_EXTRACT_DIR);
            }
        } finally {
            IOUtils.closeQuietly(stream);
        }

        assertExtractionWasSuccessful();
    }

    @Test
    void stream_extractPassedEntry_throwsException() throws Exception {
        try (ArchiveStream stream = archiver.stream(archive)) {
            ArchiveEntry entry = null;
            try {
                entry = stream.getNextEntry();
                stream.getNextEntry();
            } catch (IllegalStateException e) {
                fail("Illegal state exception caught to early");
            }

            ArchiveEntry finalEntry = entry;
            assertThrows(IllegalStateException.class, () -> finalEntry.extract(ARCHIVE_EXTRACT_DIR));
        }
    }

    @Test
    void stream_extractOnClosedStream_throwsException() throws Exception {
        ArchiveEntry entry = null;
        try (ArchiveStream stream = archiver.stream(archive)) {
            entry = stream.getNextEntry();
        } catch (IllegalStateException e) {
            fail("Illegal state exception caught too early");
        }

        ArchiveEntry finalEntry = entry;
        assertThrows(IllegalStateException.class, () -> finalEntry.extract(ARCHIVE_EXTRACT_DIR));
    }
}
