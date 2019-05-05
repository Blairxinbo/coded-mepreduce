object types {

  abstract class Network() {

    def sendBroadcast(message: Array[Byte])

    def receiveBroadcast(node: Node)

    def summary()

  }

  abstract class Data() {

    def split(index: Int, total: Int): Array[String]

  }

}
