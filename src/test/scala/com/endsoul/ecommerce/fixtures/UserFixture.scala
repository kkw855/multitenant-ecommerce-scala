package com.endsoul.ecommerce.fixtures

import com.endsoul.ecommerce.domain.user.{NewUserInfo, User}

trait UserFixture {
  val leeUsername = "Lee"
  val leePassword = "lee123"

  val hongUsername = "Hong"
  val hongPassword = "hong123"

  val Lee: User = User(leeUsername, "lee@gmail.com", "/ELXh1K0RhxExGt5iy+M+g==,ycaoYHXvOuVDNSdBcHf2AfL3t+Blm+5D/BlSwfA+fhg=")
  val Hong: User = User(hongUsername, "hong@gmail.com", "4bVwJep2zVKuGEyOJON69A==,vck3g99w8ccboF5Wx/2H/yISJoryuLh0R9/u4M7ewOo=")
  val UpdatedHong: User = User(hongUsername, "hong123@naver.com", "3VzkHLEKWzC8mjKwztYxuw==,x2tY20TxnLj3j6GmQIWJL7FdeHihH3FXCx/oqFuo3cI=")
  val NewUser: User = User("Go", "newuser@gmail.com", "JgHXCXr8Sch1p4d3ItEIeQ==,IcFe8qw9/bg0p3IvySJ0/IOV/N7wbJVCZtXlEoPgFgc=")

  val NewUserLee: NewUserInfo = NewUserInfo(
    leeUsername,
    "lee@gmail.com",
    leePassword
  )

  val NewUserHong: NewUserInfo = NewUserInfo(
    hongUsername,
    "lee@gmail.com",
    hongPassword
  )
}
