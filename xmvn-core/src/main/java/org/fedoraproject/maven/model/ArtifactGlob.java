/*-
 * Copyright (c) 2012 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fedoraproject.maven.model;

import java.util.regex.Pattern;

import org.fedoraproject.maven.utils.GlobUtils;
import org.fedoraproject.maven.utils.StringSplitter;

public class ArtifactGlob
{
    private final Pattern groupId;

    private final Pattern artifactId;

    private final Pattern version;

    public ArtifactGlob( String glob )
    {
        String[] tok = StringSplitter.split( glob, 3, ':' );
        groupId = GlobUtils.glob2pattern( tok[0] );
        artifactId = GlobUtils.glob2pattern( tok[1] );
        version = GlobUtils.glob2pattern( tok[2] );
    }

    public boolean matches( Artifact artifact )
    {
        return ( groupId == null || groupId.matcher( artifact.getGroupId() ).matches() )
            && ( artifactId == null || artifactId.matcher( artifact.getArtifactId() ).matches() )
            && ( version == null || version.matcher( artifact.getVersion() ).matches() );
    }
}
