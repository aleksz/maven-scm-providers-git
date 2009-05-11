package org.apache.maven.scm.provider.git.jgit.command.checkin;

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

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.checkin.AbstractCheckInCommand;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.jgit.command.JGitUtils;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.spearce.jgit.simple.SimpleRepository;
import org.spearce.jgit.simple.StatusEntry;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 * @version $Id: JGitCheckInCommand.java $
 */
public class JGitCheckInCommand
    extends AbstractCheckInCommand
    implements GitCommand
{
    /** {@inheritDoc} */
    protected CheckInScmResult executeCheckInCommand( ScmProviderRepository repo, ScmFileSet fileSet, String message,
                                                      ScmVersion version )
        throws ScmException
    {
        GitScmProviderRepository repository = (GitScmProviderRepository) repo;

        try 
        {
            SimpleRepository srep = SimpleRepository.existing( fileSet.getBasedir() );
    
            String branch = JGitUtils.getBranchName( version );

            JGitUtils.addAllFiles( srep, fileSet );
    
            List<StatusEntry> entries = srep.status();
            
            srep.commit( null, null, message );
            srep.push( JGitUtils.getMonitor( getLogger() ), "origin", branch );
    
            List<ScmFile> checkedInFiles = new ArrayList<ScmFile>( entries.size() );
    
            // parse files to now have status 'checked_in'
            for ( StatusEntry entry : entries )
            {
                ScmFile scmfile = new ScmFile( entry.getFilePath().getPath(), JGitUtils.getScmFileStatus( entry ) );
    
                if ( fileSet.getFileList().isEmpty() )
                {
                    checkedInFiles.add( scmfile );
                }
                else
                {
                    // if a specific fileSet is given, we have to check if the file is really tracked
                    for ( Iterator<File> itfl = fileSet.getFileList().iterator(); itfl.hasNext(); )
                    {
                        File f =  itfl.next();
                        if ( f.toString().equals( scmfile.getPath() ) )
                        {
                            checkedInFiles.add( scmfile );
                        }
    
                    }
                }
            }
    
            return new CheckInScmResult( "JGit checkin", checkedInFiles );
        }
        catch ( Exception e )
        {
            throw new ScmException("JGit checkin failure!", e );
        }
    }


}