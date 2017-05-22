package swiss.sib.taccession

import scala.util.matching.Regex
import java.io.File

case class TConfigPattern(patternName: String, pattern: Regex, mustFindRegexInFile: Option[Regex], excludeTokens: Option[Regex])

case class TConfig(name: String, 
                  filesDirectory: File,
                  statsOutputFolder: File,
                  dataOutputFolder: File,
                  sparkPartitions: Int,
                  patterns: List[TConfigPattern])