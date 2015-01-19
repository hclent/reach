package edu.arizona.sista.bionlp.reach.ruler

import java.io.File
import jline.console.ConsoleReader
import jline.console.history.FileHistory
import edu.arizona.sista.processors.bionlp.BioNLPProcessor

object RulerShell extends App {
  require(args.size % 2 == 0, "wrong command line args")

  val proc = new BioNLPProcessor

  def createBasicRuler: BasicRuler = {
    val entityRules = BasicRuler.readEntityRules(shell=true)
    val ruleArgIndex = args.indexOf("--rules")
    val eventRules = if (ruleArgIndex == -1) BasicRuler.readEventRules(shell=true) else BasicRuler.readFile(args(ruleArgIndex + 1))
    val rules = entityRules + "\n\n" + eventRules
    val actions = new DarpaActions
    new BasicRuler(rules, actions)
  }

  var basicRuler: BasicRuler = createBasicRuler

  val history = new FileHistory(new File(System.getProperty("user.home"), ".rulershellhistory"))
  sys addShutdownHook {
    history.flush()  // we must flush the file before exiting
  }

  val reader = new ConsoleReader
  reader.setPrompt(">>> ")
  reader.setHistory(history)

  val commands = Map(
    "%reload" -> "reload rules",
    "%help" -> "show commands",
    "%exit" -> "exit system"
  )

  val commandsForDisplay = commands.map{case (k, v) => s"\t$k\t=>\t$v"}.mkString("\n")

  println(s"\nWelcome to RulerShell!\n\nCOMMANDS:\n\n$commandsForDisplay\n")

  var running = true

  while (running) {
    reader.readLine match {
      case "%reload" =>
        println("reloading RulerShell...")
        try {
          basicRuler = createBasicRuler
        } catch {
          case e: Throwable => println(s"Error reloading RulerShell: ${e.getMessage}")
        }

      case "%exit" =>
        running = false

      case "%help" =>
        println(s"COMMANDS:\n\n$commandsForDisplay")

      case null =>
        running = false

      case text =>
        val doc = proc.annotate(text)
        val mentions = basicRuler.extractFrom(doc)
        displayMentions(mentions, doc)
    }
  }

  // manual terminal cleanup
  reader.getTerminal().restore()
  reader.shutdown()
}
