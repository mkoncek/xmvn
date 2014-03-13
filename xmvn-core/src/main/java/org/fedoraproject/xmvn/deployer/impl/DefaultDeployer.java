/*-
 * Copyright (c) 2013-2014 Red Hat, Inc.
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
package org.fedoraproject.xmvn.deployer.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.MXSerializer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.codehaus.plexus.util.xml.pull.XmlSerializer;

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.deployer.Deployer;
import org.fedoraproject.xmvn.deployer.DeploymentRequest;
import org.fedoraproject.xmvn.deployer.DeploymentResult;
import org.fedoraproject.xmvn.utils.ArtifactUtils;

/**
 * Default implementation of XMvn {@code Deployer} interface.
 * <p>
 * <strong>WARNING</strong>: This class is part of internal implementation of XMvn and it is marked as public only for
 * technical reasons. This class is not part of XMvn API. Client code using XMvn should <strong>not</strong> reference
 * it directly.
 * 
 * @author Mikolaj Izdebski
 */
@Named
@Singleton
public class DefaultDeployer
    implements Deployer
{
    @Override
    public DeploymentResult deploy( DeploymentRequest request )
    {
        DefaultDeploymentResult result = new DefaultDeploymentResult();

        try
        {
            Xpp3Dom dom = readInstallationPlan();
            addArtifact( dom, request.getArtifact() );
            writeInstallationPlan( dom );
        }
        catch ( Exception e )
        {
            result.setException( e );
        }

        return result;
    }

    private Xpp3Dom readInstallationPlan()
        throws IOException
    {
        try (Reader reader = Files.newBufferedReader( Paths.get( ".xmvn-reactor" ), StandardCharsets.US_ASCII ))
        {
            return Xpp3DomBuilder.build( reader );
        }
        catch ( FileNotFoundException e )
        {
            return new Xpp3Dom( "reactorInstallationPlan" );
        }
        catch ( XmlPullParserException e )
        {
            throw new IOException( "Failed to parse existing reactor installation plan", e );
        }
    }

    private void addChild( Xpp3Dom parent, String tag, Object value )
    {
        if ( value != null )
        {
            String stringValue = value.toString();
            if ( stringValue.length() > 0 )
            {
                Xpp3Dom child = new Xpp3Dom( tag );
                child.setValue( stringValue );
                parent.addChild( child );
            }
        }
    }

    private void addArtifact( Xpp3Dom parent, Artifact artifact )
    {
        Xpp3Dom child = ArtifactUtils.toXpp3Dom( artifact, "artifact" );

        addChild( child, "file", artifact.getPath() );
        addChild( child, "stereotype", artifact.getStereotype() );

        parent.addChild( child );
    }

    private void writeInstallationPlan( Xpp3Dom dom )
        throws IOException
    {
        try (Writer writer = Files.newBufferedWriter( Paths.get( ".xmvn-reactor" ), StandardCharsets.US_ASCII ))
        {
            XmlSerializer s = new MXSerializer();
            s.setProperty( "http://xmlpull.org/v1/doc/properties.html#serializer-indentation", "  " );
            s.setProperty( "http://xmlpull.org/v1/doc/properties.html#serializer-line-separator", "\n" );
            s.setOutput( writer );
            s.startDocument( "US-ASCII", null );
            s.comment( " Reactor installation plan generated by XMvn " );
            s.text( "\n" );

            dom.writeToSerializer( null, s );

            s.endDocument();
        }
    }
}
