package com.endsoul.ecommerce.fixtures

import com.endsoul.ecommerce.domain.category.CategoryInfo

import java.util.UUID

trait CategoryFixture {
  val SoftwareCategoryId: UUID = UUID.fromString("acdcd251-9001-41ae-adb4-ad601c171cea")
  val DesignCategoryId: UUID = UUID.fromString("7ef31fe6-9459-4832-ab26-fe246a3d6d10")

  val SoftwareCategory: CategoryInfo = CategoryInfo(
    SoftwareCategoryId,
    "Software Development",
    "software-development",
    "#FFB347"
  )

  val DesignCategory: CategoryInfo = CategoryInfo(
    DesignCategoryId,
    "Design & UX",
    "design",
    "#7EC8E3"
  )
}
