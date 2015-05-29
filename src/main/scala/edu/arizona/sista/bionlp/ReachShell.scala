package edu.arizona.sista.bionlp

import java.io.File
import jline.console.ConsoleReader
import jline.console.history.FileHistory
import edu.arizona.sista.bionlp.display._
import RuleReader._

object ReachShell extends App {

  println("Loading ReachSystem ...")
  var reach = new ReachSystem
  val proc = reach.processor

  val history = new FileHistory(new File(System.getProperty("user.home"), ".reachshellhistory"))
  sys addShutdownHook {
    history.flush() // we must flush the file before exiting
  }

  val reader = new ConsoleReader
  reader.setPrompt(">>> ")
  reader.setHistory(history)

  val commands = Map(
    ":help" -> "show commands",
    ":exit" -> "exit system",
    ":entityrules" -> "show entity rules",
    ":modrules" -> "show modification rules",
    ":eventrules" -> "show event rules",
    ":reload" -> "reload rules"
  )

  println("\nWelcome to ReachShell!")
  printCommands()

  var running = true

  while (running) {
    reader.readLine match {
      case ":help" =>
        printCommands()

      case ":exit" | null =>
        running = false

      case ":reload" =>
        println("reloading rules ...")
        try {
          val rules = reloadRules()
          reach = new ReachSystem(Some(rules), Some(proc))
          println("successfully reloaded rules")
        } catch {
          case e: Exception => println(s"error reloading: ${e.getMessage}")
        }

      case ":entityrules" =>
        println(s"${reach.entityRules}\n")
      case ":modrules" =>
        println(s"${reach.modificationRules}\n")
      case ":eventrules" =>
        println(s"${reach.eventRules}\n")

      case text =>
        val doc = reach.mkDoc(text, "rulershell")
        val mentions = reach.extractFrom(doc)
        displayMentions(mentions, doc)
    }
  }

  // manual terminal cleanup
  reader.getTerminal().restore()
  reader.shutdown()


  // functions

  def printCommands(): Unit = {
    println("\nCOMMANDS:")
    val longest = commands.keys.toSeq.sortBy(_.length).last.length 
    for ((cmd, msg) <- commands)
      println(s"\t$cmd${"\t"*(1 + (longest - cmd.length)/4)}=> $msg")
    println
  }

}

