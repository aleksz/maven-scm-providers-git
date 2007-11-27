package org.apache.maven.scm.provider.git.gitexe.command.checkin;

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
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.checkin.AbstractCheckInCommand;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.apache.maven.scm.provider.git.gitexe.command.GitCommandLineUtils;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public class GitCheckInCommand extends AbstractCheckInCommand implements GitCommand
{
    protected CheckInScmResult executeCheckInCommand( ScmProviderRepository repo, ScmFileSet fileSet, String message,
                                                      ScmVersion version )
        throws ScmException
    {
    	GitScmProviderRepository repository = (GitScmProviderRepository) repo;

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();
        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();
        int exitCode;

        File messageFile = FileUtils.createTempFile( "maven-scm-", ".commit", null );
        try
        {
            FileUtils.fileWrite( messageFile.getAbsolutePath(), message );
        }
        catch ( IOException ex )
        {
            return new CheckInScmResult( null, "Error while making a temporary file for the commit message: " +
                ex.getMessage(), null, false );
        }

        try
        {
        	Commandline clAdd = createAddCommandLine(repository, fileSet);
        	
            exitCode = GitCommandLineUtils.execute( clAdd, stdout, stderr, getLogger() );
	        if ( exitCode != 0 )
	        {
	            return new CheckInScmResult( clAdd.toString(), "The git command failed.", stderr.getOutput(), false );
	        }

        	Commandline clCommit = createCommitCommandLine(repository, fileSet, messageFile);
        	
            exitCode = GitCommandLineUtils.execute( clCommit, stdout, stderr, getLogger() );
	        if ( exitCode != 0 )
	        {
	            return new CheckInScmResult( clCommit.toString(), "The git command failed.", stderr.getOutput(), false );
	        }
	        
	        Commandline cl = createPushCommandLine( repository, fileSet, version );
	
	        GitCheckInConsumer consumer = new GitCheckInConsumer( getLogger(), fileSet.getBasedir() );
	
            exitCode = GitCommandLineUtils.execute( cl, consumer, stderr, getLogger() );
	        if ( exitCode != 0 )
	        {
	            return new CheckInScmResult( cl.toString(), "The git command failed.", stderr.getOutput(), false );
	        }

	        return new CheckInScmResult( cl.toString(), consumer.getCheckedInFiles() );
        }
        finally
        {
            try
            {
                FileUtils.forceDelete( messageFile );
            }
            catch ( IOException ex )
            {
                // ignore
            }
        }

    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static Commandline createPushCommandLine( GitScmProviderRepository repository, ScmFileSet fileSet
    		                                       , ScmVersion version )
        throws ScmException
    {
        Commandline cl = GitCommandLineUtils.getBaseGitCommandLine( fileSet.getBasedir(), "push");

        //X TODO handle version
        
        GitCommandLineUtils.addTarget( cl, fileSet.getFileList() );

        return cl;
    }
    
    public static Commandline createCommitCommandLine( GitScmProviderRepository repository, ScmFileSet fileSet,
                                                       File messageFile )
	throws ScmException
	{
		Commandline cl = GitCommandLineUtils.getBaseGitCommandLine( fileSet.getBasedir(), "push");

		cl.createArgument().setValue( "-F" );
		cl.createArgument().setValue( messageFile.getAbsolutePath() );
		GitCommandLineUtils.addTarget( cl, fileSet.getFileList() );
		
		return cl;
	}

    public static Commandline createAddCommandLine( GitScmProviderRepository repository, ScmFileSet fileSet )
	throws ScmException
	{
		Commandline cl = GitCommandLineUtils.getBaseGitCommandLine( fileSet.getBasedir(), "add");
		
		GitCommandLineUtils.addTarget( cl, fileSet.getFileList() );

		return cl;
	}

}