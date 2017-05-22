package swiss.sib.taccession

import collection.mutable.Stack
import org.scalatest._
import scala.collection.immutable.HashMap
import com.sun.org.apache.bcel.internal.classfile.LineNumber

class TacccessionTest extends FlatSpec with Matchers {

  "Taccession" should "find one element" in {

    val config = TaccessionUtils.readConfigFile("accession-config.yaml");

    val tokens = Taccession.searchTokens(config.patterns, "src/test/resources/text-with-orthodb-pattern-and-keyword.txt")
    val noTokens = Taccession.searchTokens(config.patterns, "src/test/resources/text-with-orthodb-pattern-but-no-keyword.txt")
    val excludedTokens = Taccession.searchTokens(config.patterns, "src/test/resources/text-with-orthodb-pattern-and-keyword-but-excluded-pattern.txt")

    tokens.size should be(1)
    noTokens.size should be(0)
    excludedTokens.size should be(0)

  }

  it should "find enzyme accessions" in {

    val config = TaccessionUtils.readConfigFile("accession-config.yaml");
    val tokens = Taccession.searchTokens(config.patterns, "src/test/resources/text-with-enzyme-accessions.txt")
    println(tokens)
    tokens.size should be(5)

    tokens.filter(t => t.matchedPattern.equals("1.2.-.-")).size should be (2)
    tokens.filter(t => t.matchedPattern.equals("1.2.2.-")).size should be (1)
  }

  it should "find the correct lines of the elements" in {

    val config = TaccessionUtils.readConfigFile("accession-config.yaml");

    val tokens = Taccession.searchTokens(config.patterns, "src/test/resources/text-with-nextprot-pattern.txt")

    println(tokens)
    tokens.size should be(3)
    tokens.map(_.lineNumber).distinct.size should be(2)
    tokens.filter(_.lineNumber === 2).size should be(1) //1 words one line 2
    tokens.filter(_.lineNumber === 9).size should be(2) //2 words one line 9

  }

  it should "find the correct column" in {

    val config = TaccessionUtils.readConfigFile("accession-config.yaml");
    val tokens = Taccession.searchTokens(config.patterns, "src/test/resources/text-with-nextprot-pattern.txt")

    println(tokens)
    val tm = tokens.filter(_.lineNumber === 2)(0);
    tm.columnNumber should be(50)
    tm.matchedPattern should be("NX_P01308")

  }

  it should "read the config" in {

    val config = TaccessionUtils.readConfigFile("accession-config.yaml");

  }

}