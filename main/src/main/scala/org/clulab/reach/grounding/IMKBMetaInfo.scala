package org.clulab.reach.grounding

import org.clulab.reach.grounding.ReachKBConstants._

/**
  * Class holding meta-information for an in-memory knowledge base instance.
  *   Written by: Tom Hicks. 10/25/2015.
  *   Last Modified: Refactor.
  */
class IMKBMetaInfo (

  /** The primary URI of the external KB (e.g., http://identifiers.org/uniprot/). */
  val baseURI: String = "",

  /** The Resource Identifier for the primary resource location for this KB (e.g., MIR:00100164).
    * NB: This is MIRIAM registration ID of the external knowledge base, NOT an entity ID. */
  val resourceId: String = "",

  namespace: String = DefaultNamespace,     // default UAZ namespace
  kbFilename: Option[String] = None,        // default for KBs with no file to load
  hasSpeciesInfo: Boolean = false,          // default to KBs without species info
  isFamilyKB: Boolean = false,              // does KB contain protein family entries
  isProteinKB: Boolean = false              // does KB contain protein entries

) extends KBMetaInfo (kbFilename, namespace, hasSpeciesInfo, isFamilyKB, isProteinKB) {

  /**
    * Using the given ID string, generate a URI which references an entry
    * in an external knowledge base:
    * (e.g., given "P2345" => "http://identifiers.org/uniprot/P2345").
    */
  def referenceURI (id:String): String = s"${baseURI}${id}"

  override def toString: String =
    s"<IMKBMetaInfo: $namespace, $kbFilename, $baseURI, $resourceId, sp=$hasSpeciesInfo, f=$isFamilyKB, p=$isProteinKB>"

}
