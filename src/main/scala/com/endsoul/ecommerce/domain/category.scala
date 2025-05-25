package com.endsoul.ecommerce.domain

import java.util.UUID

import com.endsoul.ecommerce.domain.subcategory.Subcategory

object category {
  final case class CategoryInfo(id: UUID, name: String, slug: String, color: String)
  
  final case class Category private (
      id: UUID,
      name: String,
      slug: String,
      color: String,
      subcategories: List[Subcategory]
  )

  object Category {
    def apply(categoryInfo: CategoryInfo, subcategories: List[Subcategory]): Category = Category(
      id = categoryInfo.id,
      name = categoryInfo.name,
      slug = categoryInfo.slug,
      color = categoryInfo.color,
      subcategories = subcategories
    )
  }
}
