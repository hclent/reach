package edu.arizona.sista.reach.context

import edu.arizona.sista.reach.mentions._
import edu.arizona.sista.reach.nxml.FriesEntry

// Policy Two
class BoundedPaddingContext(vocabulary:Map[(String, String), Int],
 lines:Seq[(Seq[BioMention], FriesEntry)],
 bound:Int = 5 // Default bound to extend the policy
) extends Context(vocabulary, lines){

  private def contextTypes = Seq("Species", "Organ", "CellType", "CellLine")

  // TODO: Do something smart to resolve ties
  protected def untie(entities:Seq[(String, String)]) = entities.head

  protected def padContext(prevStep:Seq[Int], remainingSteps:List[Seq[Int]], repetitions:Seq[Int], bound:Int):List[Seq[Int]] = {

    remainingSteps match {

      case head::tail =>
        // Group the prev step inferred row and the current by context type, then recurse
        val prevContext = prevStep map (this.inverseVocabulary(_)) groupBy (_._1)
        val currentContext = head map (this.inverseVocabulary(_)) groupBy (_._1)

        // Apply the heuristic
        // Inferred context of type "x"
        val newRepetitions = new Array[Int](repetitions.size)

        val currentStep = contextTypes.flatMap{ // Do this for each type of context. Flat Map as there could be more than one context of a type (maybe)
          contextType =>
            val stepIx = this.contextTypes.indexOf(contextType)

            if(repetitions(stepIx) < bound){
              (prevContext.lift(contextType), currentContext.lift(contextType)) match {
                // No prev, Current
                case (None, Some(curr)) =>
                  newRepetitions(stepIx) = 1
                  Seq(untie(curr))
                // Prev, No current
                case (Some(prev), None) =>
                  newRepetitions(stepIx) = repetitions(stepIx)+1
                  Seq(prev.head)
                // Prev, Current
                case (Some(prev), Some(curr)) =>
                  newRepetitions(stepIx) = 1
                  Seq(untie(curr))
                // No prev, No current
                case (None, None) =>
                  newRepetitions(stepIx) = 1
                  Nil
              }
            }
            else{
              newRepetitions(stepIx) = 1
              currentContext.lift(contextType) match {
                case Some(curr) =>
                  Seq(untie(curr))
                case None =>
                  Seq()
              }
            }

        } map (this.vocabulary(_))

        // Recurse
        currentStep :: padContext(currentStep, tail, newRepetitions, bound)


      case Nil => Nil
    }
  }
  // Apply the policy
  protected override def inferContext = padContext(Seq(), latentSparseMatrix, Seq.fill(this.contextTypes.size)(1), bound)

  protected override def extractEntryFeatures(entry:FriesEntry):Array[(String, Double)] = Array()
}

// Policy 1
class PaddingContext(vocabulary:Map[(String, String), Int], lines:Seq[(Seq[BioMention], FriesEntry)]) extends BoundedPaddingContext(vocabulary, lines, lines.size){

}

// Policy 3
class FillingPolicy(vocabulary:Map[(String, String), Int],
 lines:Seq[(Seq[BioMention], FriesEntry)],
  bound:Int = 5) extends BoundedPaddingContext(vocabulary, lines, bound){

    // Get the most common mentioned context of each type
    val defaultContexts = this.mentions.flatten
}
