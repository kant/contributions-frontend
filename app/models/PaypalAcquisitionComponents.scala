package models

import abtests.Allocation
import actions.CommonActions.MetaDataRequest
import com.paypal.api.payments.Payment
import controllers.httpmodels.CaptureRequest
import ophan.thrift.componentEvent.ComponentType
import ophan.thrift.event._
import services.ContributionOphanService.{ContributionAcquisitionSubmissionBuilder, OphanIds}

import scala.reflect.ClassTag

trait PaypalAcquisitionSubmissionBuilder[A] extends ContributionAcquisitionSubmissionBuilder[A] {
  import PaypalAcquisitionSubmissionBuilder._

  protected def paymentInfo(payment: Payment)(implicit classTag: ClassTag[A]): Either[String, PaymentInfo] =
    for {
      transaction <- attemptToGet("transaction")(payment.getTransactions.get(0))
      amount <- attemptToGet("amount")(transaction.getAmount.getTotal.toDouble)
      currency <- attemptToGet("currency")(transaction.getAmount.getCurrency)
      countryCode <- attemptToGet("country code")(payment.getPayer.getPayerInfo.getCountryCode)
    } yield PaymentInfo(amount, currency, countryCode)
}

object PaypalAcquisitionSubmissionBuilder {

  protected case class PaymentInfo(amount: Double, currency: String, countryCode: String)
}

object PaypalAcquisitionComponents {

  case class Execute(payment: Payment, request: Execute.RequestData)

  object Execute {

    case class RequestData(
      cmp: Option[String],
      intCmp: Option[String],
      refererPageviewId: Option[String],
      refererUrl: Option[String],
      ophanPageviewId: Option[String],
      ophanBrowserId: Option[String],
      ophanVisitId: Option[String],
      componentId: Option[String],
      componentType: Option[ComponentType],
      source: Option[AcquisitionSource],
      abTest: Option[AbTest],
      testAllocations: Set[Allocation]
    )

    implicit object paypalAcquisitionSubmissionBuilder extends PaypalAcquisitionSubmissionBuilder[Execute] {

      def buildOphanIds(components: Execute): Either[String, OphanIds] = {
        import components._
        for {
          browserId <- attemptToGet("browser id")(request.ophanBrowserId.get)
          pageviewId <- attemptToGet("pageview id")(request.ophanPageviewId.get)
        } yield OphanIds(browserId, pageviewId, request.ophanVisitId)
      }

      def buildAcquisition(components: Execute): Either[String, Acquisition] = {
        import components._

        paymentInfo(payment).map { info =>
          Acquisition(
            product = Product.Contribution,
            paymentFrequency = PaymentFrequency.OneOff,
            currency = info.currency,
            amount = info.amount,
            amountInGBP = None, // Calculated at the sinks of the Ophan stream
            paymentProvider = Some(ophan.thrift.event.PaymentProvider.Paypal),
            campaignCode = Some(Set(request.intCmp, request.cmp).flatten),
            abTests = Some(abTestInfo(request.testAllocations, request.abTest)),
            countryCode = Some(info.countryCode),
            referrerPageViewId = request.refererPageviewId,
            referrerUrl = request.refererUrl,
            componentId = request.componentId,
            componentTypeV2 = request.componentType,
            source = request.source
          )
        }
      }
    }
  }

  case class Capture(payment: Payment, request: MetaDataRequest[CaptureRequest])

  object Capture {

    implicit object paypalAcquisitionSubmissionBuilder extends PaypalAcquisitionSubmissionBuilder[Capture] {

      override def buildOphanIds(components: Capture): Either[String, OphanIds] = {
        import components._
        for {
          browserId <- attemptToGet("browser id")(request.body.ophanBrowserId.get)
          pageviewId <- attemptToGet("pageview id")(request.body.ophanPageviewId.get)
        } yield OphanIds(browserId, pageviewId, visitId = None)
      }

      override def buildAcquisition(components: Capture): Either[String, Acquisition] = {
        import components._

        paymentInfo(payment).map { info =>
          Acquisition(
            product = ophan.thrift.event.Product.Contribution,
            paymentFrequency = ophan.thrift.event.PaymentFrequency.OneOff,
            currency = info.currency,
            amount = info.amount,
            amountInGBP = None, // Calculated at the sinks of the Ophan stream
            paymentProvider = Some(ophan.thrift.event.PaymentProvider.Paypal),
            campaignCode = Some(Set(request.body.cmp, request.body.intCmp).flatten),
            abTests = Some(abTestInfo(request.testAllocations, request.body.abTest)),
            countryCode = Some(info.countryCode),
            referrerPageViewId = request.body.refererPageviewId,
            referrerUrl = request.body.refererUrl,
            componentId = request.body.componentId,
            componentTypeV2 = request.body.componentType,
            source = request.body.source
          )
        }
      }
    }
  }
}