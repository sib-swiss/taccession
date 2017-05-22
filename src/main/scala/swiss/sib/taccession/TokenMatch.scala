package swiss.sib.taccession

//Domain class that represents a match of a pattern in a given publication
case class TokenMatch(matchedPattern: String, //The token that was matched 
                      context: String, //Text before and after the match
                      matchedPatternLength: Integer, // The length of the token
                      lineNumber: Integer, //The line in the text where the word appears
                      columnNumber: Integer, //Number of words from the beginning of the line 
                      publicationName: String, 
                      patternName: String,
                      containsMustFindKeywordInCurrentLine: Boolean,
                      containsExcludedToken: Boolean)