/*-
 * Copyright (c) 2012-2013 Red Hat, Inc.
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
package org.fedoraproject.maven.repository;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

import org.fedoraproject.maven.config.ResolverSettings;
import org.fedoraproject.maven.model.Artifact;

/**
 * @author Mikolaj Izdebski
 */
@Deprecated
public class DefaultAggregatorRepository
    implements Repository
{
    private final Collection<Repository> jarRepos = new LinkedList<>();

    private final Collection<Repository> pomRepos = new LinkedList<>();

    private final File root;

    private void initRepos( Collection<Repository> repos, Iterable<String> dirs, RepositoryType layout1,
                            RepositoryType layout2 )
    {
        for ( RepositoryType layout : new RepositoryType[] { layout1, layout2 } )
        {
            for ( String repoPath : dirs )
            {
                File repoRoot = new File( root, repoPath );
                Repository repo = new SingletonRepository( repoRoot, layout );
                repos.add( repo );
            }
        }
    }

    public DefaultAggregatorRepository( File root, ResolverSettings settings )
    {
        this.root = root;

        initRepos( jarRepos, settings.getJarRepositories(), RepositoryType.JPP, RepositoryType.JPP_VERSIONLESS );
        initRepos( pomRepos, settings.getPomRepositories(), RepositoryType.FLAT, RepositoryType.FLAT_VERSIONLESS );
    }

    @Override
    public File findArtifact( Artifact artifact, boolean versioned )
    {
        Iterable<Repository> repos = artifact.isPom() ? pomRepos : jarRepos;
        for ( Repository repo : repos )
        {
            File file = repo.findArtifact( artifact, versioned );
            if ( file != null )
                return file;
        }

        return null;
    }
}