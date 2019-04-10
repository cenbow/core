package com.cc.sql.executor

import com.cc.antlr.SqlParser.SqlContext
import com.cc.shell.engine.repl.SparkInterpreter
import com.cc.sql.ExecutorTracker
import org.apache.spark.sql.SparkSession

/*
 * User: chenchong
 * Date: 2019/4/9
 * description:
 */

class InsertExecutor(sql: String) extends Executor {
  override protected def parse(session: SparkSession, interpreter: SparkInterpreter): ExecutorTracker = {
    commonParse{
      session.sql(sql).count()
      success("success: 执行成功")
    }
  }
}

object InsertExecutor {
  def apply(sql:SqlContext): InsertExecutor = new InsertExecutor(sql.getText)
}