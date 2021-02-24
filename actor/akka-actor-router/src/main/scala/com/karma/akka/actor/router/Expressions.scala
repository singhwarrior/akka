package com.karma.akka.actor.router

object Expressions {
  def callExpression(a : Int, b : Int, expr : String) = expr match {
    case "divide" => 
      if(b == 0) throw new ArithmeticException
      else divide(a,b)
      
  }
  
  def init() = 
    throw new IllegalArgumentException
  
  
//  @throws(classOf[ArithmeticException])
  def divide(num : Int, denom : Int): Int = num/denom 
}