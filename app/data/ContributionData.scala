package data

import java.sql.Connection

import anorm._
import cats.data.{Xor, XorT}
import data.AnormMappings._
import models.{ContributionMetaData, Contributor, PaymentHook}
import play.api.Logger
import play.api.db.Database

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class ContributionData(db: Database)(implicit ec: ExecutionContext) {

  def withAsyncConnection[A](autocommit: Boolean = false)(block: Connection => A): XorT[Future, String, A] = XorT(Future {
    val result = Try(db.withConnection(autocommit)(block))
    Xor.fromTry(result).leftMap { exception =>
      Logger.error("Error encountered during the execution of the sql query", exception)
      "Error encountered during the execution of the sql query"
    }
  })

  def insertPaymentHook(paymentHook: PaymentHook): XorT[Future, String, PaymentHook] = {
    withAsyncConnection(autocommit = true) { implicit conn =>
      val request = SQL"""
        INSERT INTO payment_hooks(
          contributionid,
          paymentid,
          provider,
          created,
          currency,
          cardcountry,
          amount,
          convertedamount,
          status,
          email
        ) VALUES (
          ${paymentHook.contributionId}::uuid,
          ${paymentHook.paymentId},
          ${paymentHook.provider}::paymentProvider,
          ${paymentHook.created},
          ${paymentHook.currency},
          ${paymentHook.cardCountry},
          ${paymentHook.amount},
          ${paymentHook.convertedAmount},
          ${paymentHook.status}::paymentStatus,
          ${paymentHook.email}
        ) ON CONFLICT(contributionId) DO
        UPDATE SET
          contributionid = excluded.contributionid,
          paymentid = excluded.paymentid,
          provider = excluded.provider,
          created = excluded.created,
          currency = excluded.currency,
          cardcountry = excluded.cardcountry,
          amount = excluded.amount,
          convertedamount = excluded.convertedamount,
          status = excluded.status,
          email = excluded.email"""
      request.execute()
      paymentHook
    }
  }

  def insertPaymentMetaData(pmd: ContributionMetaData): XorT[Future, String, ContributionMetaData] = {
    withAsyncConnection(autocommit = true) { implicit conn =>
      val request = SQL"""
        INSERT INTO contribution_metadata(
          contributionid,
          created,
          email,
          ophanid,
          abtests,
          cmp,
          intcmp
        ) VALUES (
          ${pmd.contributionId}::uuid,
          ${pmd.created},
          ${pmd.email},
          ${pmd.ophanId},
          ${pmd.abTests},
          ${pmd.cmp},
          ${pmd.intCmp}
        ) ON CONFLICT(contributionId) DO
        UPDATE SET
          contributionid = excluded.contributionid,
          created = excluded.created,
          email = excluded.email,
          ophanid = excluded.ophanid,
          abtests = excluded.abtests,
          cmp = excluded.cmp,
          intcmp = excluded.intcmp"""
      request.execute()
      pmd
    }
  }

  def saveContributor(contributor: Contributor): XorT[Future, String, Contributor] = {
    withAsyncConnection(autocommit = true) { implicit conn =>
      val request = SQL"""
        INSERT INTO live_contributors(
          receipt_email,
          name,
          firstname,
          lastname,
          iduser,
          postcode,
          marketingoptin
        ) VALUES (
          ${contributor.email},
          ${contributor.name},
          ${contributor.firstName},
          ${contributor.lastName},
          ${contributor.idUser},
          ${contributor.postCode},
          ${contributor.marketingOptIn}
        ) ON CONFLICT(receipt_email) DO
        UPDATE SET
          receipt_email = excluded.receipt_email,
          name = COALESCE(excluded.name, live_contributors.name),
          firstname = COALESCE(excluded.firstname, live_contributors.firstname),
          lastname = COALESCE(excluded.lastname, live_contributors.lastname),
          iduser = COALESCE(excluded.iduser, live_contributors.iduser),
          postcode = COALESCE(excluded.postcode, live_contributors.postcode),
          marketingoptin = COALESCE(excluded.marketingoptin, live_contributors.marketingoptin
        )"""
      request.execute()
      contributor
    }
  }
}
