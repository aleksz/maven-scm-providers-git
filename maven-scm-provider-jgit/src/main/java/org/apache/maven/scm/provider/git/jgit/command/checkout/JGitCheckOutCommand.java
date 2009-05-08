package org.apache.maven.scm.provider.git.jgit.command.checkout;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.checkout.AbstractCheckOutCommand;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.jgit.command.JGitUtils;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.spearce.jgit.lib.ProgressMonitor;
import org.spearce.jgit.simple.LsFileEntry;
import org.spearce.jgit.simple.SimpleRepository;
import org.spearce.jgit.transport.URIish;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 * @version $Id: JGitCheckOutCommand.java $
 */
public class JGitCheckOutCommand
    extends AbstractCheckOutCommand
    implements GitCommand
{
    /**
     * For git, the given repository is a remote one.
     * We have to clone it first if the working directory does not contain a git repo yet,
     * otherwise we have to git-pull it.
     *
     * {@inheritDoc}
     */
    protected CheckOutScmResult executeCheckOutCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                        ScmVersion version, boolean recursive )
        throws ScmException
    {
        GitScmProviderRepository repository = (GitScmProviderRepository) repo;

        if ( GitScmProviderRepository.PROTOCOL_FILE.equals( repository.getProtocol() )
            && repository.getUrl().indexOf( fileSet.getBasedir().getPath() ) >= 0 )
        {
            throw new ScmException( "remote repository must not be the working directory" );
        }
        
        try {
            
            SimpleRepository srep;
            ProgressMonitor monitor = JGitUtils.getMonitor( getLogger() );
    
            String branch = JGitUtils.getBranchName( version );
            
            if ( !fileSet.getBasedir().exists() || !( new File( fileSet.getBasedir(), ".git" ).exists() ) )
            {
                if ( fileSet.getBasedir().exists() )
                {
                    // git refuses to clone otherwise
                    fileSet.getBasedir().delete();
                }
    
                
                // no git repo seems to exist, let's clone the original repo
                URIish uri = new URIish(repository.getUrl());
                srep = SimpleRepository.clone( fileSet.getBasedir(), "origin", uri, branch, monitor );
            }
            else
            {
                srep = SimpleRepository.existing( fileSet.getBasedir() );
                
                // switch branch if we currently are not on the proper one
                if ( !branch.equals( srep.getBranch() ) )
                {
                    //X TODO have to check if this really switches the branch!
                    srep.checkout( branch, monitor );
                }
                
                URIish uri = new URIish(repository.getUrl());
                srep.pull( uri, branch ); 
            }
            
            List<ScmFile> listedFiles = new ArrayList<ScmFile>();
            List<LsFileEntry> fileEntries = srep.lsFiles();
            for (LsFileEntry entry : fileEntries)
            {
                listedFiles.add( new ScmFile(entry.getFilePath(), JGitUtils.getScmFileStatus( entry.getStatus() ) ) );
            }
            
            return new CheckOutScmResult("checkout via JGit", listedFiles );
        }
        catch (Exception e)
        {
            throw new ScmException( "JGit checkout failure!", e );
        }
    }

}
