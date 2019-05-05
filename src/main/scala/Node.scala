import scala.math.max

class Node(
            nodeId: Int,
            nodeKey: String,
            data: Map[Int, List[String]],
            allEncodeMap: Map[Int, Map[String, Int]],
            allDecodeMap: Map[Int, Map[Int, DecodeMapItem]],
          ) {

  private val encodeMap: Map[String, Int] = allEncodeMap(nodeId)
  private val decodeMap: Map[Int, DecodeMapItem] = allDecodeMap(nodeId)

  // mutable local result
  // [ splitId => { key1 => value1,, ... }, ... ]
  private val localResult: Map[Int, Map[String, Int]] = {
    data.keys.map(splitIndex =>
      // read the data items
      splitIndex -> data(splitIndex)
        // map to <key, 1>
        .map((str: String) => Map(str -> 1))
        // get sum of keys => <key, sum>
        .fold(Map())(mergeKV)) toMap
  }

  // result from other results
  // [ splitId => value ]
  private var reduceResult: Map[Int, Int] = Map()

  //  private var finalResult: Array[Int] = Array()

  def mapping(): Array[Byte] = {
    encodedMessage()
  }

  private def digLocalResult(fileIndex: Int, keyName: String): Int = {
    val sub = localResult.getOrElse(fileIndex, Map())
    sub.getOrElse(keyName, 0)
  }

  private def encodedMessage(): Array[Byte] = {

    // localResults
    // Map(
    //  0 => Map(A -> 6, B -> 9, C -> 4),
    //  1 => Map(A -> 6, B -> 9, C -> 4),
    //  2 => Map(A -> 6, B -> 9, C -> 4),
    //  3 => Map(A -> 6, B -> 9, C -> 4))

    // encode
    // Map(key3 -> 0, key2 -> 2)
    // =>
    // List["0:Key3:500", "2:Key2:123"]
    val beforeCode: Array[String] = encodeMap.map {
      case (key, splitId) =>
        splitId + ":" + key + ":" + digLocalResult(splitId, key)
    }.toArray

    println("node " + nodeId + " preparing two messages:")
    beforeCode.foreach(println(_))

    val left: Array[Byte] = beforeCode(0).toCharArray.map(_.toByte)
    val right: Array[Byte] = beforeCode(1).toCharArray.map(_.toByte)

    val codedLength = max(left.length, right.length)

    val afterCoded: Array[Byte] = 0.until(codedLength).map(index => {
      val leftByte = if (index < left.length) left(index) else ' '.toByte
      val rightByte = if (index < right.length) right(index) else ' '.toByte
      val r = leftByte ^ rightByte
      r.toByte
    }).toArray

    println("node " + nodeId + " sent an encoded message:")
    println(afterCoded.mkString(" "))

    Array(nodeId.toByte) ++ afterCoded
  }

  private def decodeMessage(packet: Array[Byte]): String = {
    val otherNodeId = packet(0).toInt
    if (otherNodeId == nodeId) {
      return ""
    }

    val decodeMapItem = decodeMap(otherNodeId)
    val myKey = decodeMapItem.key
    val mySplitId = decodeMapItem.splitId
    val myMessage = mySplitId.toString +
      ":" + myKey +
      ":" + digLocalResult(mySplitId, myKey).toString

    val myBytes = myMessage.toCharArray.map(_.toByte)
    val otherBytes = packet.slice(1, packet.length)

    val codedLength = max(myBytes.length, otherBytes.length)

    val afterDecoded: Array[Byte] = 0.until(codedLength).map(index => {
      val myByte = if (index < myBytes.length) myBytes(index) else ' '.toByte
      val otherByte = if (index < otherBytes.length) otherBytes(index) else ' '.toByte
      (myByte ^ otherByte).toByte
    }).toArray

    val msg = afterDecoded.map(_.toChar).mkString("").trim

    println("node " + nodeId + " received an message from node " + otherNodeId + ":")
    println(msg)

    msg
  }

  def receiving(packet: Array[Byte]): Boolean = {
    val otherNodeId = packet(0).toInt
    val decodedString = decodeMessage(packet)
    if (decodedString == "") return false

    val decodedSegments = decodedString.split(":")

    if (decodedSegments.length == 3) {
      val otherSplitId = decodedSegments(0).toInt
      // val otherKey = decodedSegments(1)
      val otherValue = decodedSegments(2).toInt

      reduceResult = reduceResult.updated(otherSplitId, otherValue)

      return true
    }

    false
  }

  def reducing(): Unit = {
    localResult.foreach {
      case (splitId, kv) =>
        reduceResult = reduceResult.updated(splitId, kv(nodeKey))
    }

    if (reduceResult.size == 6) {
      val finalResult = reduceResult.values.sum

      println("node " + nodeId + " has final result:")
      println(nodeKey + ": " + finalResult)
    } else {
      println("node " + nodeId + " failed.")
    }
  }

  private def mergeKV(m1: Map[String, Int], m2: Map[String, Int]): Map[String, Int] =
    (m1.keySet ++ m2.keySet) map { i => i -> (m1.getOrElse(i, 0) + m2.getOrElse(i, 0)) } toMap

}