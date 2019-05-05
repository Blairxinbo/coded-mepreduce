object DemoData {

  val numKeys = 3
  val numNode = 3

  val keyBlue = "B"
  val keyRed = "R"
  val keyGreen = "G"

  val data = Array(
    "BRRG".toList.map(_.toString),
    "BRRG".toList.map(_.toString),
    "BRRG".toList.map(_.toString),
    "BRRG".toList.map(_.toString),
    "BRRG".toList.map(_.toString),
    "BRRG".toList.map(_.toString),
  )

  val nodeKeyMap = Map(
    0 -> keyBlue,
    1 -> keyGreen,
    2 -> keyRed
  )

  val encodeMap = Map(
    0 -> Map(keyGreen -> 0, keyRed -> 2),
    1 -> Map(keyBlue -> 4, keyRed -> 3),
    2 -> Map(keyGreen -> 1, keyBlue -> 5)
  )

  val decodeMap = Map(
    0 -> Map(1 -> DecodeMapItem(keyRed, 3), 2 -> DecodeMapItem(keyGreen, 1)),
    1 -> Map(0 -> DecodeMapItem(keyRed, 2), 2 -> DecodeMapItem(keyBlue, 5)),
    2 -> Map(0 -> DecodeMapItem(keyGreen, 0), 1 -> DecodeMapItem(keyBlue, 4)),
  )

  val splitMap = Map(
    0 -> List(0, 1, 2, 3),
    1 -> List(2, 3, 4, 5),
    2 -> List(4, 5, 0, 1)
  )

}
