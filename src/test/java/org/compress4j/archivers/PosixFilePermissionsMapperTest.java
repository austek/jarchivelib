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

import static java.nio.file.attribute.PosixFilePermission.GROUP_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.GROUP_READ;
import static java.nio.file.attribute.PosixFilePermission.GROUP_WRITE;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_READ;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_WRITE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

@SuppressWarnings("OctalInteger")
class PosixFilePermissionsMapperTest {

    private final FileModeMapper.PosixFilePermissionsMapper posixFilePermissionsMapper =
            new FileModeMapper.PosixFilePermissionsMapper();

    private Set<PosixFilePermission> setOf(PosixFilePermission... posixFilePermissions) {
        return new HashSet<>(Arrays.asList(posixFilePermissions));
    }

    @Test
    void noPermissions() {
        assertThat(posixFilePermissionsMapper.map(0000)).isEmpty();
    }

    @Test
    void allPermissions() {
        assertThat(posixFilePermissionsMapper.map(0777)).containsAll(setOf(PosixFilePermission.values()));
    }

    @Test
    void ownerPermissions() {
        assertThat(posixFilePermissionsMapper.map(0400)).containsAll(setOf(OWNER_READ));
        assertThat(posixFilePermissionsMapper.map(0200)).containsAll(setOf(OWNER_WRITE));
        assertThat(posixFilePermissionsMapper.map(0100)).containsAll(setOf(OWNER_EXECUTE));

        assertThat(posixFilePermissionsMapper.map(0700)).containsAll(setOf(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE));
        assertThat(posixFilePermissionsMapper.map(0600)).containsAll(setOf(OWNER_WRITE, OWNER_READ));
        assertThat(posixFilePermissionsMapper.map(0500)).containsAll(setOf(OWNER_EXECUTE, OWNER_READ));
        assertThat(posixFilePermissionsMapper.map(0300)).containsAll(setOf(OWNER_EXECUTE, OWNER_WRITE));
    }

    @Test
    void groupPermissions() {
        assertThat(posixFilePermissionsMapper.map(0040)).containsAll(setOf(GROUP_READ));
        assertThat(posixFilePermissionsMapper.map(0020)).containsAll(setOf(GROUP_WRITE));
        assertThat(posixFilePermissionsMapper.map(0010)).containsAll(setOf(GROUP_EXECUTE));

        assertThat(posixFilePermissionsMapper.map(0070)).containsAll(setOf(GROUP_READ, GROUP_WRITE, GROUP_EXECUTE));
        assertThat(posixFilePermissionsMapper.map(0060)).containsAll(setOf(GROUP_WRITE, GROUP_READ));
        assertThat(posixFilePermissionsMapper.map(0050)).containsAll(setOf(GROUP_EXECUTE, GROUP_READ));
        assertThat(posixFilePermissionsMapper.map(0030)).containsAll(setOf(GROUP_EXECUTE, GROUP_WRITE));
    }

    @Test
    void othersPermissions() {
        assertThat(posixFilePermissionsMapper.map(0004)).containsAll(setOf(OTHERS_READ));
        assertThat(posixFilePermissionsMapper.map(0002)).containsAll(setOf(OTHERS_WRITE));
        assertThat(posixFilePermissionsMapper.map(0001)).containsAll(setOf(OTHERS_EXECUTE));

        assertThat(posixFilePermissionsMapper.map(0007)).containsAll(setOf(OTHERS_READ, OTHERS_WRITE, OTHERS_EXECUTE));
        assertThat(posixFilePermissionsMapper.map(0006)).containsAll(setOf(OTHERS_WRITE, OTHERS_READ));
        assertThat(posixFilePermissionsMapper.map(0005)).containsAll(setOf(OTHERS_EXECUTE, OTHERS_READ));
        assertThat(posixFilePermissionsMapper.map(0003)).containsAll(setOf(OTHERS_EXECUTE, OTHERS_WRITE));
    }

    @Test
    void permissionsSameForAll() {
        assertThat(posixFilePermissionsMapper.map(0444)).containsAll(setOf(OTHERS_READ, GROUP_READ, OWNER_READ));
        assertThat(posixFilePermissionsMapper.map(0222)).containsAll(setOf(OTHERS_WRITE, GROUP_WRITE, OWNER_WRITE));
        assertThat(posixFilePermissionsMapper.map(0111))
                .containsAll(setOf(OTHERS_EXECUTE, GROUP_EXECUTE, OWNER_EXECUTE));
    }

    @Test
    void permissionCombinations() {
        assertThat(posixFilePermissionsMapper.map(0750))
                .containsAll(setOf(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE, GROUP_READ, GROUP_EXECUTE));
        assertThat(posixFilePermissionsMapper.map(0753))
                .containsAll(setOf(
                        OWNER_READ,
                        OWNER_WRITE,
                        OWNER_EXECUTE,
                        GROUP_READ,
                        GROUP_EXECUTE,
                        OTHERS_WRITE,
                        OTHERS_EXECUTE));
        assertThat(posixFilePermissionsMapper.map(0574))
                .containsAll(setOf(OWNER_READ, OWNER_EXECUTE, GROUP_READ, GROUP_WRITE, GROUP_EXECUTE, OTHERS_READ));
        assertThat(posixFilePermissionsMapper.map(0544))
                .containsAll(setOf(OWNER_READ, OWNER_EXECUTE, GROUP_READ, OTHERS_READ));
        assertThat(posixFilePermissionsMapper.map(0055))
                .containsAll(setOf(GROUP_READ, GROUP_EXECUTE, OTHERS_READ, OTHERS_EXECUTE));
    }
}
