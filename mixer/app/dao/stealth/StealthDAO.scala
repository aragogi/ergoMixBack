package dao.stealth

import javax.inject.{Inject, Singleton}
import models.StealthModel._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import helpers.DbUtils

import scala.concurrent.{ExecutionContext, Future}

trait StealthComponent { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import profile.api._

  class StealthTable(tag: Tag) extends Table[StealthModel](tag, "STEALTH") {
    def stealthId = column[String]("STEALTH_ID", O.PrimaryKey)
    def sk = column[String]("SK")
    def stealthName = column[String]("STEALTH_NAME")
    def * = (stealthId, sk, stealthName) <> (StealthModel.tupled, StealthModel.unapply)
  }
}

@Singleton()
class StealthDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends StealthComponent
    with HasDatabaseConfigProvider[JdbcProfile] with DbUtils{

  import profile.api._

  val stealthQuery = TableQuery[StealthTable]

  /**
   * inserts a scan into db
   * @param stealth StealthModel
   */
  def insert(stealth: StealthModel): Future[Unit] = db.run(stealthQuery += stealth).map(_ => ())

  /**
   * returns all addresses
   *
   */
  def all: Future[Seq[StealthModel]] = db.run(stealthQuery.result)

  /**
   * deletes all of addresses
   *
   */
  def clear: Future[Unit] = db.run(stealthQuery.delete).map(_ => ())


  /**
   * @param sk String
   * @return Output record(s) associated with the header
   */
  def getBySecretKey(sk: String): DBIO[Seq[StealthTable#TableElementType]] = {
    stealthQuery.filter(_.sk === sk).result
  }


  /**
   * @param stealthName String
   * @return Number of rows deleted
   */
  def deleteByStealthName(stealthName: String): Int = {
    execAwait(stealthQuery.filter(_.stealthName === stealthName).delete)
  }

  /**
   * @param stealthId Long
   * @return Number of rows deleted
   */
  def deleteById(stealthId: String): Int = {
    execAwait(stealthQuery.filter(_.stealthId === stealthId).delete)
  }

  /**
   * @param sk String
   * @return Number of rows deleted
   */
  def deleteBySecretKey(sk: String): Int = {
    execAwait(stealthQuery.filter(_.sk === sk).delete)
  }


  /**
   * @return Int number of scanning rules
   */
  def count(): Int = {
    execAwait(stealthQuery.length.result)
  }

  /**
   * @param stealthName String
   * @return whether this stealth query exists for a specific stealthName or not
   */
  def existsByStealthName(stealthName: String): Future[Boolean] = {
    db.run(stealthQuery.filter(req => req.stealthName === stealthName).exists.result)
  }

  /**
   * selects request stealthName
   *
   * @param stealthName String
   *
   * @return stealth record with stealthName == stealthName
   */
  def selectByStealthName(stealthName: String): Future[Seq[StealthModel]] = {
    db.run(stealthQuery.filter(req => req.stealthName === stealthName).result)
  }

  def selectByStealthId(stealthId: String): Future[Option[StealthModel]] = {
    db.run(stealthQuery.filter(req => req.stealthId === stealthId).result.headOption)
  }
}
