package swiss.sib.taccession

object SIBPatterns {

  //Pattern function based on what was found in idenfitiers.org
  //Trembl?, Glyco ? Sugar ?
  //WRONG regex in identifiers.org !!!!!!
  
  val uniprotPattern = "[OPQ][0-9][A-Z0-9]{3}[0-9]|[A-NR-Z][0-9]([A-Z][A-Z0-9]{2}[0-9]){1,2}".r
  val uniprotFullPattern = "^" + uniprotPattern + "$".r

  //Looks ok, but lots of false positives that should be filtered out

  val stringPattern = "^([A-N,R-Z][0-9][A-Z][A-Z, 0-9][A-Z, 0-9][0-9])|([O,P,Q][0-9][A-Z, 0-9][A-Z, 0-9][A-Z, 0-9][0-9])|([0-9][A-Za-z0-9]{3})$".r
  //Returns dates like 2008, 2009 ....

  val swissLipidPattern = "^SLM:d+$".r
  val bgeeGenePattern = "^(ENS|FBgn)w+$".r
  val bgeeOrganPattern = "^(XAO|ZFA|EHDAA|EMAPA|EV|MA):d+$".r
  val bgeeStagePattern = "^(FBvd|XtroDO|HsapDO|MmusDO):d+$".r
  val bgeeFamilyPattern = "^(ENSFM|ENSGTV:)d+$"
  val neXtProtPattern = "^NX_w+".r

  //val cellosaurusCVCL = "^wCVCL+$".r

  //Cellosorus / CALOHA ? SwissRegulon
  //Too generic?
  val epdPattern = "^[A-Z-_0-9]+$".r
  val swissModelPattern = "^w+$".r

}