package scalafix.internal.rule

import scala.meta._
import scalafix.Patch
import scalafix.SemanticdbIndex
import scalafix.rule.RuleCtx
import scalafix.rule.SemanticRule

case class NoAutoTupling(index: SemanticdbIndex)
    extends SemanticRule(index, "NoAutoTupling") {

  private[this] def addWrappingParens(ctx: RuleCtx, args: Seq[Term]): Patch =
    ctx.addLeft(args.head.tokens.head, "(") +
      ctx.addRight(args.last.tokens.last, ")")

  override def fix(ctx: RuleCtx): Patch = {
    val adaptations = index.messages.toIterator.collect {
      case Message(pos, _, msg)
          if msg.startsWith("Adapting argument list by creating a") =>
        pos
    }.toSet

    ctx.tree.collect {
      case t: Term.Apply if adaptations.contains(t.pos) =>
        addWrappingParens(ctx, t.args)
    }.asPatch
  }
}
