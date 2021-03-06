package examples.ZSChain.mining

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import examples.commons.{PublicKey25519NoncedBox, SimpleBoxTransaction, SimpleBoxTransactionMemPool}
import examples.ZSChain.blocks.{HybridBlock, PosBlock, PowBlock}
import examples.ZSChain.history.HybridHistory
import examples.ZSChain.state.HBoxStoredState
import examples.ZSChain.wallet.HBoxWallet
import scorex.core.NodeViewHolder.CurrentView
import scorex.core.transaction.state.PrivateKey25519
import scorex.crypto.hash.Blake2b256
import scorex.util.ScorexLogging
import scorex.utils.Random


class PosForger(settings: HybridSettings, viewHolderRef: ActorRef) extends Actor with ScorexLogging {

  import PosForger.ReceivableMessages._
  import PosForger._
  import scorex.core.NodeViewHolder.ReceivableMessages.LocallyGeneratedModifier


  var forging = false

  override def receive: Receive = {
    case StartForging =>
      forging = true
      viewHolderRef ! getRequiredData

    case pfi: PosForgingInfo =>
      val target = settings.mining.MaxTarget / pfi.diff
      println("POS DIFFICULTY "+pfi.diff)
      val boxKeys = pfi.boxKeys

      //last check on whether to forge at all
      if (pfi.pairCompleted) {
        self ! StopForging
      } else {
        val powBlock = pfi.bestPowBlock
        val posBlock = pfi.bestPosBlock
        if(posBlock.timestamp > powBlock.timestamp){
          log.debug(s"Trying to generate PoS block on top of PoS Block ${posBlock.encodedId} with balance " +
            s"${boxKeys.map(_._1.value.toLong).sum}")
          val attachment = Random.randomBytes(settings.mining.posAttachmentSize)
          posIterationSecond(powBlock, posBlock, boxKeys, pfi.txsToInclude, attachment, target) match {
            case Some(posBlock) =>
              log.debug(s"Locally generated PoS block: $posBlock")
              forging = false
              viewHolderRef !
                LocallyGeneratedModifier[HybridBlock](posBlock)
            case None =>
              log.debug(s"Failed to generate PoS block")
          }
        }
        else{
          log.debug(s"Trying to generate PoS block on top of ${powBlock.encodedId} with balance " +
            s"${boxKeys.map(_._1.value.toLong).sum}")
          val attachment = Random.randomBytes(settings.mining.posAttachmentSize)
          posIteration(powBlock, boxKeys, pfi.txsToInclude, attachment, target) match {
            case Some(posBlock) =>
              log.debug(s"Locally generated PoS block: $posBlock")
              forging = false
              viewHolderRef !
                LocallyGeneratedModifier[HybridBlock](posBlock)
            case None =>
              log.debug(s"Failed to generate PoS block")
          }
        }

      }

    case StopForging =>
      forging = false
  }
}

object PosForger extends ScorexLogging {

  import scorex.core.NodeViewHolder.ReceivableMessages.GetDataFromCurrentView

  val InitialDifficuly: Long = 1

  object ReceivableMessages {
    case object StartForging
    case object StopForging
    case class PosForgingInfo(pairCompleted: Boolean,
                              bestPowBlock: PowBlock,
                              bestPosBlock: PosBlock,
                              diff: BigInt,
                              boxKeys: Seq[(PublicKey25519NoncedBox, PrivateKey25519)],
                              txsToInclude: Seq[SimpleBoxTransaction])
  }

  import ReceivableMessages.PosForgingInfo

  def hit(pwb: PowBlock)(box: PublicKey25519NoncedBox): BigInt = {
    val h = Blake2b256(pwb.bytes ++ box.bytes)
    BigInt(1, h)
  }

  def posIteration(powBlock: PowBlock,
                   boxKeys: Seq[(PublicKey25519NoncedBox, PrivateKey25519)],
                   txsToInclude: Seq[SimpleBoxTransaction],
                   attachment: Array[Byte],
                   target: BigInt
                  ): Option[PosBlock] = {
    val successfulHits = boxKeys.map { boxKey =>
      val h = hit(powBlock)(boxKey._1)
      (boxKey, h)
    }.filter(t => t._2 < t._1._1.value * target)

    log.info(s"Successful hits: ${successfulHits.size}")

    successfulHits.headOption.map { case (boxKey, _) =>
      PosBlock.create(
        powBlock.id,
        powBlock.id,
        System.currentTimeMillis(),
        txsToInclude,
        boxKey._1,
        attachment,
        boxKey._2)
    }
  }

  def posIterationSecond(powBlock: PowBlock,
                         posBlock: PosBlock,
                   boxKeys: Seq[(PublicKey25519NoncedBox, PrivateKey25519)],
                   txsToInclude: Seq[SimpleBoxTransaction],
                   attachment: Array[Byte],
                   target: BigInt
                  ): Option[PosBlock] = {
    val successfulHits = boxKeys.map { boxKey =>
      val h = hit(powBlock)(boxKey._1)
      (boxKey, h)
    }.filter(t => t._2 < t._1._1.value * target)

    log.info(s"Successful hits: ${successfulHits.size}")

    successfulHits.headOption.map { case (boxKey, _) =>
      PosBlock.create(
        powBlock.id,
        posBlock.id,
        System.currentTimeMillis(),
        txsToInclude,
        boxKey._1,
        attachment,
        boxKey._2)
    }
  }


  val getRequiredData: GetDataFromCurrentView[HybridHistory,
    HBoxStoredState,
    HBoxWallet,
    SimpleBoxTransactionMemPool,
    PosForgingInfo] = {
    val f: CurrentView[HybridHistory, HBoxStoredState, HBoxWallet, SimpleBoxTransactionMemPool] => PosForgingInfo = {
      view: CurrentView[HybridHistory, HBoxStoredState, HBoxWallet, SimpleBoxTransactionMemPool] =>

        val diff = view.history.posDifficulty
        val pairCompleted = view.history.pairCompleted
        val bestPowBlock = view.history.bestPowBlock
        val bestPosBlock = view.history.bestPosBlock
        val boxes = view.vault.boxes().map(_.box).filter(box => view.state.closedBox(box.id).isDefined)
        val boxKeys = boxes.flatMap(b => view.vault.secretByPublicImage(b.proposition).map(s => (b, s)))

        val txs = view.pool.take(TransactionsPerBlock).foldLeft(Seq[SimpleBoxTransaction]()) { case (collected, tx) =>
          if (view.state.validate(tx).isSuccess &&
            tx.boxIdsToOpen.forall(id => !collected.flatMap(_.boxIdsToOpen).contains(id))) collected :+ tx
          else collected
        }

        PosForgingInfo(pairCompleted, bestPowBlock, bestPosBlock, diff, boxKeys, txs)
        //PosForgingInfo(pairCompleted, bestPowBlock, diff, boxKeys, txs)
    }
    GetDataFromCurrentView[HybridHistory,
      HBoxStoredState,
      HBoxWallet,
      SimpleBoxTransactionMemPool,
      PosForgingInfo](f)

  }

  private val TransactionsPerBlock = 50

}

object PosForgerRef {
  def props(settings: HybridSettings, viewHolderRef: ActorRef): Props = Props(new PosForger(settings, viewHolderRef))
  def apply(settings: HybridSettings, viewHolderRef: ActorRef)
           (implicit system: ActorSystem): ActorRef = system.actorOf(props(settings, viewHolderRef))
  def apply(name: String, settings: HybridSettings, viewHolderRef: ActorRef)
           (implicit system: ActorSystem): ActorRef = system.actorOf(props(settings, viewHolderRef), name)
}
