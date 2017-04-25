package swiss.sib.taccession

import collection.mutable.Stack
import org.scalatest._
import scala.collection.immutable.HashMap
import com.sun.org.apache.bcel.internal.classfile.LineNumber

class TacccessionTest extends FlatSpec with Matchers {

  "Taccession" should "find one element" in {

    val config = TaccessionConfig.init("config-accession.properties");
    val patterns = TaccessionConfig.getPatterns(config)
    val keywords = TaccessionConfig.getKeywords(config)

    println("Patterns:" + patterns)
    println("Keywords:" + keywords)

    val tokens = Taccession.searchTokens(patterns, keywords, "src/test/resources/text-with-caloha-pattern-and-keyword.txt")
    val noTokens = Taccession.searchTokens(patterns, keywords, "src/test/resources/text-with-caloha-pattern-but-no-keyword.txt")

    tokens.size should be(1)
    noTokens.size should be(0)

  }

  
  it should "find the correct lines of the elements" in {

    val config = TaccessionConfig.init("config-accession.properties");
    val patterns = TaccessionConfig.getPatterns(config)
    val keywords = TaccessionConfig.getKeywords(config)

    println("Patterns:" + patterns)
    println("Keywords:" + keywords)

    val tokens = Taccession.searchTokens(patterns, keywords, "src/test/resources/text-with-nextprot-pattern.txt")

    println(tokens)
    tokens.size should be(3)
    tokens.map(_.lineNumber).distinct.size should be(2)
    tokens.filter(_.lineNumber === 2).size should be(1) //1 words one line 2
    tokens.filter(_.lineNumber === 9).size should be(2) //2 words one line 9

  }

  it should "find the correct column" in {

    val config = TaccessionConfig.init("config-accession.properties");
    val patterns = TaccessionConfig.getPatterns(config)
    val keywords = TaccessionConfig.getKeywords(config)

    println("Patterns:" + patterns)
    println("Keywords:" + keywords)

    val tokens = Taccession.searchTokens(patterns, keywords, "src/test/resources/text-with-nextprot-pattern.txt")

    println(tokens)
    val tm = tokens.filter(_.lineNumber === 2)(0);
    tm.columnNumber should be(50) 
    tm.matchedPattern should be("NX_P01308") 
    
  }

}