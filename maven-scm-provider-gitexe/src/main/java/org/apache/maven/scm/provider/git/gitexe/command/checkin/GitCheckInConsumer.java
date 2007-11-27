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

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.git.gitexe.command.AbstractFileCheckingConsumer;

import java.io.File;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: GitCheckInConsumer.java 538861 2007-05-17 10:08:06Z evenisse $
 */
public class GitCheckInConsumer
    extends AbstractFileCheckingConsumer
{
    private final static String SENDING_TOKEN = "Sending        ";

    private final static String ADDING_TOKEN = "Adding         ";

    private final static String ADDING_BIN_TOKEN = "Adding  (bin)  ";

    private final static String DELETING_TOKEN = "Deleting       ";

    private final static String TRANSMITTING_TOKEN = "Transmitting file data";

    private final static String COMMITTED_REVISION_TOKEN = "Committed revision";

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public GitCheckInConsumer( ScmLogger logger, File workingDirectory )
    {
        super( logger, workingDirectory );
    }

    // ----------------------------------------------------------------------
    // StreamConsumer Implementation
    // ----------------------------------------------------------------------

    protected void parseLine( String line )
    {
        String file;

        if ( line.startsWith( COMMITTED_REVISION_TOKEN ) )
        {
            String revisionString = line.substring( COMMITTED_REVISION_TOKEN.length() + 1, line.length() - 1 );

            revision = parseInt( revisionString );

            return;
        }
        else if ( line.startsWith( SENDING_TOKEN ) )
        {
            file = line.substring( SENDING_TOKEN.length() );
        }
        else if ( line.startsWith( ADDING_TOKEN ) )
        {
            file = line.substring( ADDING_TOKEN.length() );
        }
        else if ( line.startsWith( ADDING_BIN_TOKEN ) )
        {
            file = line.substring( ADDING_BIN_TOKEN.length() );
        }
        else if ( line.startsWith( DELETING_TOKEN ) )
        {
            file = line.substring( DELETING_TOKEN.length() );
        }
        else if ( line.startsWith( TRANSMITTING_TOKEN ) )
        {
            // ignore
            return;
        }
        else
        {
            logger.info( "Unknown line: '" + line + "'" );

            return;
        }

        addFile( new ScmFile( file, ScmFileStatus.CHECKED_IN ) );
    }

    public List getCheckedInFiles()
    {
        return getFiles();
    }
}