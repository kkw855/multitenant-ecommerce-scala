package com.endsoul.ecommerce.fixtures

import com.endsoul.ecommerce.domain.subcategory.Subcategory

trait SubcategoryFixture extends CategoryFixture {
  val FrontendSubcategory: Subcategory = Subcategory(
    SoftwareCategoryId,
    "Frontend",
    "frontend",
  )

  val BackendSubcategory: Subcategory = Subcategory(
    SoftwareCategoryId,
    "Backend",
    "backend",
  )

  val UserExperienceSubcategory: Subcategory = Subcategory(
    DesignCategoryId,
    "User experience",
    "user-experience",
  )
}
