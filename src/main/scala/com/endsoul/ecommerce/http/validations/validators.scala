package com.endsoul.ecommerce.http.validations

import cats.data.Validated
import cats.implicits.*
import com.endsoul.ecommerce.domain.auth.LoginInfo

object validators {
  
  sealed trait ValidationFailure(val errorMessage: String)
  
  type ValidationResult[A] = Validated[ValidationFailure, A]
  
  trait Validator[A] {
    def validate(a: A): ValidationResult[A]
  }
  
//  given loginInfoValidator: Validator[LoginInfo] = (loginInfo) => {
//    val validUsername = 
//  }
}
