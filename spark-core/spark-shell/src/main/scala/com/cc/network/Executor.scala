package com.cc.network

import com.cc.common.{PropsUtils, ZkSession}
import com.cc.shell.engine.SparkBuilder
import com.cc.shell.engine.repl.SparkInterpreter
import com.cc.shell.utils.Logging
import com.cc.sql.{JobStatus, RpcSqlParser, Status}
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import protocol.client.{Detail, Execute}
import protocol.executor.{BatchReply, Reply}
import rainpoetry.spark.rpc.{RpcCallContext, RpcEnv, ThreadSafeRpcEndpoint}

/*
 * User: chenchong
 * Date: 2019/4/4
 * description: 任务执行器
 */

class Executor(
                override val rpcEnv: RpcEnv,
                _interpreter: SparkInterpreter,
                sparkConf: SparkConf,
                zkDir: String,
                id: String) extends Logging with ThreadSafeRpcEndpoint {

  var sparkSession: SparkSession = _
  var interpreter: SparkInterpreter = _
  var zkSession: ZkSession = _
  val name = Executor.prefix + "_" + id

  //  override def receive: PartialFunction[Any, Unit] = {
  //    case Execute(sql) => println("execute: " + sql)
  //  }

  override def receiveAndReply(context: RpcCallContext): PartialFunction[Any, Unit] = {
    case Execute(sql) =>
      exec(sql) {
        (jobs, times) => {
          val job = jobs(jobs.length - 1)
          val duration = times.sum
          job.status match {
            case Status.Success =>
              job.data match {
                case a:Array[String] =>
                  context.reply(Reply(a, duration, true))
                case s:String => context.reply(Reply(s, duration, true))
              }
            case Status.Failure => context.reply(Reply(job.msg, duration, false))
          }
        }
      }
    case Detail(sql) =>
      exec(sql) {
        (jobs, times) => context.reply(BatchReply(jobs, times))
      }
  }

  private def exec[T](sql: String)(f: (Array[JobStatus], Array[Long]) => Unit): Unit = {
    val resolveSQL = sql.trim.replaceAll("\\r\\n", "")
    info(s"resolveSQL: ${resolveSQL}")
    val sqlParser = new RpcSqlParser(sparkSession, interpreter)
    val (jobs,times) = sqlParser.command(resolveSQL)
    f(jobs,times)
  }

  def measureTime[T](f: T): T = {
    f
  }

  override def onStart(): Unit = {
    zkSession = new ZkSession(PropsUtils.get("zkServers").getOrElse(throw new Exception("zkServers 配置信息不存在")))
    interpreter = _interpreter
    sparkSession = SparkBuilder.createSpark(sparkConf).newSession()
    if (zkDir != null) {
      zkSession.register(zkDir, name)
    }
  }

  override def onStop(): Unit = {
    interpreter.close()
    sparkSession.close()
    zkSession.close()
  }

}

object Executor {
  val prefix = "executor"
}
