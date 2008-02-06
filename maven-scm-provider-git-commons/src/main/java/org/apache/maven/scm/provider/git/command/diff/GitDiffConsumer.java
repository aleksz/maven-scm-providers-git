import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
	// diff --git a/readme.txt b/readme.txt
	// index fea1611..9e131cf 100644
	// --- a/readme.txt
	// +++ b/readme.txt
	// @@ -1 +1 @@
	// -/readme.txt
	// \ No newline at end of file
	// +new version of /readme.txt


	/**
	 * patern matches the index line of the diff comparison
	 * paren.1 matches the first file
	 * paren.2 matches the 2nd file
	 */
    private final static String DIFF_FILES_PATTERN = "^diff --git\\sa/(.*)\\sb/(.*)";
    private final static String INDEX_LINE_TOKEN = "index ";

    private final static String NEW_FILE_MODE_TOKEN = "new file mode ";
    
    private final static String DELETED_FILE_MODE_TOKEN = "deleted file mode ";
       
    /**
     * @see #DIFF_FILES_PATTERN
     */
    private RE filesRegexp;

        try
        {
        	filesRegexp = new RE( DIFF_FILES_PATTERN );
        }
        catch ( RESyntaxException ex )
        {
            throw new RuntimeException(
                "INTERNAL ERROR: Could not create regexp to parse git log file. This shouldn't happen. Something is probably wrong with the oro installation.",
                ex );
        }        

        if ( filesRegexp.match(line) )
            currentFile = filesRegexp.getParen(1);
        else if ( line.startsWith( INDEX_LINE_TOKEN ) )
        {
            // skip, though could parse to verify start revision and end revision
            patch.append( line ).append( "\n" );
        }
        else if ( line.startsWith( NEW_FILE_MODE_TOKEN ) || 
        		  line.startsWith( DELETED_FILE_MODE_TOKEN ))
            // skip, though could parse to verify file mode