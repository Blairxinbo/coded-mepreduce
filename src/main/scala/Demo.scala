import java.io.IOException
import java.net._

object Demo extends App {

  if (args.length == 0) {
    println("wrong args.")
    println("java -jar coded-mapreduce.jar [Node ID]")
  }

  val nodeId: Int = args(0).toInt

  val INET_ADDR = "224.0.0.3"
  val PORT = 8888

  val numberOfNodes = DemoData.numKeys
  val encodeMap = DemoData.encodeMap
  val decodeMap = DemoData.decodeMap
  val allData = DemoData.data
  val splitMap = DemoData.splitMap
  val nodeKeyMap = DemoData.nodeKeyMap

  val composeData = (nodeId: Int) => {
    splitMap(nodeId).map(i => i -> allData(i)).toMap
  }

  val node = new Node(nodeId, nodeKeyMap(nodeId), composeData(nodeId), encodeMap, decodeMap)

  // the mapping thread
  val threadMapping: Thread = new Thread {

    override def run(): Unit = {
      val addr = InetAddress.getByName(INET_ADDR)
      val bytes = node.mapping()

      val serverSocket = new DatagramSocket
      try {
        val msgPacket = new DatagramPacket(bytes, bytes.length, addr, PORT)
        serverSocket.send(msgPacket)
      } finally {
        if (serverSocket != null) serverSocket.close()
      }
    }

  }

  val threadReceiving: Thread = new Thread {

    override def run(): Unit = {
      // Get the address that we are going to connect to.
      try {
        val address = InetAddress.getByName(INET_ADDR)
        val buf = new Array[Byte](256)

        val clientSocket = new MulticastSocket(PORT)
        clientSocket.joinGroup(address)

        // read packets from other nodes
        var packetCount = numberOfNodes - 1
        while (packetCount > 0) {
          val msgPacket = new DatagramPacket(buf, buf.length)
          clientSocket.receive(msgPacket)
          if (node.receiving(buf)) {
            packetCount -= 1
          }
        }
        clientSocket.close()

      } catch {
        case e: IOException =>
          e.printStackTrace()
        case e: UnknownHostException =>
          e.printStackTrace()
      }
    }
  }

  threadReceiving.start()

  println("Start receiving packages, waiting 5 seconds for other nodes to start.")
  Thread.sleep(5000)

  threadMapping.start()
  threadMapping.join()
  threadReceiving.join()

  node.reducing()

}
