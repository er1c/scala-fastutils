import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap

import scala.collection.mutable
import scala.{specialized => sp}

/**
 * Specialized, fast and memory efficient implementation of map.
 *
 * TODO
 */
class FastMap[@sp(Int, Long, AnyRef) A, @sp(Int, Long, AnyRef) B] {
  private val underlying = mutable.Map[A, B]()
  def +=(key: A, value: B): FastMap[A, B] = {
    underlying += ((key, value))
    this
  }

  def -=(key: A): FastMap[A, B] = {
    underlying -= key
    this
  }

  def get(key: A): Option[B] = {
    underlying.get(key)
  }

  def iterator: Iterator[(A, B)] = underlying.iterator

  def clear(): Unit = {
    underlying.clear()
  }

  def contains(key: A) = get(key).isDefined

  /**
   * Farewell performance!
   */
  def asScalaMap() = new FastMapWrapper[A, B](this)
}

object FastMap {
  def apply[@sp(Int, Long, AnyRef) A, @sp(Int, Long, AnyRef) B]()(implicit impl: FastMapFactory[A, B]): FastMap[A, B] = {
    impl()
  }
}

abstract class FastMapFactory[@sp(Int, Long, AnyRef) A, @sp(Int, Long, AnyRef) B] {
  def apply(): FastMap[A, B]
}

final class FastMapFactoryAnyAny[A, B] extends FastMapFactory[A, B] {
  def apply() = new FastMap[A, B]()
}

final class FastMapFactoryIntAny[B] extends FastMapFactory[Int, B] {
  def apply() = new FastMapInt[B]()
}

final class FastMapFactoryIntInt extends FastMapFactory[Int, Int] {
  def apply() = new FastMapIntInt()
}

trait LowestPriorityImplicit {
  private val factoryAnyAny = new FastMapFactoryAnyAny[AnyRef, AnyRef]()

  implicit def factoryAnyAny[A, B]: FastMapFactory[A, B] = factoryAnyAny.asInstanceOf[FastMapFactoryAnyAny[A, B]]
}

trait LowerPriorityImplicit extends LowestPriorityImplicit {
  private val factoryIntAny = new FastMapFactoryIntAny[AnyRef]()
  implicit def factoryIntAny[B]: FastMapFactoryIntAny[B] = factoryIntAny.asInstanceOf[FastMapFactoryIntAny[B]]
}

object FastMapFactories extends LowerPriorityImplicit {
  implicit val factoryIntInt = new FastMapFactoryIntInt()
}

class FastMapInt[@sp(Int, Long, AnyRef) B] extends FastMap[Int, B] {
  //todo
}

class FastMapIntInt extends FastMap[Int, Int] {
  val underlying = new Int2IntOpenHashMap()
  override def +=(key: Int, value: Int): FastMapIntInt = {
    underlying.put(key, value)
    this
  }

  override def -=(key: Int): FastMapIntInt = {
    underlying.remove(key)
    this
  }

  override def get(key: Int): Option[Int] = {
    Option(underlying.get(key))
  }

  override def clear(): Unit = {
    underlying.clear()
  }

  override def contains(key: Int) = underlying.containsKey(key)
}

/**
 * Wraps `FastMap` to use it as Scala's Map.
 */
class FastMapWrapper[A, B](underlying: FastMap[A, B]) extends mutable.AbstractMap[A, B]
with mutable.MapLike[A, B, FastMapWrapper[A, B]] {
  import FastMapFactories._

  override def +=(kv: Tuple2[A, B]): FastMapWrapper.this.type = {
    underlying += (kv._1, kv._2)
    this
  }

  override def -=(key: A): FastMapWrapper.this.type = {
    underlying -= key
    this
  }

  override def get(key: A): Option[B] = underlying.get(key)

  override def iterator: Iterator[(A, B)] = underlying.iterator

  override def empty = new FastMapWrapper[A, B](FastMap[A, B])
}

object Test {
  def main(args: Array[String]): Unit = {
    import FastMapFactories._
    val map0 = new Int2IntOpenHashMap()
    val map1 = FastMap[Int, Int]()
    val map2 = mutable.Map[Int, Int]()

    // warm up jvm
    for (i <- 1 to 10000) {
      map0.put(i, i * i)
      map1.+=(i, i * i)
      map2.+=((i, i * i))
    }
    map0.clear()
    map1.clear()
    map2.clear()
    // end warm up

    val times = 10000000

    val start0 = System.currentTimeMillis()
    for (i <- 1 to times) {
      map0.put(i, i * i)
    }
    for (i <- 1 to times) {
      map0.containsKey(i)
    }
    println(System.currentTimeMillis() - start0)

    val start = System.currentTimeMillis()
    for (i <- 1 to times) {
      map1.+=(i, i * i)
    }
    for (i <- 1 to times) {
      map1.contains(i)
    }
    println(System.currentTimeMillis() - start)

    val start2 = System.currentTimeMillis()
    for (i <- 1 to times) {
      map2.+=((i, i * i))
    }
    for (i <- 1 to times) {
      map2.contains(i)
    }
    println(System.currentTimeMillis() - start2)
  }
}
