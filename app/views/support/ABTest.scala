package views.support

import com.gu.i18n.CountryGroup
import com.gu.i18n.CountryGroup._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.mvc.{Cookie, Request}
import scala.util.Random
import scalaz.NonEmptyList
import views.support.AmountHighlightTest.AmountVariantData

sealed trait VariantData

object VariantData {
  implicit val formatter: Writes[VariantData] = new Writes[VariantData] {
    def writes(o: VariantData): JsValue = {
      o match {
        case amount: AmountVariantData => AmountVariantData.format.writes(amount)
      }
    }
  }
}

case class Variant(testName: String, testSlug: String, variantName: String, variantSlug: String, weight: Double, data: Option[VariantData], countryGroupFilter: Set[CountryGroup] = Set.empty) {
  def matches(countryGroup: CountryGroup): Boolean = countryGroupFilter.isEmpty || countryGroupFilter.contains(countryGroup)
}

object Variant {
  implicit val writes: Writes[Variant] = (
    (__ \ "testName").write[String] and
      (__ \ "testSlug").write[String] and
      (__ \ "variantName").write[String] and
      (__ \ "variantSlug").write[String] and
      (__ \ "weight").write[Double] and
      (__ \ "data").writeNullable[VariantData]
    ) (v => (v.testName, v.testSlug, v.variantName, v.variantSlug, v.weight, v.data))
}

trait TestTrait {
  def name: String
  def slug: String
  def variants: NonEmptyList[Variant]

  def makeVariant(variantName: String, variantSlug: String, weight: Double, data: Option[VariantData] = None, countryGroupFilter: Set[CountryGroup] = Set.empty): Variant = {
    Variant(name, slug, variantName, variantSlug, weight, data, countryGroupFilter)
  }

  val variantsByCountry: Map[CountryGroup, Set[Variant]] = {
    def filterVariants(countryGroup: CountryGroup) = variants.list.filter(_.matches(countryGroup)).toSet

    CountryGroup.allGroups.map(country => country -> filterVariants(country)).toMap
    }

  val variantRangesByCountry: Map[CountryGroup, Seq[(Double, Variant)]] = {
   def addRanges(filteredVariants: Set[Variant]): Seq[(Double, Variant)] = {
     val filteredVariantList = filteredVariants.toList
     val variantWeights = filteredVariantList.map(_.weight)
     val weightSum = variantWeights.sum
     val totalWeight = if (weightSum != 0.0) weightSum else 1
     val cdf: Seq[Double] = variantWeights.scanLeft(0.0)(_ + _).tail.map(_ / totalWeight)
     cdf.zip(filteredVariantList)
   }

    variantsByCountry.map { case (country, variants) => country -> addRanges(variants) }
  }
}

object AmountHighlightTest extends TestTrait {
  def name = "AmountHighlightTest"

  def slug = "highlight"

  case class AmountVariantData(values: List[Int], preselect: Option[Int]) extends VariantData

  object AmountVariantData {
    implicit val format: Format[AmountVariantData] = Json.format[AmountVariantData]
  }

  private lazy val notAustralia: Set[CountryGroup] = Set(
    UK,
    US,
    Canada,
    NewZealand,
    Ireland,
    Europe,
    RestOfTheWorld
  )

  def variants = NonEmptyList(
    //New variants go here.
    makeVariant("6 values - 25 highlight", "6-25", 0, Some(AmountVariantData(List(5,10,25, 50, 100, 250), Some(25))), notAustralia),

    makeVariant("Amount - 25 highlight", "25", 0, Some(AmountVariantData(List(25, 50, 100, 250), Some(25))), notAustralia),
    makeVariant("Amount - 50 highlight", "50", 1, Some(AmountVariantData(List(25, 50, 100, 250), Some(50))), notAustralia),
    makeVariant("Amount - 100 highlight", "100", 0, Some(AmountVariantData(List(25, 50, 100, 250), Some(100))), notAustralia),
    makeVariant("Amount - 250 highlight", "250", 0, Some(AmountVariantData(List(25, 50, 100, 250), Some(250))), notAustralia),
    makeVariant("Amount - 100 highlight Australia", "100-Australia", 1, Some(AmountVariantData(List(50, 100, 250, 500), Some(100))), Set(Australia))
  )
}

object Test {
  private val MaxTestId = 100
  val CookiePrefix     = "gu.contributions.test"
  val TestIdCookieName = s"$CookiePrefix.id"

  val allTests = List(AmountHighlightTest, AARecurringTest)

  def cookieName(v: Variant) = s"$CookiePrefix.${v.testSlug}"
  def cookieName(t: TestTrait) = s"$CookiePrefix.${t.slug}"

  def testIdFor[A](request: Request[A]): Int = {
    val id = request.cookies.get(TestIdCookieName) map(_.value.toInt) getOrElse Random.nextInt(MaxTestId)
    if (id == 0) MaxTestId else id
  }

  def testIdCookie(id: Int) = Cookie(TestIdCookieName, id.toString, maxAge = Some(604800))
  def variantCookie(v: Variant) = Cookie(cookieName(v), v.variantSlug, maxAge = Some(604800))

  def pickVariant[A](countryGroup: CountryGroup, request: Request[A], test: TestTrait): Variant = {

    def pickRandomly: Variant = {
      val n = Random.nextDouble
      test.variantRangesByCountry(countryGroup).dropWhile(_._1 < n).head._2
    }

    def pickByQueryStringOrCookie: Option[Variant] = {
      val search: Option[String] = request.getQueryString(test.slug)
        .orElse(request.cookies.get(s"$CookiePrefix.${test.slug}").map(_.value))
      test.variantsByCountry(countryGroup).find(v => search.contains(v.variantSlug))
    }

    pickByQueryStringOrCookie getOrElse pickRandomly
  }

  def pickTest[A](request: Request[A], userId: Int): TestTrait = {
    def pickByQueryString = allTests.find(t => request.getQueryString(t.slug).nonEmpty)
    def pickByCookie = allTests.find(t => request.cookies.get(s"$CookiePrefix.${t.slug}").nonEmpty)
    def pickRandomly = allTests(userId % allTests.length)

    pickByQueryString orElse pickByCookie getOrElse pickRandomly
  }

  def getContributePageVariant[A](countryGroup: CountryGroup, userId: Int, request: Request[A]): Variant = {
    pickVariant(countryGroup, request, pickTest(request, userId))
  }
}

/**
 * The first time the recurring test was run, there was a big discrepancy between the data in Ophan and GA.
 * This test fires an event to Ophan on page load; (independently) an event will also be fired to GA on page load.
 * By comparing the event counts, we hope to debug the discrepancy.
 */
object AARecurringTest extends TestTrait {

  override def name: String = "AARecurringTest"

  override def slug: String = "aa-recurring-test"

  override def variants: NonEmptyList[Variant] = NonEmptyList(
    makeVariant("default", "default", weight = 1, data = None)
  )
}
