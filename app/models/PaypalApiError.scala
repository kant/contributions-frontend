package models

import com.paypal.api.payments.{Error, ErrorDetails}
import com.paypal.base.rest.PayPalRESTException
import enumeratum.{Enum, EnumEntry, PlayJsonEnum}
import play.api.libs.json.{JsValue, Json, Writes}
import scala.collection.JavaConverters._

sealed trait PaypalErrorType extends EnumEntry

object PaypalErrorType extends Enum[PaypalErrorType] with PlayJsonEnum[PaypalErrorType] {
  val values = findValues

  case object NotFound extends PaypalErrorType
  case object PaymentAlreadyDone extends PaypalErrorType
  case object Other extends PaypalErrorType

  def fromPaypalError(error: Error): PaypalErrorType = error.getName match {
    case "INVALID_RESOURCE_ID" => NotFound
    case "PAYMENT_ALREADY_DONE" => PaymentAlreadyDone
    case _ => Other
  }
}

case class PaypalApiError(
  errorType: PaypalErrorType,
  message: String
)

object PaypalApiError {
  def apply(message: String): PaypalApiError = PaypalApiError(PaypalErrorType.Other, message)

  private def detailToString(details: ErrorDetails): String =
    s"""
       |field: ${details.getField}
       |issue: ${details.getIssue}
       |""".stripMargin

  def fromThrowable(exception: Throwable): PaypalApiError = exception match {
    case paypalException: PayPalRESTException =>
      val details = Option(paypalException.getDetails)
        .flatMap(d => Option(d.getDetails))
        .map(_.asScala).getOrElse(Nil)
        .map(detailToString)
        .mkString(";\n")
      PaypalApiError(PaypalErrorType.fromPaypalError(paypalException.getDetails), details)
    case exception: Exception =>
      PaypalApiError(exception.getMessage)
  }

  // defining the Writes manually, otherwise Play-Json gets offended by the overloaded "apply" just above
  implicit val jf: Writes[PaypalApiError] = new Writes[PaypalApiError] {
    override def writes(o: PaypalApiError): JsValue = Json.obj(
      "errorType" -> o.errorType,
      "message" -> o.message
    )
  }
}
