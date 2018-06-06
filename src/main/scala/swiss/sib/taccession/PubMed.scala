package swiss.sib.taccession


import scala.xml.pull.{XMLEvent, EvElemStart, EvText, XMLEventReader}



case class PubMed( pmid : String, synopsis : String ) {

}


object PubMed {


    def fromSource( src : scala.io.Source ) : List[ PubMed ] = {
        new XMLEventReader( src ).foldLeft( ( Nil, None ) : Tuple2[ List[ PubMed ], Option[ XMLEvent ] ] ) {
            case ( ( pms : List[ PubMed ], o_prev_event : Option[ XMLEvent ] ), event : XMLEvent ) => {
                val o_prev_tag : Option[ String ] = o_prev_event.collect { case EvElemStart( _, label, _, _ ) => label }
                event match {
                    case EvText( pmid )     if o_prev_tag.contains( "PMID" )         => {
                        ( pms :+ PubMed( pmid, "" ), Some( event ) )
                    }
                    case EvText( synopsis ) if o_prev_tag.contains( "AbstractText" ) => {
                        val pubmed : PubMed = pms.last
                        ( pms.init :+ PubMed( pubmed.pmid, synopsis ), Some( event ) )
                    }
                    case _                     => ( pms, Some( event ) )
                }
            }
        }._1
    }


}
